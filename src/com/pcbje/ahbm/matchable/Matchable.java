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
import com.pcbje.ahbm.SdbfSet;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class Matchable {

    public static final String NEW_STATUS = "NEW_STATUS";
    public static final String ADD_CHILD = "ADD_CHILD";
    public static final String CHILDREN_TYPE = "CHILDREN_TYPE";
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private List<Matchable> children;
    private Matchable parent;
    private String filename;
    private Content content;
    private String similarityToParent;
    private static CaseWrapper caseWrapper;
    private static SdbfSet sdbfSet;
    private File referenceSet;
    private boolean completed;
    private boolean expanded;

    static {
        caseWrapper = new CaseWrapper();
        sdbfSet = new SdbfSet();
    }

    public Matchable(String filename, Content content) {
        children = new ArrayList();
        this.filename = filename;
        this.content = content;
        this.similarityToParent = null;
    }

    public Matchable(String filename, Content content, String similarityToParent) {
        children = new ArrayList();
        this.filename = filename;
        this.content = content;
        this.similarityToParent = similarityToParent;
    }

    public static void setSdbfSet(SdbfSet _sdbfSet) {
        sdbfSet = _sdbfSet;
    }

    public static void setCaseWrapper(CaseWrapper _caseWrapper) {
        caseWrapper = _caseWrapper;
    }

    public String getFilename() {
        return filename;
    }

    public String getUniquePath() throws TskCoreException {
        if (content != null) {
            return content.getUniquePath();
        }

        return "";
    }

    public String getReferenceSetName() {
        if (referenceSet != null) {
            return referenceSet.getName();
        }

        return "";
    }

    public String getParentSimilarity() {
        return similarityToParent;
    }

    public String getStatus() {
        if (!expanded) {
            return null;
        } else if (expanded && !completed) {
            return "Matching...";
        } else {
            int norm = children.size() - 1;
            return String.format("%d match%s", norm, norm != 1 ? "es" : "");
        }
    }

    public String getParentName() throws TskCoreException {
        if (parent != null) {
            if (parent.content == null) {
                return parent.filename;
            } else {
                return parent.content.getUniquePath();
            }
        }

        return null;
    }

    public Content getContent() {
        return content;
    }

    public void addChild(final Matchable child) {
        boolean oldType = hasChildren();
        int oldCount = getChildren().size();

        children.add(child);

        child.setParent(this);

        propertyChangeSupport.firePropertyChange(CHILDREN_TYPE, oldType, hasChildren());
        propertyChangeSupport.firePropertyChange(ADD_CHILD, oldCount, getChildren().size());
    }

    public int findChildren() {
        if (content == null || children.size() > 0) {
            return children.size();
        }

        Map<String, Matchable> input = new HashMap<>();
        input.put(Long.toString(content.getId()), this);

        try {
            sdbfSet.searchMatching(input);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return children.size();
    }

    public List<Matchable> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public static void fromSdhashResults(Map<String, Matchable> input, String results) throws TskCoreException {
        if (results.trim().length() == 0) {
            return;
        }

        String[] lines = fastSplit(results.trim(), '\n');
        String[] parts;
        Content matchedContent;

        Matchable matchable;

        for (String line : lines) {
            parts = fastSplit(line, '|');

            matchedContent = lookupFile(parts[1]);

            if (matchedContent != null) {
                parts[1] = matchedContent.getName();
            }

            matchable = new Matchable(parts[1], matchedContent, parts[2]);

            matchable.setReferenceSet(new File(parts[3]));

            input.get(parts[0]).addChild(matchable);
        }
    }

    private static String[] fastSplit(String line, char split) {
        String[] temp = new String[line.length() / 2];
        int wordCount = 0;
        int i = 0;
        int j = line.indexOf(split);
        while (j >= 0) {
            temp[wordCount++] = line.substring(i, j);
            i = j + 1;
            j = line.indexOf(split, i);
        }
        temp[wordCount++] = line.substring(i);
        String[] result = new String[wordCount];
        System.arraycopy(temp, 0, result, 0, wordCount);
        return result;
    }

    private static Content lookupFile(String potentialFileId) {
        try {
            return caseWrapper.getContentById(Long.parseLong(potentialFileId));
        } catch (NumberFormatException nfe) {
        } catch (TskCoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    boolean hasChild(Matchable childProbe) {
        for (Matchable child : children) {
            if (child.getFilename().equals(childProbe.getFilename())) {
                return true;
            }
        }

        return false;
    }

    public void setParent(Matchable parent) {
        this.parent = parent;
    }

    public Matchable getParent() {
        return parent;
    }

    public boolean isParentFor(Matchable probe) throws TskCoreException {
        if (probe.getIdentfier().equals(getIdentfier())) {
            return true;
        }

        Matchable currentParent = probe.getParent();

        while (currentParent != null) {
            if (currentParent.getFilename().equals(filename)) {
                return true;
            }

            currentParent = currentParent.getParent();
        }

        return false;
    }

    private void setReferenceSet(File referenceSet) {
        this.referenceSet = referenceSet;
    }

    public File getReferenceSet() {
        return referenceSet;
    }

    public void setCompleted(boolean completed) {
        boolean old = this.completed;

        this.completed = completed;

        propertyChangeSupport.firePropertyChange(NEW_STATUS, old, completed);
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String getUniqueIdentifier() {
        if (content != null) {
            try {
                return content.getUniquePath();
            } catch (TskCoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return filename;
    }

    public void setExpanded(boolean expanded) {
        boolean old = this.expanded;

        this.expanded = expanded;

        propertyChangeSupport.firePropertyChange(NEW_STATUS, old, expanded);
    }

    private String getIdentfier() throws TskCoreException {
        if (content != null) {
            return content.getUniquePath();
        } else {
            return filename;
        }
    }

    void removeChild(Matchable child) {
        boolean oldType = hasChildren();
        int oldCount = getChildren().size();

        children.remove(child);

        propertyChangeSupport.firePropertyChange(CHILDREN_TYPE, oldType, hasChildren());
        propertyChangeSupport.firePropertyChange(ADD_CHILD, oldCount, getChildren().size());
    }
}