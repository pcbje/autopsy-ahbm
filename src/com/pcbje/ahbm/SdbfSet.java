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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class SdbfSet {

    public static final String DEFAULT_OPEN_CASE_SET = "open_case.sdbf";
    public static final String DEFAULT_STREAM_SET = "streamset.txt";
    private static CaseWrapper caseWrapper;
    private File openCaseSet;
    private BufferedWriter openCaseSdbfWriter;
    private List<File> streamSets;
    private Sdhash sdhash;

    static {
        caseWrapper = new CaseWrapper();
    }
    private File openCaseSdbfCopy;

    public SdbfSet() {
        sdhash = new Sdhash();
    }

    public static void setCaseWrapper(CaseWrapper _caseWrapper) {
        caseWrapper = _caseWrapper;
    }

    public void addSdbfToOpenCase(String sdbf) throws IOException {
        if (openCaseSet == null) {
            openCaseSet = caseWrapper.getFileInModuleDir(DEFAULT_OPEN_CASE_SET);
            openCaseSdbfWriter = new BufferedWriter(new FileWriter(openCaseSet, true));
        }

        openCaseSdbfWriter.write(sdbf);
    }

    public void close() throws IOException {
        if (openCaseSdbfWriter != null) {
            openCaseSdbfWriter.close();
            openCaseSet = null;
        }

        if (openCaseSdbfCopy != null) {
            openCaseSdbfCopy.delete();
            openCaseSdbfCopy = null;
        }
    }

    public void setStreamSets(List<File> streamSets) {
        this.streamSets = streamSets;
    }

    public void setSdhash(Sdhash sdhash) {
        this.sdhash = sdhash;
    }

    public Collection<Matchable> streamMatch(Content content, String sdbf) throws IOException, TskCoreException {
        File sdbfFile = writeStringToFile(sdbf);

        String results;

        Matchable probe = new Matchable(content.getName(), content);

        Map<String, Matchable> input = new HashMap<>();

        if (content.getId() > 0) {
            input.put(Long.toString(content.getId()), probe);
        } else {
            input.put(content.getName(), probe);
        }

        for (File streamSet : streamSets) {
            results = sdhash.compareSets(sdbfFile, streamSet);

            Matchable.fromSdhashResults(input, results);
        }

        return input.values();
    }

    private File writeStringToFile(String sdbf) throws IOException {
        File file = File.createTempFile("stream", "probe");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.write(sdbf);

        writer.close();

        return file;
    }

    public void loadDefaultStreamSets(boolean againstExisting) throws FileNotFoundException, IOException {
        loadStreamSets(DEFAULT_STREAM_SET, againstExisting);
    }

    public void loadStreamSets(String source, boolean againstExisting) throws FileNotFoundException, IOException {
        File streamSetsFile = caseWrapper.getFileInModuleDir(source);

        if (!streamSetsFile.exists()) {
            streamSetsFile.createNewFile();
        }

        streamSets = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(streamSetsFile));

        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().length() > 0 && !line.startsWith("#") && !line.startsWith("//")) {
                streamSets.add(new File(line));
            }
        }

        openCaseSdbfCopy = createCopyOfOpenCaseSDBF();

        if (againstExisting && openCaseSdbfCopy != null) {
            streamSets.add(openCaseSdbfCopy);
        }

        reader.close();
    }

    public List<File> getStreamSets() {
        return streamSets;
    }

    public void searchMatching(final Map<String, Matchable> probes) throws IOException, TskCoreException {
        if (streamSets == null) {
            loadDefaultStreamSets(true);
        }

        StringBuilder signatures = new StringBuilder();

        for (Matchable probe : probes.values()) {
            if (probe.getContent() != null) {
                signatures.append(sdhash.generateSdbf(probe));
                probe.setExpanded(true);
                probe.setCompleted(false);
            }
        }

        final File probeFile = writeStringToFile(signatures.toString());

        final StringBuilder results = new StringBuilder();

        for (File streamSet : streamSets) {
            results.append(sdhash.compareSets(probeFile, streamSet));
        }

        Matchable.fromSdhashResults(probes, results.toString());

        for (Matchable probe : probes.values()) {
            if (probe.getContent() != null) {
                probe.setCompleted(true);
            }
        }
    }

    public void setOpenCaseSet(File openCaseSet) {
        this.openCaseSet = openCaseSet;
    }

    public void importSdbfSet(File sourceFile) throws IOException {
        sdhash.validateSdbf(sourceFile);

        BufferedWriter writer = new BufferedWriter(new FileWriter(caseWrapper.getFileInModuleDir(DEFAULT_STREAM_SET), true));
        writer.write(sourceFile.getAbsolutePath());
        writer.write("\n");
        writer.close();
    }

    private File createCopyOfOpenCaseSDBF() throws IOException {
        File original = caseWrapper.getFileInModuleDir(DEFAULT_OPEN_CASE_SET);

        if (!original.exists()) {
            return null;
        }

        File copy = File.createTempFile(DEFAULT_OPEN_CASE_SET, "copy");
        copyFile(original, copy);
        return copy;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
