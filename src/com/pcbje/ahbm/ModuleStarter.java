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

import java.util.List;
import org.openide.modules.OnStart;
import org.sleuthkit.autopsy.ingest.IngestManager;
import org.sleuthkit.autopsy.ingest.IngestModuleAbstractFile;

/**
 *
 * @author pcbje
 */
@OnStart
public class ModuleStarter implements Runnable {

    @Override
    public void run() {
        List<IngestModuleAbstractFile> modules = IngestManager.getDefault().enumerateAbstractFileModules();

        boolean started = false;

        for (IngestModuleAbstractFile module : modules) {
            if (module instanceof AhbmIngestModule) {
                started = true;
                break;
            }
        }

        if (!started) {
            modules.add(AhbmIngestModule.getDefault());
        }
    }
}
