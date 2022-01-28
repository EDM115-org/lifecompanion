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

package org.lifecompanion.config.data.action.impl;

import org.lifecompanion.api.action.definition.BaseConfigActionI;
import org.lifecompanion.api.action.definition.UndoRedoActionI;
import org.lifecompanion.api.component.definition.useevent.UseEventGeneratorI;
import org.lifecompanion.api.component.definition.useevent.UseEventManagerI;
import org.lifecompanion.api.exception.LCException;

/**
 * Class that keep every config actions relative to use action.
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public class UseEventGeneratorActions {

	public static class AddUseEventAction implements UndoRedoActionI {
		private UseEventManagerI manager;
		private UseEventGeneratorI eventGenerator;

		public AddUseEventAction(final UseEventManagerI manager, final UseEventGeneratorI eventGenerator) {
			this.manager = manager;
			this.eventGenerator = eventGenerator;
		}

		@Override
		public void doAction() throws LCException {
			this.manager.componentEventGenerators().add(this.eventGenerator);
		}

		@Override
		public String getNameID() {
			return "action.use.event.add";
		}

		@Override
		public void undoAction() throws LCException {
			this.manager.componentEventGenerators().remove(this.eventGenerator);
		}

		@Override
		public void redoAction() throws LCException {
			this.doAction();
		}

	}

	public static class RemoveUseEventAction implements UndoRedoActionI {
		private UseEventManagerI manager;
		private UseEventGeneratorI eventGenerator;

		public RemoveUseEventAction(final UseEventManagerI manager, final UseEventGeneratorI eventGenerator) {
			this.manager = manager;
			this.eventGenerator = eventGenerator;
		}

		@Override
		public void doAction() throws LCException {
			this.manager.componentEventGenerators().remove(this.eventGenerator);
		}

		@Override
		public String getNameID() {
			return "action.use.event.remove";
		}

		@Override
		public void undoAction() throws LCException {
			this.manager.componentEventGenerators().add(this.eventGenerator);
		}

		@Override
		public void redoAction() throws LCException {
			this.doAction();
		}
	}

	public static class EditUseEventAction implements BaseConfigActionI {

		@Override
		public void doAction() throws LCException {
			//Do nothing, this action is just created to trace
		}

		@Override
		public String getNameID() {
			return "action.use.event.edit";
		}
	}
}