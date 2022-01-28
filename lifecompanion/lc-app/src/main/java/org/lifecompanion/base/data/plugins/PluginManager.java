/*
 * LifeCompanion AAC and its sub projects
 *
 * Copyright (C) 2014 to 2019 Mathieu THEBAUD
 * Copyright (C) 2020 to 2021 CMRRF KERPAPE (Lorient, France)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lifecompanion.base.data.plugins;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jdom2.Element;
import org.lifecompanion.api.component.definition.ConfigurationChildComponentI;
import org.lifecompanion.api.component.definition.LCConfigurationI;
import org.lifecompanion.api.component.definition.keyoption.KeyOptionConfigurationViewI;
import org.lifecompanion.api.component.definition.keyoption.KeyOptionI;
import org.lifecompanion.api.component.definition.useaction.BaseUseActionI;
import org.lifecompanion.api.component.definition.useaction.UseActionConfigurationViewI;
import org.lifecompanion.api.component.definition.useevent.UseEventGeneratorConfigurationViewI;
import org.lifecompanion.api.component.definition.useevent.UseEventGeneratorI;
import org.lifecompanion.api.component.definition.useevent.UseVariableDefinitionI;
import org.lifecompanion.api.component.definition.useevent.UseVariableI;
import org.lifecompanion.api.exception.LCException;
import org.lifecompanion.api.io.IOContextI;
import org.lifecompanion.api.io.XMLSerializable;
import org.lifecompanion.api.mode.LCStateListener;
import org.lifecompanion.api.mode.ModeListenerI;
import org.lifecompanion.api.plugins.PluginConfigPropertiesI;
import org.lifecompanion.api.plugins.PluginI;
import org.lifecompanion.api.prediction.CharPredictorI;
import org.lifecompanion.api.prediction.WordPredictorI;
import org.lifecompanion.api.ui.PossibleAddComponentI;
import org.lifecompanion.api.voice.VoiceSynthesizerI;
import org.lifecompanion.base.data.common.LCUtils;
import org.lifecompanion.base.data.config.LCConstant;
import org.lifecompanion.base.data.config.UserBaseConfiguration;
import org.lifecompanion.base.data.control.update.InstallationController;
import org.lifecompanion.base.data.io.IOManager;
import org.lifecompanion.base.view.reusable.GeneralConfigurationStepViewI;
import org.lifecompanion.framework.commons.translation.Translation;
import org.lifecompanion.framework.commons.utils.app.VersionUtils;
import org.lifecompanion.framework.commons.utils.io.IOUtils;
import org.lifecompanion.framework.commons.utils.lang.CollectionUtils;
import org.lifecompanion.framework.commons.utils.lang.LangUtils;
import org.lifecompanion.framework.commons.utils.lang.StringUtils;
import org.lifecompanion.framework.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class that load the plugin classes, and that instantiate found plugins.</br>
 *
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public enum PluginManager implements LCStateListener, ModeListenerI {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    /**
     * Plugin infos list
     */
    private final ObservableList<PluginInfo> pluginInfoList;

    /**
     * Contains loaded plugin instance
     */
    private final Map<String, PluginI> loadedPlugins; // TODO pair of Plugin and PluginInfo ?

    PluginManager() {
        this.loadedPlugins = new HashMap<>();
        this.pluginInfoList = FXCollections.observableArrayList();
    }

    // Class part : "Users"
    // ========================================================================

    public ObservableList<PluginInfo> getPluginInfoList() {
        return pluginInfoList;
    }

    public InputStream getResourceFromPlugin(String resourcePath) {
        for (PluginI plugin : loadedPlugins.values()) {
            InputStream is = plugin.getClass().getResourceAsStream(resourcePath);
            if (is != null) return is;
        }
        return null;
    }

    public boolean isPluginLoaded(String pluginDependencyId) {
        return loadedPlugins.containsKey(pluginDependencyId);
    }

    public Map<String, UseVariableI<?>> generatePluginsUseVariable() {
        Map<String, UseVariableI<?>> vars = new HashMap<>();
        // TODO : variable map should be cached when running in use mode
        // For each plugin, generate the variables
        for (PluginI plugin : this.loadedPlugins.values()) {
            try {
                List<UseVariableDefinitionI> defVar = plugin.getDefinedVariables();
                if (!CollectionUtils.isEmpty(defVar)) {
                    Map<String, UseVariableDefinitionI> varForPlugin = defVar.stream()
                            .collect(Collectors.toMap(UseVariableDefinitionI::getId, v -> v));
                    Map<String, UseVariableI<?>> generateVariables = plugin.generateVariables(varForPlugin);
                    if (generateVariables != null) {
                        vars.putAll(generateVariables);// Plugin can override variables
                    }
                }
            } catch (Throwable t) {
                PluginManager.LOGGER.warn("Couldn't generate plugin use variable for plugin {}", plugin.getClass(), t);
            }
        }
        return vars;
    }
    // ========================================================================


    // PLUGIN LOADING
    //========================================================================
    private final PluginImplementationLoadingHelper<Class<? extends BaseUseActionI>> useActions = new PluginImplementationLoadingHelper<>(BaseUseActionI.class);
    private final PluginImplementationLoadingHelper<Class<? extends UseActionConfigurationViewI>> useActionConfigViews = new PluginImplementationLoadingHelper<>(UseActionConfigurationViewI.class);
    private final PluginImplementationLoadingHelper<Class<? extends CharPredictorI>> charPredictors = new PluginImplementationLoadingHelper<>(CharPredictorI.class);
    private final PluginImplementationLoadingHelper<Class<? extends WordPredictorI>> wordPredictors = new PluginImplementationLoadingHelper<>(WordPredictorI.class);
    private final PluginImplementationLoadingHelper<Class<? extends VoiceSynthesizerI>> voiceSynthesizers = new PluginImplementationLoadingHelper<>(VoiceSynthesizerI.class);
    private final PluginImplementationLoadingHelper<Class<? extends UseEventGeneratorI>> useEventGenerators = new PluginImplementationLoadingHelper<>(UseEventGeneratorI.class);
    private final PluginImplementationLoadingHelper<Class<? extends UseEventGeneratorConfigurationViewI>> useEventGeneratorConfigViews = new PluginImplementationLoadingHelper<>(UseEventGeneratorConfigurationViewI.class);
    private final PluginImplementationLoadingHelper<Class<? extends KeyOptionI>> keyOptions = new PluginImplementationLoadingHelper<>(KeyOptionI.class);
    private final PluginImplementationLoadingHelper<Class<? extends KeyOptionConfigurationViewI>> keyOptionConfigViews = new PluginImplementationLoadingHelper<>(KeyOptionConfigurationViewI.class);
    private final PluginImplementationLoadingHelper<Class<? extends PossibleAddComponentI>> possibleAddComponents = new PluginImplementationLoadingHelper<>(PossibleAddComponentI.class);
    private final PluginImplementationLoadingHelper<Class<? extends GeneralConfigurationStepViewI>> generalConfigurationSteps = new PluginImplementationLoadingHelper<>(GeneralConfigurationStepViewI.class);
    private final PluginImplementationLoadingHelper<UseVariableDefinitionI> useVariableDefinitions = new PluginImplementationLoadingHelper<>(null);
    private final PluginImplementationLoadingHelper<String[]> stylesheets = new PluginImplementationLoadingHelper<>(null);

    public PluginImplementationLoadingHelper<Class<? extends BaseUseActionI>> getUseActions() {
        return useActions;
    }

    public PluginImplementationLoadingHelper<Class<? extends UseActionConfigurationViewI>> getUseActionConfigViews() {
        return useActionConfigViews;
    }

    public PluginImplementationLoadingHelper<Class<? extends CharPredictorI>> getCharPredictors() {
        return charPredictors;
    }

    public PluginImplementationLoadingHelper<Class<? extends WordPredictorI>> getWordPredictors() {
        return wordPredictors;
    }

    public PluginImplementationLoadingHelper<Class<? extends VoiceSynthesizerI>> getVoiceSynthesizers() {
        return voiceSynthesizers;
    }

    public PluginImplementationLoadingHelper<Class<? extends UseEventGeneratorI>> getUseEventGenerators() {
        return useEventGenerators;
    }

    public PluginImplementationLoadingHelper<Class<? extends UseEventGeneratorConfigurationViewI>> getUseEventGeneratorConfigViews() {
        return useEventGeneratorConfigViews;
    }

    public PluginImplementationLoadingHelper<Class<? extends KeyOptionI>> getKeyOptions() {
        return keyOptions;
    }

    public PluginImplementationLoadingHelper<Class<? extends KeyOptionConfigurationViewI>> getKeyOptionConfigViews() {
        return keyOptionConfigViews;
    }

    public PluginImplementationLoadingHelper<Class<? extends PossibleAddComponentI>> getPossibleAddComponents() {
        return possibleAddComponents;
    }

    public PluginImplementationLoadingHelper<UseVariableDefinitionI> getUseVariableDefinitions() {
        return useVariableDefinitions;
    }

    public PluginImplementationLoadingHelper<String[]> getStylesheets() {
        return stylesheets;
    }

    public PluginImplementationLoadingHelper<Class<? extends GeneralConfigurationStepViewI>> getGeneralConfigurationSteps() {
        return generalConfigurationSteps;
    }

    private final List<PluginImplementationLoadingHelper<? extends Class<?>>> pluginImplementationLoadingHelpers = Arrays.asList(
            useActions, useActionConfigViews, charPredictors, wordPredictors, voiceSynthesizers, useEventGenerators,
            useEventGeneratorConfigViews, keyOptions, keyOptionConfigViews, possibleAddComponents, generalConfigurationSteps
    );

    private List<PluginInfo> getPluginById(String id) {
        return getPluginById(id, p -> true);
    }

    private List<PluginInfo> getPluginById(String id, Predicate<PluginInfo> pluginInfoPredicate) {
        return pluginInfoList.stream()
                .filter(p -> StringUtils.isEquals(id, p.getPluginId()))
                .filter(pluginInfoPredicate)
                .collect(Collectors.toList());
    }

    private PluginInfo loadPluginInfo(File pluginJar) {
        try {
            return PluginInfo.createFromJarManifest(pluginJar);
        } catch (Exception e) {
            LOGGER.error("Couldn't load plugin info from file {}", pluginJar, e);
            return null;
        }
    }

    private void loadPlugin(PluginInfo pluginInfo, File pluginJar) {
        if (pluginInfo != null) {
            LOGGER.info("Will try to load plugin {}", pluginInfo);
            try {
                // Try to load plugin class
                Class<? extends PluginI> pluginClassType = (Class<? extends PluginI>) Class.forName(pluginInfo.getPluginClass());
                Constructor<? extends PluginI> constructor = pluginClassType.getConstructor();
                if (constructor == null) {
                    throw new Exception("Couldn't find default public constructor on plugin type " + pluginInfo.getPluginClass() + ", you should provide a public no arg constructor");
                }

                PluginI pluginInstance = constructor.newInstance();
                startPlugin(pluginInfo, pluginInstance);

                // Add to plugin list
                loadedPlugins.put(pluginInfo.getPluginId(), pluginInstance);

                // Will now look for implementations
                try (ScanResult scanResult = new ClassGraph()
                        .whitelistJars(pluginJar.getName())
                        .whitelistPackages(pluginInfo.getPluginPackageScanningBase().split(","))
                        .enableClassInfo()
                        .scan()
                ) {
                    // Register every serializable types to IOManager
                    List<Class<? extends XMLSerializable>> serializableClassesInPlugin = getClassesInPlugin(scanResult, XMLSerializable.class);
                    IOManager.addSerializableTypes(serializableClassesInPlugin, pluginInfo);

                    // Find and register plugin custom implementations
                    for (PluginImplementationLoadingHelper pluginImplementationLoadingHelper : pluginImplementationLoadingHelpers) {
                        scanForImplementationInPlugin(pluginInfo, scanResult, pluginImplementationLoadingHelper);
                    }
                }
                pluginInfo.stateProperty().set(PluginInfoState.LOADED);
            } catch (Throwable t) {
                LOGGER.error("Failed to load plugin : {}", pluginInfo, t);
                pluginInfo.stateProperty().set(PluginInfoState.ERROR);
            } finally {
                pluginInfoList.add(pluginInfo);
            }
        }
    }

    private <T extends Class<T>> void scanForImplementationInPlugin(PluginInfo pluginInfo, ScanResult scanResult, PluginImplementationLoadingHelper<T> pluginImplementationLoadingHelper) {
        List<Class<? extends T>> classes = getClassesInPlugin(scanResult, pluginImplementationLoadingHelper.getType());
        pluginImplementationLoadingHelper.elementAdded(pluginInfo.getPluginId(), (Collection<T>) classes);
        LOGGER.info("Found {} {} implementations in plugin {}", classes.size(), pluginImplementationLoadingHelper.getType().getName(), pluginInfo.getPluginId());
    }

    private <T> List<Class<? extends T>> getClassesInPlugin(ScanResult scanResult, Class<T> type) {
        ClassInfoList actionImplementation = scanResult.getClassesImplementing(type.getName()).filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface());
        return actionImplementation.loadClasses().stream().map(c -> (Class<? extends T>) c).collect(Collectors.toList());
    }

    private void startPlugin(PluginInfo pluginInfo, PluginI plugin) {
        plugin.start(null);//TODO : datafolder
        // Load language
        String[] languageFiles = plugin.getLanguageFiles(UserBaseConfiguration.INSTANCE.userLanguageProperty().get());
        if (languageFiles != null) {
            for (String languageFilePath : languageFiles) {
                try (InputStream fis = plugin.getClass().getResourceAsStream(languageFilePath)) {
                    Translation.INSTANCE.load(languageFilePath, fis);
                    PluginManager.LOGGER.info("Plugin language file {} loaded for {}", languageFilePath, pluginInfo.getPluginName());
                } catch (Exception e) {
                    PluginManager.LOGGER.error("Couldn't load the {} plugin language file {}", pluginInfo.getPluginId(), languageFilePath, e);
                    //result.getErrorTextIds().add("plugin.error.invalid.lang.file");
                }
            }
        }

        // Load JavaFX stylesheets
        String[] javaFXStylesheets = plugin.getJavaFXStylesheets();
        if (javaFXStylesheets != null) {
            stylesheets.elementAdded(pluginInfo.getPluginId(), Collections.singleton(javaFXStylesheets));
        }

        // Load use variable definitions
        List<UseVariableDefinitionI> definedVariables = plugin.getDefinedVariables();
        if (LangUtils.isNotEmpty(definedVariables)) {
            LOGGER.info("Found {} use variable definition in plugin {}", definedVariables.size(), pluginInfo.getPluginId());
            useVariableDefinitions.elementAdded(pluginInfo.getPluginId(), definedVariables);
        }
    }
    //========================================================================

    // PLUGIN ADD/REMOVE
    //========================================================================
    public Pair<String, PluginInfo> tryToAddPluginFrom(File pluginFile) throws Exception {
        PluginInfo addedPluginInfo = loadPluginInfo(pluginFile);
        if (addedPluginInfo == null) LCException.newException().withMessageId("plugin.error.load.info.from.jar").buildAndThrow();

        // Check min app version
        // Note : no plugin minAppVersion means old plugin so it is not compatible with new plugin API
        if (addedPluginInfo.getPluginMinAppVersion() == null || VersionUtils.compare(InstallationController.INSTANCE.getBuildProperties().getVersionLabel(), addedPluginInfo.getPluginMinAppVersion()) < 0) {
            LCException.newException().withMessage(addedPluginInfo.getPluginMinAppVersion() != null ? "plugin.error.min.app.version.with.number" : "plugin.error.min.app.version.without.number", addedPluginInfo.getPluginMinAppVersion()).buildAndThrow();
        }

        // TODO : handle removed then added plugin

        // If the plugin is already in app with the same or > version
        List<PluginInfo> pluginsWithSameId = getPluginById(addedPluginInfo.getPluginId(), pluginInfo -> pluginInfo.stateProperty().get() != PluginInfoState.REMOVED);
        boolean newerVersionFound = false;
        if (!CollectionUtils.isEmpty(pluginsWithSameId)) {
            for (PluginInfo pluginWithSameId : pluginsWithSameId) {
                // Found a newer version loaded
                if (VersionUtils.compare(pluginWithSameId.getPluginVersion(), addedPluginInfo.getPluginVersion()) >= 0) {
                    newerVersionFound = true;
                }
                // Found a older version loaded : remove previous version and add new one
                else {
                    removePlugin(pluginWithSameId);
                }
            }
        }
        if (newerVersionFound) {
            return Pair.of(Translation.getText("plugin.load.success.base.not.loaded.update", addedPluginInfo.getPluginName(), addedPluginInfo.getPluginVersion()), addedPluginInfo);
        }

        // Add to plugin jar directory (if the same file is not already there)
        File destinationPluginFile = new File(LCConstant.PATH_PLUGIN_JAR_DIR + addedPluginInfo.getFileName());
        IOUtils.createParentDirectoryIfNeeded(destinationPluginFile);
        if (!destinationPluginFile.exists()) {
            IOUtils.copyFiles(pluginFile, destinationPluginFile);
        } else {
            LOGGER.warn("Didn't copy the new plugin file {} because the file already exists in plugin directory", pluginFile);
        }

        // Modify and save classpath config
        Set<String> pluginCpConfig = readClasspathConfigurationOrGetDefault();
        pluginCpConfig.add(LCConstant.PATH_PLUGIN_JAR_DIR + addedPluginInfo.getFileName());
        writeClasspathConfiguration(pluginCpConfig);

        // Add to info list
        pluginInfoList.add(addedPluginInfo);

        return Pair.of(Translation.getText("plugin.load.success.base.message", addedPluginInfo.getPluginName(), addedPluginInfo.getPluginVersion()), addedPluginInfo);
    }

    public void removePlugin(PluginInfo pluginInfo) {
        pluginInfo.stateProperty().set(PluginInfoState.REMOVED);
        Set<String> pluginCpConfig = readClasspathConfigurationOrGetDefault();
        boolean remove = pluginCpConfig.remove(LCConstant.PATH_PLUGIN_JAR_DIR + pluginInfo.getFileName());
        LOGGER.info("Remove plugin {} = {}", pluginInfo, remove);
        writeClasspathConfiguration(pluginCpConfig);
    }

    private Set<String> readClasspathConfigurationOrGetDefault() {
        Set<String> pluginInCp = new HashSet<>();
        File cpConfigFile = new File(LCConstant.PATH_PLUGIN_CP_FILE);
        if (cpConfigFile.exists()) {
            try (Scanner scan = new Scanner(cpConfigFile, StandardCharsets.UTF_8)) {
                pluginInCp.addAll(Arrays.asList(scan.nextLine().split(File.pathSeparator)));
            } catch (Exception e) {
                LOGGER.error("Couldn't read the classpath configuration for plugins", e);
            }
        }
        return pluginInCp;
    }

    private void writeClasspathConfiguration(Set<String> pluginsInCp) {
        File cpConfigFile = new File(LCConstant.PATH_PLUGIN_CP_FILE);
        IOUtils.createParentDirectoryIfNeeded(cpConfigFile);
        if (CollectionUtils.isEmpty(pluginsInCp)) {
            cpConfigFile.delete();
        } else {
            try (PrintWriter pw = new PrintWriter(cpConfigFile, StandardCharsets.UTF_8)) {
                pw.println(pluginsInCp.stream().collect(Collectors.joining(File.pathSeparator)));
            } catch (Exception e) {
                LOGGER.error("Couldn't write the classpath configuration for plugins", e);
            }
        }
    }
    //========================================================================

    // Class part : "Init/stop"
    // ========================================================================
    @Override
    public void lcStart() {
        if (!InstallationController.INSTANCE.isUpdateDownloadFinished()) {
            try {
                // Detect plugin in classpath configuration and try to load them
                Set<String> classPathPlugins = readClasspathConfigurationOrGetDefault();
                for (String classPathPlugin : classPathPlugins) {
                    LOGGER.info("Will try to load plugin from classpath configuration : {}", classPathPlugin);
                    File pluginJarFile = new File(classPathPlugin);
                    loadPlugin(loadPluginInfo(pluginJarFile), pluginJarFile);
                }

                // This part is for dev only
                if (LCUtils.safeParseBoolean(System.getProperty("org.lifecompanion.load.plugins.from.cp"))) {
                    List<File> jarFiles = new ClassGraph().getClasspathFiles();
                    for (File jarFile : jarFiles) {
                        if (jarFile.getName().contains("plugin")) {
                            PluginInfo pluginInfo = loadPluginInfo(jarFile);
                            if (pluginInfo != null && !loadedPlugins.containsKey(pluginInfo.getPluginId())) {
                                loadPlugin(pluginInfo, jarFile);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("Global problem when trying to load plugins...", t);
            }

            // Launch plugin update check
            InstallationController.INSTANCE.launchPluginUpdateCheckTask(false);
        }
    }

    @Override
    public void lcExit() {
        for (PluginI plugin : this.loadedPlugins.values()) {
            plugin.stop(null);//TODO : datafolder
        }
    }

    @Override
    public void modeStart(final LCConfigurationI configuration) {
        for (PluginI plugin : this.loadedPlugins.values()) {
            try {
                plugin.modeStart(configuration);
            } catch (Throwable t) {
                LOGGER.error("Fire modeStart(...) on plugin {} failed", plugin.getClass(), t);
            }
        }
    }

    @Override
    public void modeStop(final LCConfigurationI configuration) {
        for (PluginI plugin : this.loadedPlugins.values()) {
            try {
                plugin.modeStop(configuration);
            } catch (Throwable t) {
                LOGGER.error("Fire modeStop(...) on plugin {} failed", plugin.getClass(), t);
            }
        }
    }
    // ========================================================================


    // PLUGIN IO
    // ========================================================================
    private static final String NODE_PLUGINS = "Plugins";
    private static final String NODE_PLUGIN_DEPENDENCIES = "PluginDependencies";
    private static final String NODE_PLUGIN_CUSTOM_INFORMATIONS = "PluginCustomInformations";
    private static final String NODE_PLUGIN_CUSTOM_INFORMATION = "PluginCustomInformation";
    private static final String ATB_PLUGIN_ID = "pluginId";

    public void serializePluginInformation(final ConfigurationChildComponentI pluginUser, final IOContextI context, final Element configurationElement) {
        // Detected used plugin (auto and manual)
        HashSet<String> usedPluginIds = new HashSet<>();
        usedPluginIds.addAll(context.getAutomaticPluginDependencyIds());
        LCConfigurationI parentConfiguration = pluginUser.configurationParentProperty().get();
        if (parentConfiguration == null) {
            LOGGER.warn("Serialized plugin user haven't any configuration parent : {}", pluginUser);
        } else {
            usedPluginIds.addAll(parentConfiguration.getManualPluginDependencyIds());
        }

        // Save dependencies
        Element pluginDependenciesElement = new Element(PluginManager.NODE_PLUGIN_DEPENDENCIES);
        usedPluginIds.stream().flatMap(id -> getPluginById(id, pi -> pi.stateProperty().get() == PluginInfoState.LOADED).stream()).map(pi -> pi.serialize(context)).forEach(pluginDependenciesElement::addContent);

        // Save information
        Element pluginInformations = new Element(PluginManager.NODE_PLUGIN_CUSTOM_INFORMATIONS);
        for (String pluginId : usedPluginIds) {

            PluginConfigPropertiesI configurationProperties = parentConfiguration != null ? parentConfiguration.getPluginConfigProperties(pluginId, PluginConfigPropertiesI.class) : null;
            if (configurationProperties != null) {
                Element pluginXmlNode = configurationProperties.serialize(context);
                if (pluginXmlNode != null) {
                    Element customInfo = new Element(NODE_PLUGIN_CUSTOM_INFORMATION);
                    customInfo.addContent(pluginXmlNode);
                    customInfo.setAttribute(ATB_PLUGIN_ID, pluginId);
                    pluginInformations.addContent(customInfo);
                }
            } else {
                LOGGER.warn("Didn't find any configuration properties for plugin {}", pluginId);
            }
        }

        Element elementPlugins = new Element(NODE_PLUGINS);
        configurationElement.addContent(elementPlugins);
        elementPlugins.addContent(pluginDependenciesElement);
        elementPlugins.addContent(pluginInformations);
    }

    public void deserializePluginInformation(final ConfigurationChildComponentI pluginUser, final IOContextI context, final Element configurationElement) throws LCException {
        if (pluginUser == null || pluginUser.configurationParentProperty().get() == null) {
            LOGGER.warn("Plugin user {} haven't any configuration parent, plugin information can't be loaded", pluginUser);
            return;
        }
        LCConfigurationI parentConfiguration = pluginUser.configurationParentProperty().get();
        Element pluginsElement = configurationElement.getChild(PluginManager.NODE_PLUGINS);
        if (pluginsElement != null) {
            // Load each plugin custom information
            Element pluginInformations = pluginsElement.getChild(PluginManager.NODE_PLUGIN_CUSTOM_INFORMATIONS);
            for (Element pluginInformation : pluginInformations.getChildren()) {
                String pluginId = pluginInformation.getAttributeValue(ATB_PLUGIN_ID);
                PluginI pluginI = this.loadedPlugins.get(pluginId);
                if (pluginI == null || pluginInformation.getChildren().size() < 1) {
                    LOGGER.warn("Couldn't load plugin information from XML because plugin {} is not loaded or there is no information", pluginId);
                } else {
                    final PluginConfigPropertiesI pluginConfigProperties = parentConfiguration.getPluginConfigProperties(pluginId, PluginConfigPropertiesI.class);
                    pluginConfigProperties.deserialize(pluginInformation.getChildren().get(0), context);
                }
            }
        }
    }

    // FIXME user dependencies
    public String checkPluginDependencies(final Element xmlRoot) throws LCException {
        Element pluginsElement = xmlRoot.getChild(PluginManager.NODE_PLUGINS);
        if (pluginsElement != null) {

            StringBuilder warningMessage = new StringBuilder();

            Element pluginDependenciesElement = pluginsElement.getChild(PluginManager.NODE_PLUGIN_DEPENDENCIES);
            for (Element pluginDependencyElement : pluginDependenciesElement.getChildren()) {
                PluginInfo usedPluginInfo = new PluginInfo();
                usedPluginInfo.deserialize(pluginDependencyElement, null);
                PluginInfo loadedPluginInfo = getPluginById(usedPluginInfo.getPluginId(),pi -> pi.stateProperty().get() == PluginInfoState.LOADED).stream().findFirst().orElseGet(() -> null);
                if (loadedPluginInfo == null) {
                    warningMessage.append("\n - ").append(Translation.getText("configuration.loading.plugin.not.loaded", usedPluginInfo.getPluginName(), usedPluginInfo.getPluginVersion()));
                } else if (VersionUtils.compare(loadedPluginInfo.getPluginVersion(), usedPluginInfo.getPluginVersion()) < 0) {
                    warningMessage.append("\n - ").append(Translation.getText("configuration.loading.plugin.older.version", loadedPluginInfo.getPluginName(), loadedPluginInfo.getPluginVersion(),
                            usedPluginInfo.getPluginVersion()));
                }
            }
            return warningMessage.length() > 0 ? warningMessage.toString() : null;
        }
        return null;
    }

    public Map<String, PluginConfigPropertiesI> getPluginConfigurationPropertiesMap(ObjectProperty<LCConfigurationI> parentConfiguration) {
        Map<String, PluginConfigPropertiesI> pluginConfigurationPropertiesMap = new HashMap<>();
        pluginInfoList.stream()
                .filter(p -> p.stateProperty().get() == PluginInfoState.LOADED)
                .forEach(p -> pluginConfigurationPropertiesMap.put(p.getPluginId(), loadedPlugins.get(p.getPluginId()).newPluginConfigProperties(parentConfiguration)));
        return Collections.unmodifiableMap(pluginConfigurationPropertiesMap);
    }
    // ========================================================================


}