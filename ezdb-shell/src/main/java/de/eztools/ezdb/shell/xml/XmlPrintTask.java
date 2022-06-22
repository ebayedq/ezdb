package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.Parameter;
import de.eztools.ezdb.api.model.PrintTask;
import lombok.Data;
import org.eclipse.persistence.oxm.annotations.XmlCDATA;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(propOrder = {})
@XmlRootElement(name = "print")
@Data
public class XmlPrintTask implements PrintTask {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "statement", required = true)
    @XmlCDATA
    private String statement;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = XmlParameter.class)
    private Set<Parameter> parameters = new HashSet<>();
}
