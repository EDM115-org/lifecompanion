/*
 * LifeCompanion AAC and its sub projects
 *
 * Copyright (C) 2014 to 2019 Mathieu THEBAUD
 * Copyright (C) 2020 to 2023 CMRRF KERPAPE (Lorient, France) and CoWork'HIT (Lorient, France)
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

package org.lifecompanion.plugin.caaai.ui.keyoption;

import org.lifecompanion.model.impl.editaction.BasePropertyChangeAction;
import org.lifecompanion.plugin.caaai.model.keyoption.AiSuggestionKeyOption;

public class ChangeExamplePropAction extends BasePropertyChangeAction<Boolean> {
    public ChangeExamplePropAction(AiSuggestionKeyOption option, Boolean wantedValueP) {
        super(option.examplePropertyProperty(), wantedValueP);
    }

    public String getNameID() {
        return "caa.ai.plugin.todo";
    }
}
