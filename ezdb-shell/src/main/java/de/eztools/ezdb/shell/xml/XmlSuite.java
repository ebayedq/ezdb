package de.eztools.ezdb.shell.xml;

import de.eztools.ezdb.api.model.Suite;
import de.eztools.ezdb.api.model.Task;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(propOrder = {})
@XmlRootElement(name = "suite")
@Data
public class XmlSuite implements Suite {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElementWrapper(name = "tasks")
    @XmlElements({@XmlElement(name = "copy", type = XmlCopyTask.class),
            @XmlElement(name = "update", type = XmlUpdateTask.class),
            @XmlElement(name = "print", type = XmlPrintTask.class),
            @XmlElement(name = "importCsv", type = XmlImportTask.class),
            @XmlElement(name = "exportBinary", type = XmlBinaryExportTask.class),
            @XmlElement(name = "exportCsv", type = XmlCsvExportTask.class)})
    private List<Task> tasks = new ArrayList<>();

}
