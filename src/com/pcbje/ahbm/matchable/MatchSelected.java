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

import com.pcbje.ahbm.AhbmIngestModule;
import com.pcbje.ahbm.SdbfSet;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.sleuthkit.autopsy.corecomponents.DataResultTopComponent;
import org.sleuthkit.datamodel.Content;

@ActionID(
        category = "Tools",
        id = "com.pcbje.ahbm.matchable.MatchSelected")
@ActionRegistration(
        displayName = "#CTL_MatchSelected")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 1900),
    @ActionReference(path = "Shortcuts", name = "D-M")
})
@Messages("CTL_MatchSelected=AHBM selected files")
public final class MatchSelected implements ActionListener {

    private MatchableHandler handler;
    private SdbfSet sdbfSet;

    public MatchSelected() {
        handler = new MatchableHandler();
        sdbfSet = new SdbfSet(AhbmIngestModule.getSettings());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final DataResultTopComponent tc = getActiveTopComponent();

        if (tc == null) {
            return;
        }

        final ExplorerManager mgr = tc.getExplorerManager();

        Content content;

        final Map<String, Matchable> input = new HashMap<>();

        for (Node node : mgr.getSelectedNodes()) {
            content = node.getLookup().lookup(Content.class);

            input.put(Long.toString(content.getId()), new Matchable(content.getName(), content));
        }

        try {
            handler.handleSearchMatches(input.values());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                try {
                    sdbfSet.searchMatching(input);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private DataResultTopComponent getActiveTopComponent() {
        TopComponent.Registry r = TopComponent.getRegistry();
        for (TopComponent s : r.getOpened()) {
            if (s.hasFocus() && s instanceof DataResultTopComponent) {
                return ((DataResultTopComponent) s);

            }
        }

        for (TopComponent s : r.getOpened()) {
            if (s.isShowing() && s instanceof DataResultTopComponent) {
                return ((DataResultTopComponent) s);
            }
        }

        return null;
    }
}
