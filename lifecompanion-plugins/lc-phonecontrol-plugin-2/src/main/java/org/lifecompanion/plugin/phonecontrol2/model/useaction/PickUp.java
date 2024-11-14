package org.lifecompanion.plugin.phonecontrol2.model.useaction;

import org.lifecompanion.model.api.categorizedelement.useaction.UseActionEvent;
import org.lifecompanion.model.api.categorizedelement.useaction.UseActionTriggerComponentI;
import org.lifecompanion.model.api.usevariable.UseVariableI;
import org.lifecompanion.model.impl.categorizedelement.useaction.SimpleUseActionImpl;
import org.lifecompanion.plugin.phonecontrol2.controller.PhoneControlController;

import java.util.Map;

public class PickUp extends SimpleUseActionImpl<UseActionTriggerComponentI> {

    public PickUp() {
        super(UseActionTriggerComponentI.class);
        this.order = 0;
        this.category = PhoneControlActionSubCategories.APPEL;
        this.parameterizableAction = false;
        this.nameID = "phonecontrol2.plugin.action.pick.up.name";
        this.staticDescriptionID = "phonecontrol2.plugin.action.pick.up.description";
        this.configIconPath = "current/call.png";
        this.variableDescriptionProperty().set(this.getStaticDescription());
    }

    @Override
    public void execute(UseActionEvent event, Map<String, UseVariableI<?>> variables) {
        PhoneControlController.INSTANCE.pickUp();
    }
}
