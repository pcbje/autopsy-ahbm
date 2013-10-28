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
public class MatchableViewerTest {

    @Test
    public void testGetExistingNode() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    MatchableViewer matchViewer = new MatchableViewer();

                    Matchable root = matchViewer.getRootNode();

                    Matchable[] files = new Matchable[6];

                    files[0] = new Matchable("file-1", null);
                    files[1] = new Matchable("file-2", null);
                    files[2] = new Matchable("file-3", null);
                    files[3] = new Matchable("file-4", null);
                    files[4] = new Matchable("file-5", null);
                    files[5] = new Matchable("file-6", null);

                    root.addChild(files[0]);
                    root.addChild(files[1]);

                    files[0].addChild(files[2]);
                    files[1].addChild(files[3]);
                    files[1].addChild(files[4]);
                    files[2].addChild(files[5]);

                    for (int i = 0; i < 6; i++) {
                        assertEquals("file-" + (i + 1), matchViewer.getExistingNode(files[i]).getFilename());
                    }

                    assertEquals(null, matchViewer.getExistingNode(new Matchable("not-added", null)));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
}
