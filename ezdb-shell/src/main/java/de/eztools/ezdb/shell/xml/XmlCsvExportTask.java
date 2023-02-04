package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.CsvExportTask;
import de.eztools.ezdb.api.model.Parameter;
import lombok.Data;
import org.eclipse.persistence.oxm.annotations.XmlCDATA;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(propOrder = {})
@XmlRootElement(name = "csvExport")
@Data
public class XmlCsvExportTask implements CsvExportTask {

    public static final String DEFAULT_FORMAT = "Default";

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "statement", required = true)
    @XmlCDATA
    private String statement;

    @XmlElement(name = "fileName", required = true)
    private String fileName;

    @XmlElement(name = "format")
    private String format = DEFAULT_FORMAT;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = XmlParameter.class)
    private Set<Parameter> parameters = new HashSet<>();
}
