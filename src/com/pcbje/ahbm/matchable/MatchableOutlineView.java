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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.sleuthkit.autopsy.corecomponents.DataContentTopComponent;
import org.sleuthkit.autopsy.datamodel.FileNode;
import org.sleuthkit.datamodel.AbstractFile;

/**
 *
 * @author pcbje
 */
public class MatchableOutlineView extends OutlineView {
    
    private final ExplorerManager mgr;

    public MatchableOutlineView(ExplorerManager mgr) {
        super();

        this.mgr = mgr;
        
        getOutline().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                setNode();
            }
        });

        getOutline().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setNode();
            }
        });
    }

    private void setNode() {
        if (mgr.getSelectedNodes().length > 0) {
            Matchable matchable = mgr.getSelectedNodes()[0].getLookup().lookup(Matchable.class);

            if (matchable.getContent() != null) {
                FileNode n = new FileNode((AbstractFile) matchable.getContent(), false);
                Lookup.getDefault().lookup(DataContentTopComponent.class).setNode(n);
            }
        }
    }

    @Override
    public void expandNode(final Node node) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                expand(node);
            }
        });
    }

    private void expand(Node node) {
        super.expandNode(node);
    }
}
