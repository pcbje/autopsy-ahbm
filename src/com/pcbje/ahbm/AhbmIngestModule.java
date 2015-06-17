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
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskData;

/**
 *
 * @author pcbje
 */
public class AhbmIngestModule implements FileIngestModule {

    private Sdhash sdhash;
    private SdbfSet sdbfSet;
    private MatchableHandler matchHandler;
    private CaseWrapper caseWrapper;
    private static AhbmJobSettings settings;
    
    public static AhbmJobSettings getSettings() {
        return settings;
    }

    public AhbmIngestModule(AhbmJobSettings settings) {
        caseWrapper = new CaseWrapper(settings);
        this.settings = settings;
    }
    
    public void setSettings(AhbmJobSettings ahbmJobSettings) {
        this.settings = ahbmJobSettings;
    }

    @Override
    public ProcessResult process(AbstractFile af) {
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
        return (settings.isSkipKnownGood() && af.getKnown() == TskData.FileKnown.KNOWN)
                || (settings.getMaxFileSizeInBytes() > 0 && af.getSize() > settings.getMaxFileSizeInBytes());
    }

    @Override
    public void startUp(IngestJobContext ijc) {
        if (sdbfSet == null) {
            sdbfSet = new SdbfSet(settings);
        }

        if (sdhash == null) {
            sdhash = new Sdhash(settings);
        }

        if (matchHandler == null) {
            matchHandler = new MatchableHandler();
        }

       
        try {
            sdbfSet.loadDefaultStreamSets(caseWrapper.getSettings().isAgainstExisting());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void shutDown() {
        try {
            sdbfSet.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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
