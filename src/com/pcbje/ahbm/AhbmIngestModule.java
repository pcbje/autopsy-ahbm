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

import com.pcbje.ahbm.config.AHBMConfig;
import com.pcbje.ahbm.matchable.Matchable;
import com.pcbje.ahbm.matchable.MatchableHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.ingest.IngestModuleAbstractFile;
import org.sleuthkit.autopsy.ingest.IngestModuleInit;
import org.sleuthkit.autopsy.ingest.PipelineContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskData;

/**
 *
 * @author pcbje
 */
public class AhbmIngestModule extends IngestModuleAbstractFile {

    private static AhbmIngestModule def;
    private Sdhash sdhash;
    private SdbfSet sdbfSet;
    private MatchableHandler matchHandler;
    private CaseWrapper caseWrapper;
    private AHBMConfig config;
    private boolean skipKnownGood;
    private boolean againstExisting;

    public static IngestModuleAbstractFile getDefault() {
        if (def == null) {
            def = new AhbmIngestModule();
        }
        return def;
    }
    private long maxFileSize;

    public AhbmIngestModule() {
        caseWrapper = new CaseWrapper();
    }

    @Override
    public ProcessResult process(PipelineContext<IngestModuleAbstractFile> pc, AbstractFile af) {
        if (!af.isFile() || skipFile(af)) {
            return ProcessResult.OK;
        }

        try {
            Matchable probe = new Matchable(af.getName(), af);
            String sdbf = sdhash.generateSdbf(probe);
            sdbfSet.addSdbfToOpenCase(sdbf);
            Collection<Matchable> matchables = sdbfSet.streamMatch(af, sdbf);
            matchHandler.handleStreamMatches(matchables);

            return ProcessResult.OK;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return ProcessResult.ERROR;
        }
    }

    private boolean skipFile(AbstractFile af) {
        return (skipKnownGood && af.getKnown() == TskData.FileKnown.KNOWN)
                || (maxFileSize > 0 && af.getSize() > maxFileSize);
    }

    @Override
    public void init(IngestModuleInit imi) {
        if (sdbfSet == null) {
            sdbfSet = new SdbfSet();
        }

        if (sdhash == null) {
            sdhash = new Sdhash();
        }

        if (matchHandler == null) {
            matchHandler = new MatchableHandler();
        }

        Properties properties = caseWrapper.getProperties();

        skipKnownGood = properties.getProperty("ahbm.skip.known.good").equals("true");
        againstExisting = properties.getProperty("ahbm.against.existing").equals("true");
        maxFileSize = Long.parseLong(properties.getProperty("ahbm.max.file.size")) * (1024 * 1024);

        try {
            sdbfSet.loadDefaultStreamSets(againstExisting);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void complete() {
        try {
            sdbfSet.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void stop() {
        try {
            sdbfSet.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getName() {
        return "AHBM";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getDescription() {
        return "Module for approximate hash based matching using sdhash";
    }

    @Override
    public boolean hasBackgroundJobsRunning() {
        return false;
    }

    @Override
    public boolean hasSimpleConfiguration() {
        return true;
    }

    @Override
    public javax.swing.JPanel getSimpleConfiguration(String context) {
        config = new AHBMConfig(caseWrapper.getProperties());
        return config;
    }

    @Override
    public void saveSimpleConfiguration() {
        caseWrapper.storeProperties(config.getProperties());
    }

    public void setSdhash(Sdhash sdhash) {
        this.sdhash = sdhash;
    }

    void setSdbfSet(SdbfSet sdbfSet) {
        this.sdbfSet = sdbfSet;
    }

    void setMatchHandler(MatchableHandler matchHandler) {
        this.matchHandler = matchHandler;
    }

    void setCaseWrapper(CaseWrapper caseWrapper) {
        this.caseWrapper = caseWrapper;
    }
}
