package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.BinaryExportTask;
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
@XmlRootElement(name = "binaryExport")
@Data
public class XmlBinaryExportTask implements BinaryExportTask {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "statement", required = true)
    @XmlCDATA
    private String statement;

    @XmlElement(name = "fileNameColumnIndex", required = true)
    private int fileNameColumnIndex;

    @XmlElement(name = "dataColumnIndex", required = true)
    private int dataColumnIndex;

    @XmlElement(name = "filePrefix")
    private String filePrefix = "";

    @XmlElement(name = "fileSuffix")
    private String fileSuffix = "";

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = XmlParameter.class)
    private Set<Parameter> parameters = new HashSet<>();
}
