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
import com.pcbje.ahbm.matchable.MatchableHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.sleuthkit.autopsy.ingest.IngestModuleInit;
import org.sleuthkit.autopsy.ingest.PipelineContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 *
 * @author pcbje
 */
public class AhbmIngestModuleTest {

    private String dummySdbf = "sdbf-dd:03:15:dummy.txt.0000M:3263:sha1:256:5:7ff:192:1:16384:37:AAKAJAAEBQIACCAAAAAAEIGEABAgIkAARwDBiAAACAAAAcAAKEQKABEACBEhAAAkTCABlAADsAQIAAAJAICAWWACBSAAAQAAABBQIIKAAAIAAMCCEAAFAEYACMABBABAQACAAAQEEACQCEQAIBCQAESgAEABEBBAQgAAAgABQlNBkACQBoAEAEAAQAlAAABIgQAIFIAEARQAACAjQAAAAEAADAAAUAgAABKAACARAoCQAAAQAQAAAQBkAAEAQUIEAAAAIIAAAAgAgQRAEiIgAgAEACBkgEASWEgCBCAAAEACAIBCEAIAAgHgAASAAgAAxABGMABAQCBAQAUCJgEEAA==\n";
    private AhbmIngestModule ahbmIngestModule;
    private PipelineContext pipelineContext;
    private Sdhash sdhash;
    private SdbfSet sdbfSet;
    private MatchableHandler matchHandler;
    private Properties properties;

    @Before
    public void setup() throws IOException, TskCoreException {
        ahbmIngestModule = new AhbmIngestModule();
        pipelineContext = mock(PipelineContext.class);

        sdhash = mock(Sdhash.class);
        when(sdhash.generateSdbf(any(Matchable.class))).thenReturn(dummySdbf);
        ahbmIngestModule.setSdhash(sdhash);

        sdbfSet = mock(SdbfSet.class);
        ahbmIngestModule.setSdbfSet(sdbfSet);

        matchHandler = mock(MatchableHandler.class);
        ahbmIngestModule.setMatchHandler(matchHandler);

        properties = new Properties();
        properties.setProperty("ahbm.skip.known.good", "true");
        properties.setProperty("ahbm.against.existing", "false");
        properties.setProperty("ahbm.against.existing", "false");
        properties.setProperty("ahbm.max.file.size", "0");
    }

    @Test
    public void testSdbfIsGenerated() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdhash).generateSdbf(any(Matchable.class));
    }

    @Test
    public void testSdbfIsAddedToOpenCaseSet() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdbfSet).addSdbfToOpenCase(any(String.class));
    }

    @Test
    public void testSdbfIsStreamMatched() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdbfSet).streamMatch(any(Content.class), any(String.class));
    }

    @Test
    public void testStreamMatchesAreHandeled() throws Exception {
        AbstractFile abstractFile = mock(AbstractFile.class);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(matchHandler).handleStreamMatches(any(Collection.class));
    }

    @Test
    public void testOpenCaseSdbfFileIsClosedOnComplete() throws IOException {
        ahbmIngestModule.complete();
        verify(sdbfSet).close();
    }

    @Test
    public void testOpenCaseSdbfFileIsClosedOnStop() throws IOException {
        ahbmIngestModule.stop();
        verify(sdbfSet).close();
    }

    @Test(expected = ExceptionInInitializerError.class)
    public void testStreamSetLoadedOnInit() throws IOException {
        IngestModuleInit ingestModuleInit = mock(IngestModuleInit.class);

        ahbmIngestModule.init(ingestModuleInit);
    }

    @Test
    public void testKnownGoodIsSkippedWhenConfigIsTrue() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);

        when(abstractFile.getKnown()).thenReturn(TskData.FileKnown.KNOWN);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        CaseWrapper caseWrapper = mock(CaseWrapper.class);

        when(caseWrapper.getProperties()).thenReturn(properties);

        ahbmIngestModule.setCaseWrapper(caseWrapper);

        ahbmIngestModule.init(null);
        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdhash, never()).generateSdbf(any(Matchable.class));
    }

    @Test
    public void testUnknownIsNotSkippedWhenConfigIsTrue() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);

        when(abstractFile.getKnown()).thenReturn(TskData.FileKnown.UKNOWN);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        CaseWrapper caseWrapper = mock(CaseWrapper.class);

        properties.setProperty("ahbm.skip.known.good", "true");
        properties.setProperty("ahbm.against.existing", "false");
        when(caseWrapper.getProperties()).thenReturn(properties);

        ahbmIngestModule.setCaseWrapper(caseWrapper);

        ahbmIngestModule.init(null);
        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdhash).generateSdbf(any(Matchable.class));
    }

    @Test
    public void testKnownGoodIsNotSkippedWhenConfigIsFalse() throws IOException, TskCoreException {
        AbstractFile abstractFile = mock(AbstractFile.class);

        when(abstractFile.getKnown()).thenReturn(TskData.FileKnown.KNOWN);
        when(abstractFile.isFile()).thenReturn(Boolean.TRUE);

        CaseWrapper caseWrapper = mock(CaseWrapper.class);

        properties.setProperty("ahbm.skip.known.good", "false");
        properties.setProperty("ahbm.against.existing", "false");
        when(caseWrapper.getProperties()).thenReturn(properties);

        ahbmIngestModule.setCaseWrapper(caseWrapper);

        ahbmIngestModule.init(null);
        ahbmIngestModule.process(pipelineContext, abstractFile);

        verify(sdhash).generateSdbf(any(Matchable.class));
    }
}
