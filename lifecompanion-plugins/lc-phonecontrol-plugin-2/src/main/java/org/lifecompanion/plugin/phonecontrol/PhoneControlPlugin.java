package org.lifecompanion.plugin.phonecontrol;

import javafx.beans.property.ObjectProperty;
import org.lifecompanion.model.api.configurationcomponent.LCConfigurationI;
import org.lifecompanion.model.api.plugin.PluginConfigPropertiesI;
import org.lifecompanion.model.api.plugin.PluginI;
import org.lifecompanion.model.api.usevariable.UseVariableDefinitionI;
import org.lifecompanion.model.api.usevariable.UseVariableI;
import org.lifecompanion.model.impl.usevariable.IntegerUseVariable;
import org.lifecompanion.model.impl.usevariable.LongUseVariable;
import org.lifecompanion.model.impl.usevariable.StringUseVariable;
import org.lifecompanion.model.impl.usevariable.UseVariableDefinition;
import org.lifecompanion.plugin.phonecontrol.controller.PhoneControlController;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PhoneControlPlugin implements PluginI {
    public static final String ID = "lc-phonecontrol-plugin";

    // RES
    //========================================================================
    @Override
    public String[] getLanguageFiles(final String languageCode) {
        return new String[]{"/text/" + languageCode + "_phonecontrol_plugin.xml"};
    }

    @Override
    public String[] getJavaFXStylesheets() {
        return new String[]{"/style/phonecontrol_plugin.css"};
    }

    @Override
    public String[] getDefaultConfigurations(String languageCode) {
        return new String[]{"/configurations/phonecontrol-configuration_principal.lcc"};
    }
    //========================================================================


    // PLUGIN START/STOP
    //========================================================================
    @Override
    public void start(File dataDirectory) {
        // Plugin global init here
    }

    @Override
    public void stop(File dataDirectory) {
        // Plugin global stop here
    }
    //========================================================================

    // MODE START/STOP
    //========================================================================
    @Override
    public void modeStart(LCConfigurationI configuration) {
        PhoneControlController.INSTANCE.modeStart(configuration);
    }

    @Override
    public void modeStop(LCConfigurationI configuration) {
        PhoneControlController.INSTANCE.modeStop(configuration);
    }
    //========================================================================

    // VARIABLES
    //========================================================================
    @Override
    public List<UseVariableDefinitionI> getDefinedVariables() {
        return Arrays.asList(
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONNECTED_DEVICE,
                        "phonecontrol.plugin.use.variable.connected.device.name",
                        "phonecontrol.plugin.use.variable.connected.device.description",
                        "phonecontrol.plugin.use.variable.connected.device.example"),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACTS_LIST,
                        "phonecontrol.plugin.use.variable.contacts.list.name",
                        "phonecontrol.plugin.use.variable.contacts.list.description",
                        "phonecontrol.plugin.use.variable.contacts.list.example"),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CALL_NUMBER,
                        "phonecontrol.plugin.use.variable.call.number.name",
                        "phonecontrol.plugin.use.variable.call.number.description",
                        "phonecontrol.plugin.use.variable.call.number.example"),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACTS_LETTRES,
                        "phonecontrol.plugin.use.variable.contacts.lettres.name",
                        "phonecontrol.plugin.use.variable.contacts.lettres.description",
                        "phonecontrol.plugin.use.variable.contacts.lettres.example"),
                new UseVariableDefinition(PhoneControlController.VAR_ID_NB_CONTACTS_LETTRES,
                        "phonecontrol.plugin.use.variable.nb.contacts.lettres.name",
                        "phonecontrol.plugin.use.variable.nb.contacts.lettres.description",
                        "phonecontrol.plugin.use.variable.nb.contacts.lettres.example"),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACT,
                        "phonecontrol.plugin.use.variable.contact.name",
                        "phonecontrol.plugin.use.variable.contact.description",
                        "phonecontrol.plugin.use.variable.contact.example",
                        100),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACT_PRECEDENT,
                        "phonecontrol.plugin.use.variable.contact.precedent.name",
                        "phonecontrol.plugin.use.variable.contact.precedent.description",
                        "phonecontrol.plugin.use.variable.contact.precedent.example",
                        100),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACT_SUIVANT,
                        "phonecontrol.plugin.use.variable.contact.suivant.name",
                        "phonecontrol.plugin.use.variable.contact.suivant.description",
                        "phonecontrol.plugin.use.variable.contact.suivant.example",
                        100),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CALL_TIME,
                        "phonecontrol.plugin.use.variable.call.time.name",
                        "phonecontrol.plugin.use.variable.call.time.description",
                        "phonecontrol.plugin.use.variable.call.time.example",
                        100),
                new UseVariableDefinition(PhoneControlController.VAR_ID_CONTACT_RECHERCHE,
                        "phonecontrol.plugin.use.variable.contact.recherche.name",
                        "phonecontrol.plugin.use.variable.contact.recherche.description",
                        "phonecontrol.plugin.use.variable.contact.recherche.example",
                        100)
        );
    }

    @Override
    public Function<UseVariableDefinitionI, UseVariableI<?>> getSupplierForUseVariable(String id) {
        return switch (id) {
            case PhoneControlController.VAR_ID_CONNECTED_DEVICE -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getSelectedDeviceName());
            case PhoneControlController.VAR_ID_CALL_NUMBER -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getCallNumber());
            case PhoneControlController.VAR_ID_CONTACTS_LIST -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.listContactsString());
            case PhoneControlController.VAR_ID_CONTACTS_LETTRES -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getContactsByFirstLetter());
            case PhoneControlController.VAR_ID_NB_CONTACTS_LETTRES -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getNbContactsByFirstLetter());
            case PhoneControlController.VAR_ID_CONTACT -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getContact());
            case PhoneControlController.VAR_ID_CONTACT_PRECEDENT -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getContactPrecedent());
            case PhoneControlController.VAR_ID_CONTACT_SUIVANT -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getContactSuivant());
            case PhoneControlController.VAR_ID_CALL_TIME -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getCallTime());
            case PhoneControlController.VAR_ID_CONTACT_RECHERCHE -> def -> new StringUseVariable(def, PhoneControlController.INSTANCE.getContactRecherche());
            default -> null;
        };
    }
    //========================================================================

    // PLUGIN PROPERTIES
    //========================================================================
    @Override
    public PluginConfigPropertiesI newPluginConfigProperties(ObjectProperty<LCConfigurationI> parentConfiguration) {
        return new PhoneControlPluginProperties(parentConfiguration);
    }
    //========================================================================
}
