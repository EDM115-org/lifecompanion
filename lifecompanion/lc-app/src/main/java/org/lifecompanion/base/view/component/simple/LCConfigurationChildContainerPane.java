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
package org.lifecompanion.base.view.component.simple;

import javafx.scene.layout.Pane;
import org.lifecompanion.api.component.definition.LCConfigurationI;

/**
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public class LCConfigurationChildContainerPane extends Pane {

    private final LCConfigurationI configuration;

    public LCConfigurationChildContainerPane(final Pane parentPane, final LCConfigurationI configuration) {
        this.prefWidthProperty().bind(parentPane.widthProperty());
        this.prefHeightProperty().bind(parentPane.heightProperty());
        parentPane.getChildren().add(this);
        this.configuration = configuration;
    }

    public LCConfigurationI getConfiguration() {
        return configuration;
    }
}