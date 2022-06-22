package de.eztools.ezdb.shell;

import de.eztools.ezdb.shell.xml.XmlCopyTask;
import de.eztools.ezdb.shell.xml.XmlParameter;
import de.eztools.ezdb.shell.xml.XmlSuite;
import de.eztools.ezdb.shell.xml.XmlUpdateTask;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@Ignore
public class SerializationTest {

    private static Marshaller marshaller;
    private static JAXBContext jaxbContext;

    @BeforeClass
    public static void setUp() throws Exception {
        jaxbContext = JAXBContextFactory.createContext(new Class[]{XmlSuite.class}, null);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://eztools.de/ezdb/1.0 ezdb-1.0.xsd");
    }

    @Test
    public void testMarshalling() throws Exception {
        XmlSuite suite = createJaxbSuite();

        StringWriter writer = new StringWriter();
        marshaller.marshal(suite, writer);
        String marshalled = writer.toString();

        System.out.println(marshalled);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        XmlSuite unmarshalled = (XmlSuite) unmarshaller.unmarshal(new StringReader(marshalled));

        assertEquals(suite, unmarshalled);
    }

    private XmlSuite createJaxbSuite() {
        XmlParameter parameter = new XmlParameter();
        parameter.setName("klasse");
        parameter.setType(String.class);

        XmlCopyTask copy = new XmlCopyTask();
        copy.setName("test");
        copy.setSelectStatement("select 1 from table where klasse =${klasse}");
        copy.setInsertStatement("insert into table");
        copy.setParameters(Collections.singleton(parameter));

        XmlUpdateTask update = new XmlUpdateTask();
        update.setName("test2");
        update.setStatement("select 1");
        update.setParameters(Collections.emptySet());

        XmlSuite suite = new XmlSuite();
        suite.setName("suite name");
        suite.setTasks(Arrays.asList(copy, update));

        return suite;
    }

    @Test
    public void createSchema() throws Exception {
        jaxbContext.generateSchema(new MySchemaOutputResolver(XmlSuite.class));
    }

    private static class MySchemaOutputResolver extends SchemaOutputResolver {

        private Class<?> type;

        public MySchemaOutputResolver(Class<?> type) {
            this.type = type;
        }

        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
            File file = new File(type.getSimpleName() + ".xsd");
            StreamResult result = new StreamResult(file);
            result.setSystemId(file.toURI().toURL().toString());
            return result;
        }

    }
}
