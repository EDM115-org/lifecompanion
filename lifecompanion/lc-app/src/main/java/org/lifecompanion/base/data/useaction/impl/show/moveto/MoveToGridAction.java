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

package org.lifecompanion.base.data.useaction.impl.show.moveto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.fxmisc.easybind.EasyBind;
import org.jdom2.Element;
import org.lifecompanion.api.component.definition.GridComponentI;
import org.lifecompanion.api.component.definition.useaction.UseActionEvent;
import org.lifecompanion.api.component.definition.useaction.UseActionTriggerComponentI;
import org.lifecompanion.api.component.definition.useevent.UseVariableI;
import org.lifecompanion.api.exception.LCException;
import org.lifecompanion.api.io.IOContextI;
import org.lifecompanion.api.useaction.category.DefaultUseActionSubCategories;
import org.lifecompanion.base.data.component.utils.ComponentHolder;
import org.lifecompanion.base.data.control.SelectionModeController;
import org.lifecompanion.base.data.useaction.baseimpl.SimpleUseActionImpl;
import org.lifecompanion.framework.commons.fx.io.XMLObjectSerializer;
import org.lifecompanion.framework.commons.fx.translation.TranslationFX;
import org.lifecompanion.framework.commons.translation.Translation;

import java.util.Map;

/**
 * Action to change the current display/scanned grid.
 *
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public class MoveToGridAction extends SimpleUseActionImpl<UseActionTriggerComponentI> {

    private StringProperty targetGridId;
    private ComponentHolder<GridComponentI> targetGrid;

    public MoveToGridAction() {
        super(UseActionTriggerComponentI.class);
        this.order = 0;
        this.category = DefaultUseActionSubCategories.MOVE_TO_SIMPLE;
        this.nameID = "go.to.grid.name";
        this.movingAction = true;
        this.staticDescriptionID = "go.to.grid.static.description";
        this.configIconPath = "show/icon_move_to_grid.png";
        this.targetGridId = new SimpleStringProperty();
        this.targetGrid = new ComponentHolder<>(this.targetGridId, this.parentComponentProperty());
        this.variableDescriptionProperty().bind(TranslationFX.getTextBinding("go.to.grid.variable.description",
                EasyBind.select(this.targetGridProperty()).selectObject(GridComponentI::nameProperty).orElse(Translation.getText("grid.none.selected"))));

    }

    public ObjectProperty<GridComponentI> targetGridProperty() {
        return this.targetGrid.componentProperty();
    }

    @Override
    public void execute(final UseActionEvent eventP, final Map<String, UseVariableI<?>> variables) {
        if (this.targetGridProperty().get() != null) {
            SelectionModeController.INSTANCE.goToGridPart(this.targetGridProperty().get());
        }
    }

    @Override
    public void idsChanged(final Map<String, String> changes) {
        super.idsChanged(changes);
        this.targetGrid.idsChanged(changes);
    }

    // Class part : "XML"
    //========================================================================
    @Override
    public Element serialize(final IOContextI contextP) {
        Element elem = super.serialize(contextP);
        XMLObjectSerializer.serializeInto(MoveToGridAction.class, this, elem);
        return elem;
    }

    @Override
    public void deserialize(final Element nodeP, final IOContextI contextP) throws LCException {
        super.deserialize(nodeP, contextP);
        XMLObjectSerializer.deserializeInto(MoveToGridAction.class, this, nodeP);
    }
    //========================================================================
}