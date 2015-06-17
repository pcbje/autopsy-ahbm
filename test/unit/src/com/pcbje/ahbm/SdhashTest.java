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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sleuthkit.datamodel.Content;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class SdhashTest {

    private CaseWrapper caseWrapper;
    private Sdhash sdhash;
    private Answer<Void> readAnswer;
    private byte[] buffer;

    @Before
    public void setup() {
        sdhash = new Sdhash(new AhbmJobSettings());

        caseWrapper = mock(CaseWrapper.class);

        sdhash.setCaseWrapper(caseWrapper);

        readAnswer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                OutputStream baos = (OutputStream) invocation.getArguments()[0];
                baos.write(buffer);
                return null;
            }
        };
    }

    @Test
    public void testGenerateSdbf() throws IOException, TskCoreException {
        String actual = "sdbf:03:1:0:3271:sha1:256:5:7ff:160:1:53:AAKAJAAEBQIACCAAAAAAEIGEABAgIkAARwDBiAAACAAAAMAAKAQKABAACBEhAAAkTCABlAADsAQIAAAJAICAWWACASAAAQAAABBQAIKAAAIACMCCEAAFAEYACMABBABAQACAAAQEEACQCEAAIBCQAESAAEABERBAQgAAAgAAQlNBkACAAoAEAEAAQAlAAABIgQAIFIAEARQAACAjQAAAAEAADAAAUAAAABKAACARAoCQAAAQAQAAAQBkAAEAQUIEAAAAIAAAAAgAgQRAEiIgAgAEACJkgEASWEgCBCAAAEACAIBCEAIAAgHgAASAAgAAxABGMABAQCBAQAUAJgEAAA==\r\n";

        File file = new File(getClass().getClassLoader().getResource("dummy.txt").getPath());

        AhbmJobSettings settings = new AhbmJobSettings(false, false, (int) file.length(), (int) file.length());
       
        when(caseWrapper.getSettings()).thenReturn(settings);

        sdhash.setBufferSize((int) file.length());

        FileInputStream fis = new FileInputStream(file);
        buffer = new byte[(int) file.length()];
        fis.read(buffer);

        Content content = mock(Content.class);
        when(content.getName()).thenReturn(file.getName());
        when(content.getSize()).thenReturn((long) buffer.length);
        doAnswer(readAnswer).when(caseWrapper).readFile(any(OutputStream.class), any(Integer.class), any(Content.class));

        Matchable matchable = mock(Matchable.class);
        when(matchable.getContent()).thenReturn(content);

        String generated = sdhash.generateSdbf(matchable);

        assertEquals(actual, generated);
    }

    @Test
    public void testCompareSets() throws IOException {
        File probe = new File(getClass().getClassLoader().getResource("loremipsum.sdbf").getPath());
        File reference = new File(getClass().getClassLoader().getResource("ipsumlorem.sdbf").getPath());

        String actual = "loremipsum|ipsumlorem|087|";

        String result = sdhash.compareSets(probe, reference);

        assertEquals(actual, result.substring(0, actual.length()));
    }

    @Test
    public void testVerifyValidSdbf() throws IOException {
        File probe = new File(getClass().getClassLoader().getResource("loremipsum.sdbf").getPath());

        sdhash.validateSdbf(probe);
    }

    @Test(expected = RuntimeException.class)
    public void testVerifyInvalidSdbfThrowsException() throws IOException {
        File probe = new File(getClass().getClassLoader().getResource("ipsumlorem.txt").getPath());

        sdhash.validateSdbf(probe);
    }
}
