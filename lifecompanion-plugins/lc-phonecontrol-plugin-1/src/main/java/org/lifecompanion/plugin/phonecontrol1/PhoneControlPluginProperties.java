package org.lifecompanion.plugin.phonecontrol1;

import javafx.beans.property.*;
import org.jdom2.Element;
import org.lifecompanion.framework.commons.fx.io.XMLObjectSerializer;
import org.lifecompanion.framework.commons.utils.lang.StringUtils;
import org.lifecompanion.model.api.configurationcomponent.LCConfigurationI;
import org.lifecompanion.model.api.io.IOContextI;
import org.lifecompanion.model.impl.exception.LCException;
import org.lifecompanion.model.impl.plugin.AbstractPluginConfigProperties;

import java.util.Map;

/**
 * @author Etudiants IUT Vannes : GUERNY Baptiste, HASCOËT Anthony,
 *         Le CHANU Simon, PAVOINE Oscar
 */
public class PhoneControlPluginProperties extends AbstractPluginConfigProperties {

    public static final String NODE_PHONECONTROL_PLUGIN = "PhoneControl1PluginInformations";

    // CONFIG VARIABLES
    private final StringProperty deviceSerialNumber;
    private final BooleanProperty speakerOn;
    private final IntegerProperty durationInternal;

    protected PhoneControlPluginProperties(ObjectProperty<LCConfigurationI> parentConfiguration) {
        super(parentConfiguration);
        this.deviceSerialNumber = new SimpleStringProperty();
        this.speakerOn = new SimpleBooleanProperty();
        this.durationInternal = new SimpleIntegerProperty();
    }

    public StringProperty deviceProperty() {
        return this.deviceSerialNumber;
    }

    public BooleanProperty speakerOnProperty() {
        return this.speakerOn;
    }

    public IntegerProperty durationInternalProperty() {
        return this.durationInternal;
    }

    @Override
    public String toString() {
        return "PhoneControl1PluginProperties{" +
                "deviceSerialNumber=" + this.deviceSerialNumber +
                ", speakerOn=" + this.speakerOn +
                ", labelDurationIntervalPicker=" + this.durationInternal +
                '}';
    }

    /**
     * Check if a serial number is set
     * @return true if the configuration is set
     */
    public boolean isPhoneControlConfigurationSet() {
        return StringUtils.isNotBlank(this.deviceSerialNumber.get());
    }

    // SERIALIZE / DESERIALIZE
    //========================================================================
    @Override
    public Element serialize(final IOContextI context) {
        Element element = new Element(NODE_PHONECONTROL_PLUGIN);
        XMLObjectSerializer.serializeInto(PhoneControlPluginProperties.class, this, element);
        return element;
    }

    @Override
    public void deserialize(final Element node, final IOContextI context) throws LCException {
        XMLObjectSerializer.deserializeInto(PhoneControlPluginProperties.class, this, node);
    }

    @Override
    public void serializeUseInformation(Map<String, Element> elements) {
    }

    @Override
    public void deserializeUseInformation(Map<String, Element> elements) throws LCException {
    }

    //========================================================================
}
