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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class MatchableChildFactory extends ChildFactory<Matchable> implements PropertyChangeListener {

    private final Matchable key;

    public MatchableChildFactory(Matchable key) {
        this.key = key;
        
        if (key != null) {
            key.addPropertyChangeListener(this);
        }
    }

    @Override
    public boolean createKeys(final List<Matchable> toPopulate) {
        key.findChildren();

        List<Matchable> children = key.getChildren();

        for (int i = 0; i < children.size(); i++) {
            try {
                if (!children.get(i).isParentFor(key)) {
                    toPopulate.add(children.get(i));
                }
            } catch (TskCoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return true;
    }

    @Override
    public Node createNodeForKey(Matchable matchable) {
        MatchableNode result = new MatchableNode(
                Children.create(new MatchableChildFactory(matchable), true),
                Lookups.singleton(matchable));

        matchable.addPropertyChangeListener(result);

        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Matchable.ADD_CHILD.equals(evt.getPropertyName())) {
            refresh(true);
        }
    }
}
