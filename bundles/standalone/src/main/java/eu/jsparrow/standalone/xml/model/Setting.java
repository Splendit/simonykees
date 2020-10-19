package eu.jsparrow.standalone.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ludwig Werzowa
 */
@XmlRootElement(name = "setting")
@XmlAccessorType(XmlAccessType.FIELD)
public class Setting {

    @XmlAttribute
    String id;
    @XmlAttribute
    String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Setting{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
