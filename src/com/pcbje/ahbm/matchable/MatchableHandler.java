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

import com.pcbje.ahbm.CaseWrapper;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class MatchableHandler {

    private CaseWrapper caseWrapper;
    private MatchableViewer drawer;

    public MatchableHandler() {
        caseWrapper = new CaseWrapper();
        drawer = (MatchableViewer) WindowManager.getDefault().findTopComponent("MatchViewer");
    }

    public void handleStreamMatches(final Collection<Matchable> matchables) throws InterruptedException, InvocationTargetException {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Iterator<Matchable> itr = matchables.iterator();

                Matchable probe;

                while (itr.hasNext()) {
                    probe = itr.next();

                    if (probe.hasChildren()) {
                        drawer.addNode(probe);

                        if (!drawer.isOpened()) {
                            drawer.open();

                            drawer.requestActive();
                        }

                        try {
                            caseWrapper.addMatchNotice(probe);
                        } catch (TskCoreException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }

    public void handleSearchMatches(Collection<Matchable> matchables) throws TskCoreException {
        Iterator<Matchable> itr = matchables.iterator();

        Matchable probe;

        while (itr.hasNext()) {
            probe = itr.next();

            drawer.addNode(probe);
        }

        drawer.open();
        drawer.requestFocus();
        drawer.requestActive();
    }
}
