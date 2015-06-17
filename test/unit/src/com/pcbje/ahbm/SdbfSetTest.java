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
package com.pcbje.ahbm;

import com.pcbje.ahbm.matchable.Matchable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class SdbfSetTest {

    private String dummySdbf = "sdbf-dd:03:15:dummy.txt:3263:sha1:256:5:7ff:192:1:16384:37:AAKAJAAEBQIACCAAAAAAEIGEABAgIkAARwDBiAAACAAAAcAAKEQKABEACBEhAAAkTCABlAADsAQIAAAJAICAWWACBSAAAQAAABBQIIKAAAIAAMCCEAAFAEYACMABBABAQACAAAQEEACQCEQAIBCQAESgAEABEBBAQgAAAgABQlNBkACQBoAEAEAAQAlAAABIgQAIFIAEARQAACAjQAAAAEAADAAAUAgAABKAACARAoCQAAAQAQAAAQBkAAEAQUIEAAAAIIAAAAgAgQRAEiIgAgAEACBkgEASWEgCBCAAAEACAIBCEAIAAgHgAASAAgAAxABGMABAQCBAQAUCJgEEAA==\n";
    private SdbfSet sdbfSet;
    private CaseWrapper caseWrapper;

    @Before
    public void setup() {
        sdbfSet = new SdbfSet(new AhbmJobSettings());
        caseWrapper = mock(CaseWrapper.class);
        SdbfSet.setCaseWrapper(caseWrapper);
        Matchable.setCaseWrapper(caseWrapper);
    }

    @Test
    public void testAddToOpenCase() throws IOException {
        File target = File.createTempFile("sdbf-test", "open");
        target.deleteOnExit();

        when(caseWrapper.getFileInModuleDir(any(String.class))).thenReturn(target);

        sdbfSet.addSdbfToOpenCase(dummySdbf);

        assertEquals(true, target.exists());

        sdbfSet.close();

        assertNotSame(0, target.length());
    }

    @Test
    public void testStreamMatching() throws IOException, TskCoreException {
        List<File> streamSets = new ArrayList<File>();
        streamSets.add(new File(getClass().getClassLoader().getResource("loremlorem.sdbf").getPath()));
        sdbfSet.setStreamSets(streamSets);

        String probe = getProbeSdbf();

        Content content = mock(Content.class);
        when(content.getName()).thenReturn("loremipsum");

        Collection<Matchable> results = sdbfSet.streamMatch(content, probe);

        Matchable actual = new Matchable("loremipsum", null);
        actual.addChild(new Matchable("loremipsum", null, "100"));
        actual.addChild(new Matchable("ipsumlorem", null, "087"));

        Matchable match = results.iterator().next();

        assertEquals(actual.getChildren().size(), match.getChildren().size());
        assertEquals("087", match.getChildren().get(1).getParentSimilarity());
    }

    @Test
    public void testStreamMatchingWithMatchInOpenCase() throws IOException, TskCoreException {
        List<File> streamSets = new ArrayList<File>();
        streamSets.add(new File(getClass().getClassLoader().getResource("loremipsum-file-in-case.sdbf").getPath()));
        sdbfSet.setStreamSets(streamSets);

        String probe = getProbeSdbf();

        Content content = mock(Content.class);
        when(content.getName()).thenReturn("loremipsum");

        AbstractFile matchedContent = mock(AbstractFile.class);
        String matchedContentFilename = "i am filename";
        when(matchedContent.getName()).thenReturn(matchedContentFilename);

        when(caseWrapper.getContentById(any(Long.class))).thenReturn(matchedContent);

        Collection<Matchable> results = sdbfSet.streamMatch(content, probe);

        verify(caseWrapper).getContentById(any(Long.class));

        Matchable match = results.iterator().next();

        assertNotSame(null, match.getChildren().get(0).getContent());
        assertEquals(matchedContentFilename, match.getChildren().get(0).getFilename());
    }

    @Test
    public void testLoadStreamSet() throws IOException {
        File streamSetFile = File.createTempFile("streamset", "test");
        streamSetFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(streamSetFile));
        writer.write("stream-set-1.sdbf\n");
        writer.write("stream-set-2.sdbf\n");
        writer.write("stream-set-3.sdbf\n");
        writer.close();

        when(caseWrapper.getFileInModuleDir(any(String.class))).thenReturn(streamSetFile);

        sdbfSet.loadStreamSets(streamSetFile.getName(), false);

        assertEquals(3, sdbfSet.getStreamSets().size());
    }

    @Test
    public void testLoadStreamSetIncludingExisting() throws IOException {
        File streamSetFile = File.createTempFile("streamset", "test");
        streamSetFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(streamSetFile));
        writer.write("stream-set-1.sdbf\n");
        writer.write("stream-set-2.sdbf\n");
        writer.write("stream-set-3.sdbf\n");
        writer.close();

        when(caseWrapper.getFileInModuleDir(any(String.class))).thenReturn(streamSetFile);

        sdbfSet.loadStreamSets(streamSetFile.getName(), true);

        assertEquals(4, sdbfSet.getStreamSets().size());
    }

    @Test
    public void testLoadStreamSetWithCommentsAndEmptyLines() throws IOException {
        File streamSetFile = File.createTempFile("streamset", "test");
        streamSetFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(streamSetFile));
        writer.write("stream-set-1.sdbf\n");
        writer.write("\n");
        writer.write("# I am comment\n");
        writer.write("stream-set-3.sdbf\n");
        writer.write("// I am another comment\n");
        writer.write("stream-set-4.sdbf\n");
        writer.close();

        when(caseWrapper.getFileInModuleDir(any(String.class))).thenReturn(streamSetFile);
        sdbfSet.setCaseWrapper(caseWrapper);
        sdbfSet.loadStreamSets(streamSetFile.getName(), false);

        assertEquals(3, sdbfSet.getStreamSets().size());
    }

    @Test
    public void testAdHocMatch() throws IOException, TskCoreException {
        List<File> streamSets = new ArrayList<File>();
        streamSets.add(new File(getClass().getClassLoader().getResource("loremipsum.sdbf").getPath()));
        sdbfSet.setStreamSets(streamSets);

        Content content = mock(Content.class);
        when(content.getName()).thenReturn("loremipsum");

        Sdhash sdhash = mock(Sdhash.class);
        when(sdhash.generateSdbf(any(Matchable.class))).thenReturn(getProbeSdbf());
        when(sdhash.compareSets(any(File.class), any(File.class))).thenCallRealMethod();
        sdbfSet.setSdhash(sdhash);

        Matchable.setSdbfSet(sdbfSet);

        Matchable match = new Matchable("loremipsum", content);
        Map<String, Matchable> input = new HashMap<String, Matchable>();
        input.put("loremipsum", match);

        when(caseWrapper.getFileInModuleDir(any(String.class))).thenReturn(new File(getClass().getClassLoader().getResource("dummy.sdbf").getPath()));

        sdbfSet.searchMatching(input);

        assertEquals(1, match.getChildren().size());
    }

    private String getProbeSdbf() throws IOException {
        File probeFile = new File(getClass().getClassLoader().getResource("loremipsum.sdbf").getPath());
        BufferedReader probeReader = new BufferedReader(new FileReader(probeFile));
        String probe = probeReader.readLine();
        probeReader.close();
        return probe;
    }
}
