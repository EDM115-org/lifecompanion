package org.lifecompanion.plugin.phonecontrol.event;

import org.lifecompanion.model.api.configurationcomponent.LCConfigurationI;
import org.lifecompanion.model.impl.categorizedelement.useevent.BaseUseEventGeneratorImpl;
import org.lifecompanion.plugin.phonecontrol.PhoneControlController;
import org.lifecompanion.plugin.phonecontrol.event.categories.PhoneControlEventSubCategories;

public class OnCallEndedEventGenerator extends BaseUseEventGeneratorImpl {

    private final Runnable callEndedCallback;

    public OnCallEndedEventGenerator() {
        super();
        this.parameterizableAction = false;
        this.order = 20;
        this.category = PhoneControlEventSubCategories.MISC;
        this.nameID = "phonecontrol1.plugin.event.misc.call.hangup.name";
        this.staticDescriptionID = "phonecontrol1.plugin.event.misc.call.hangup.description";
        this.variableDescriptionProperty().set(this.getStaticDescription());
        callEndedCallback = () -> {
            this.useEventListener.fireEvent(this, null, null);
        };
    }

    @Override
    public String getConfigIconPath() {
        return "use-events/phone_hangup.png";
    }

    // Class part : "Mode start/stop"
    //========================================================================
    @Override
    public void modeStart(final LCConfigurationI configuration) {
        PhoneControlController.INSTANCE.addCallEndedCallback(callEndedCallback);
    }

    @Override
    public void modeStop(final LCConfigurationI configuration) {
    }
    //========================================================================
}
