package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.Parameter;
import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
@Data
public class XmlParameter implements Parameter {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "type", required = true)
    private Class<?> type;
}
