/**
 * This work is made available under the Apache License, Version 2.0.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.pcbje.ahbm.matchable;

import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.writer.GexfEntityWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.openide.util.Exceptions;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class GexfExport {

    private Attribute attUniquePath;
    private Attribute attReferenceSet;
    private Graph graph;
    private Set<String> cache;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public String export(Matchable root) throws IOException {
        Gexf gexf = new GexfImpl();

        Calendar date = Calendar.getInstance();

        gexf.getMetadata()
                .setLastModified(date.getTime())
                .setCreator("Autopsy AHBM 0.1")
                .setDescription("A network of similar files detected by sdhash");
        gexf.setVisualization(true);

        graph = gexf.getGraph();

        graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);

        AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
        graph.getAttributeLists().add(attrList);

        attUniquePath = attrList.createAttribute("1", AttributeType.STRING, "uniquePath").setDefaultValue("");
        attReferenceSet = attrList.createAttribute("2", AttributeType.STRING, "referenceSet").setDefaultValue("");

        cache = new HashSet<>();

        traverseChildren(root);

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        File f = new File(System.getProperty("user.home") + File.separator + "autopsy-ahbm-" + (System.currentTimeMillis() / 1000) + ".gexf");

        Writer out = new FileWriter(f, false);

        try {
            XMLStreamWriter streamWriter = factory.createXMLStreamWriter(out);
            streamWriter.writeStartDocument("UTF-8", "1.0");

            new GexfEntityWriter(streamWriter, gexf);

            streamWriter.writeEndDocument();

            streamWriter.flush();
            streamWriter.close();
        } catch (XMLStreamException e) {
            throw new IOException("XML Exception: " + e.getMessage(), e);
        }

        return f.getAbsolutePath();
    }

    private Node createNode(Matchable root) {
        Node node = graph.createNode(root.getUniqueIdentifier());
        try {
            node.setLabel(root.getFilename()).setSize(20)
                    .getAttributeValues()
                    .addValue(attUniquePath, root.getUniquePath())
                    .addValue(attReferenceSet, root.getReferenceSetName());
        } catch (TskCoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return node;
    }

    private void traverseChildren(Matchable base) {
        cache.add(base.getUniqueIdentifier());

        Node parentNode = createNode(base);
        Node childNode;
        for (Matchable child : base.getChildren()) {
            childNode = createNode(child);
            parentNode.connectTo(childNode);

            if (!cache.contains(child.getUniqueIdentifier())) {
                traverseChildren(child);
            }
        }
    }
}
