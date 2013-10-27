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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.openide.util.Exceptions;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class Sdhash {

    private CaseWrapper caseWrapper;
    private Properties props;
    private int bufferSize;

    public Sdhash() {
        caseWrapper = new CaseWrapper();
    }

    public void setCaseWrapper(CaseWrapper caseWrapper) {
        this.caseWrapper = caseWrapper;
    }

    public String generateSdbf(Matchable probe) throws IOException, TskCoreException {
        if (props == null) {
            props = caseWrapper.getProperties();

            bufferSize = Integer.parseInt(props.getProperty("read.buffer.size"));
        }

        File tmp = File.createTempFile("ahbm", "probe");

        FileOutputStream out = new FileOutputStream(tmp);

        try {
            caseWrapper.readFile(out, bufferSize, probe.getContent());

            out.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        String[] cmd = new String[]{
            "sdhash",
            tmp.getAbsolutePath()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        InputStream in = process.getInputStream();

        int read;
        byte[] buffer = new byte[1000];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((read = in.read(buffer)) > 0) {
            baos.write(buffer, 0, read);
        }

        in.close();

        tmp.delete();

        String sdbf = new String(baos.toByteArray());

        String replace = new StringBuilder().append(":").append(tmp.getAbsolutePath().length()).append(":").append(tmp.getAbsolutePath()).append(":").toString();
        String with = new StringBuilder().append(":").append(Long.toString(probe.getContent().getId()).length()).append(":").append(Long.toString(probe.getContent().getId())).append(":").toString();

        return sdbf.replace(replace, with);
    }

    String compareSets(File probe, File reference) throws IOException {
        String[] cmd = new String[]{
            "sdhash",
            "-c",
            probe.getAbsolutePath(),
            reference.getAbsolutePath()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader result = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder scores = new StringBuilder();
        String line;
        while ((line = result.readLine()) != null) {
            scores.append(line);
            scores.append("|").append(reference.getAbsolutePath());
            scores.append("\n");
        }
        return scores.toString();
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void validateSdbf(File sourceFile) throws IOException {
        if (!sourceFile.exists()) {
            throw new RuntimeException(String.format("Could not find file: %s", sourceFile.getAbsolutePath()));
        }

        String[] cmd = new String[]{
            "sdhash",
            "--validate",
            sourceFile.getAbsolutePath()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);

        Process process = processBuilder.start();
        BufferedReader result = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder error = new StringBuilder();
        String line;
        while ((line = result.readLine()) != null) {
            error.append(line);
        }

        if (error.toString().trim().length() > 0) {
            throw new RuntimeException(error.toString());
        }
    }

    public void validateSdbfs(String[] paths) throws IOException {
        for (String path : paths) {
            if (path.trim().length() > 0 && !path.startsWith("#")) {
                validateSdbf(new File(path));
            }
        }
    }
}
