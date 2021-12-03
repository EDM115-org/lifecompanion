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
package org.lifecompanion.config.view.pane.tabs.selected.part;

import org.lifecompanion.api.component.definition.GridPartKeyComponentI;
import org.lifecompanion.api.component.definition.ImageUseComponentI;
import org.lifecompanion.base.data.component.keyoption.simplercomp.KeyListNodeKeyOption;
import org.lifecompanion.config.data.control.SelectionController;
import org.lifecompanion.config.view.reusable.image.ImageUseComponentSelectorControl;
import org.lifecompanion.config.view.reusable.ribbonmenu.RibbonBasePart;
import org.lifecompanion.framework.commons.translation.Translation;
import org.lifecompanion.framework.commons.ui.LCViewInitHelper;

/**
 * Ribbon part to select a image on a component
 *
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public class ImageUseComponentRibbonPart extends RibbonBasePart<ImageUseComponentI> implements LCViewInitHelper {
    private ImageUseComponentSelectorControl imageUseComponentSelectorControl;

    public ImageUseComponentRibbonPart() {
        this.initAll();
    }

    // Class part : "UI"
    //========================================================================
    @Override
    public void initUI() {
        imageUseComponentSelectorControl = new ImageUseComponentSelectorControl();

        this.setContent(imageUseComponentSelectorControl);
        this.setTitle(Translation.getText("image.component.ribbon.title"));
    }
    //========================================================================

    // Class part : "Binding"
    //========================================================================
    @Override
    public void initBinding() {
        SelectionController.INSTANCE.selectedComponentProperty().addListener((o, oldV, newV) -> {
            if (newV instanceof ImageUseComponentI) {
                this.model.set((ImageUseComponentI) newV);
            } else {
                this.model.set(null);
            }
        });
        this.imageUseComponentSelectorControl.modelProperty().bind(model);
        this.initVisibleAndManagedBinding(GridPartKeyComponentI.class, KeyListNodeKeyOption.class);
    }

    @Override
    public void bind(ImageUseComponentI model) {
    }

    @Override
    public void unbind(ImageUseComponentI model) {
    }
    //========================================================================

}
