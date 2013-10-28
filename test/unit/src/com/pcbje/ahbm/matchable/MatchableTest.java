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

import java.awt.EventQueue;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author pcbje
 */
public class MatchableTest {

    @Test
    public void testIsParentFor() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    MatchableViewer matchViewer = new MatchableViewer();

                    Matchable root = matchViewer.getRootNode();

                    Matchable file1 = new Matchable("file-1", null);
                    Matchable file2 = new Matchable("file-2", null);
                    Matchable file3 = new Matchable("file-3", null);
                    Matchable file4 = new Matchable("file-4", null);
                    Matchable file5 = new Matchable("file-5", null);
                    Matchable file6 = new Matchable("file-6", null);

                    root.addChild(file1);
                    root.addChild(file2);

                    file1.addChild(file3);
                    file2.addChild(file4);
                    file2.addChild(file5);
                    file3.addChild(file6);

                    assertEquals(true, file1.isParentFor(file3));
                    assertEquals(false, file3.isParentFor(file1));
                    assertEquals(true, file1.isParentFor(file6));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
}
