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
package org.lifecompanion.model.impl.categorizedelement.useaction.available;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jdom2.Element;
import org.lifecompanion.controller.useapi.GlobalRuntimeConfigurationController;
import org.lifecompanion.framework.commons.fx.io.XMLObjectSerializer;
import org.lifecompanion.framework.commons.utils.lang.StringUtils;
import org.lifecompanion.model.api.categorizedelement.useaction.DefaultUseActionSubCategories;
import org.lifecompanion.model.api.categorizedelement.useaction.UseActionEvent;
import org.lifecompanion.model.api.categorizedelement.useaction.UseActionTriggerComponentI;
import org.lifecompanion.model.api.io.IOContextI;
import org.lifecompanion.model.api.usevariable.UseVariableI;
import org.lifecompanion.model.impl.categorizedelement.useaction.SimpleUseActionImpl;
import org.lifecompanion.model.impl.exception.LCException;
import org.lifecompanion.model.impl.useapi.GlobalRuntimeConfiguration;
import org.lifecompanion.util.DesktopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class OpenFolderAction extends SimpleUseActionImpl<UseActionTriggerComponentI> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenFolderAction.class);

    private final StringProperty filePath;

    public OpenFolderAction() {
        super(UseActionTriggerComponentI.class);
        this.category = DefaultUseActionSubCategories.COMPUTER_FEATURES;
        this.nameID = "action.open.folder.name";
        this.order = 5;
        this.staticDescriptionID = "action.open.folder.description";
        this.configIconPath = "computeraccess/icon_open_folder.png";
        this.parameterizableAction = true;
        filePath = new SimpleStringProperty();
        this.variableDescriptionProperty().set(getStaticDescription());
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    @Override
    public void execute(final UseActionEvent eventP, final Map<String, UseVariableI<?>> variables) {
        if (!GlobalRuntimeConfigurationController.INSTANCE.isPresent(GlobalRuntimeConfiguration.DISABLE_EXTERNAL_ACTIONS)) {
            if (StringUtils.isNotBlank(filePath.get())) {
                DesktopUtils.openFile(new File(filePath.get()));
            }
        } else {
            LOGGER.info("Ignored {} action because {} is enabled", this.getClass().getSimpleName(), GlobalRuntimeConfiguration.DISABLE_EXTERNAL_ACTIONS);
        }
    }

    @Override
    public Element serialize(final IOContextI contextP) {
        Element elem = super.serialize(contextP);
        XMLObjectSerializer.serializeInto(OpenFolderAction.class, this, elem);
        return elem;
    }

    @Override
    public void deserialize(final Element nodeP, final IOContextI contextP) throws LCException {
        super.deserialize(nodeP, contextP);
        XMLObjectSerializer.deserializeInto(OpenFolderAction.class, this, nodeP);
    }
}
