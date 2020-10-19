package eu.jsparrow.standalone.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Ludwig Werzowa
 */
@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class Profile {

    @XmlElement(name = "setting")
    private List<Setting> settings;

    public List<Setting> getSettings() {
        return settings;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "settings=" + settings +
                '}';
    }
}

