package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.ImportTask;
import de.eztools.ezdb.api.model.Parameter;
import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(propOrder = {})
@XmlRootElement(name = "import")
@Data
public class XmlImportTask implements ImportTask {

    public static final String DEFAULT_FORMAT = "Default";

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "tableName", required = true)
    private String tableName;

    @XmlElement(name = "fileName", required = true)
    private String fileName;

    @XmlElement(name = "format")
    private String format = DEFAULT_FORMAT;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = XmlParameter.class)
    private Set<Parameter> parameters = new HashSet<>();
}
