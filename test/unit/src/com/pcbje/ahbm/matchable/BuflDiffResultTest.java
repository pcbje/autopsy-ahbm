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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pcbje
 */
public class BuflDiffResultTest {
    @Test
    public void testSizeStr() {
        assertEquals("1020B", BuflDiffResult.getSizeStr(1020L));
        assertEquals("16,3KB", BuflDiffResult.getSizeStr(1024*16L + 310));
        assertEquals("1,7MB", BuflDiffResult.getSizeStr(1024*1024L + (1024 * 700)));
        assertEquals("2,1GB", BuflDiffResult.getSizeStr(1024*1024*1024*2L + (1024*1024*140)));
    }
}
