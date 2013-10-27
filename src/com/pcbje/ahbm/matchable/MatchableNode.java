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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;
import org.sleuthkit.autopsy.datamodel.FileTypeExtensions;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author pcbje
 */
public class MatchableNode extends AbstractNode implements PropertyChangeListener {

    private static final Map<String, String> icons;
    private static final String DEFAULT_ICON = "org/sleuthkit/autopsy/images/file-icon.png";
    private static ExpandSelected expandSelected;
    private static ExportGexf exportGexf;
    private static VisualizeSimilarity visualizeSimilarity;
    private static RemoveFindings removeFindings;

    static {
        expandSelected = new ExpandSelected();
        exportGexf = new ExportGexf();
        visualizeSimilarity = new VisualizeSimilarity();
        removeFindings = new RemoveFindings();

        icons = new HashMap<String, String>();

        // Images
        for (String s : FileTypeExtensions.getImageExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/image-file.png");
        }
        // Videos
        for (String s : FileTypeExtensions.getVideoExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/video-file.png");
        }
        // Audio Files
        for (String s : FileTypeExtensions.getAudioExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/audio-file.png");
        }
        // Documents
        for (String s : FileTypeExtensions.getDocumentExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/doc-file.png");
        }
        // Executables / System Files
        for (String s : FileTypeExtensions.getExecutableExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/exe-file.png");
        }
        // Text Files
        for (String s : FileTypeExtensions.getTextExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/text-file.png");
        }

        // Web Files
        for (String s : FileTypeExtensions.getWebExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/web-file.png");

        }
        // PDFs
        for (String s : FileTypeExtensions.getPDFExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/pdf-file.png");
        }
        // Archives
        for (String s : FileTypeExtensions.getArchiveExtensions()) {
            icons.put(s, "org/sleuthkit/autopsy/images/archive-file.png");
        }
    }

    public MatchableNode(Children children, Lookup lookup) {
        super(children, lookup);

        Matchable file = lookup.lookup(Matchable.class);

        this.setIconBaseWithExtension(getIconForFileType(file));
    }

    @Override
    protected Sheet createSheet() {

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        Matchable obj = getLookup().lookup(Matchable.class);

        try {
            Property uniquePathProp = new PropertySupport.Reflection(obj, String.class, "getUniquePath", null);
            Property referenceSetProp = new PropertySupport.Reflection(obj, String.class, "getReferenceSetName", null);
            Property parentSimilarityProp = new PropertySupport.Reflection(obj, String.class, "getParentSimilarity", null);
            Property statusProp = new PropertySupport.Reflection(obj, String.class, "getStatus", null);
            Property parentProp = new PropertySupport.Reflection(obj, String.class, "getParentName", null);

            uniquePathProp.setName("uniquePath");
            referenceSetProp.setName("referenceSet");
            parentSimilarityProp.setName("parentSimilarity");
            statusProp.setName("status");
            parentProp.setName("parentName");

            uniquePathProp.setValue("suppressCustomEditor", true);
            referenceSetProp.setValue("suppressCustomEditor", true);
            parentSimilarityProp.setValue("suppressCustomEditor", true);
            statusProp.setValue("suppressCustomEditor", true);
            parentProp.setValue("suppressCustomEditor", true);

            uniquePathProp.setValue("nullValue", "");
            referenceSetProp.setValue("nullValue", "");
            parentSimilarityProp.setValue("nullValue", "");
            statusProp.setValue("nullValue", "");
            parentProp.setValue("nullValue", "");

            set.put(uniquePathProp);
            set.put(referenceSetProp);
            set.put(parentSimilarityProp);
            set.put(statusProp);
            set.put(parentProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        sheet.put(set);
        return sheet;

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Matchable.NEW_STATUS.equals(evt.getPropertyName())) {
            String oldname = getDisplayName();
            setDisplayName(getHtmlDisplayName());
            fireDisplayNameChange(oldname, getDisplayName());
        }
    }

    @Override
    public String getHtmlDisplayName() {
        Matchable matchable = getLookup().lookup(Matchable.class);

        StringBuilder displayName = new StringBuilder();

        if (matchable.getContent() == null) {
            displayName.append(matchable.getFilename());
        } else {
            try {
                displayName.append(matchable.getContent().getUniquePath());
            } catch (TskCoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return displayName.toString();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actionsList = new ArrayList<Action>();

        actionsList.add(expandSelected);
        actionsList.add(visualizeSimilarity);
        actionsList.add(null);
        actionsList.add(exportGexf);
        actionsList.add(removeFindings);


        return actionsList.toArray(new Action[0]);
    }

    static String getIconForFileType(Matchable content) {
        String name = content.getFilename();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex == -1) {
            return "org/sleuthkit/autopsy/images/file-icon.png";
        }

        String ext = name.substring(dotIndex).toLowerCase();

        if (icons.containsKey(ext)) {
            return icons.get(ext);
        } else {
            return DEFAULT_ICON;
        }
    }

    private static class ExpandSelected extends CookieAction {

        @Override
        protected int mode() {
            return CookieAction.MODE_ALL;
        }

        @Override
        protected Class<?>[] cookieClasses() {
            return new Class[]{Object.class};
        }

        @Override
        protected void performAction(Node[] nodes) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MatchableViewer drawer = (MatchableViewer) WindowManager.getDefault().findTopComponent("MatchViewer");

                    drawer.expandSelectedNodes();
                }
            });
        }

        @Override
        public String getName() {
            return "Expand selected";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    private static class ExportGexf extends CookieAction {

        @Override
        protected int mode() {
            return CookieAction.MODE_EXACTLY_ONE;
        }

        @Override
        protected Class<?>[] cookieClasses() {
            return new Class[]{Object.class};
        }

        @Override
        protected void performAction(Node[] nodes) {
            GexfExport export = new GexfExport();
            Matchable root = nodes[0].getLookup().lookup(Matchable.class);

            try {
                String path = export.export(root);
                JOptionPane.showMessageDialog(null, String.format("GEXF written to %s", path));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getName() {
            return "Export to GEXF";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    private static class VisualizeSimilarity extends CookieAction {

        @Override
        protected int mode() {
            return CookieAction.MODE_EXACTLY_ONE;
        }

        @Override
        protected Class<?>[] cookieClasses() {
            return new Class[]{Object.class};
        }

        @Override
        protected void performAction(Node[] nodes) {
            Matchable source = nodes[0].getLookup().lookup(Matchable.class);

            if (source == null || source.getParent() == null || source.getContent() == null || source.getParent().getContent() == null) {
                JOptionPane.showMessageDialog(null, "The content of both files has to be available in the open case");
                return;
            }
            try {
                JFrame frame = new BuflDiffResult(source.getContent(), source.getParent().getContent());
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getName() {
            return "Visualize similarity";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    private static class RemoveFindings extends CookieAction {

        @Override
        protected int mode() {
            return CookieAction.MODE_EXACTLY_ONE;
        }

        @Override
        protected Class<?>[] cookieClasses() {
            return new Class[]{Object.class};
        }

        @Override
        protected void performAction(Node[] nodes) {
            Matchable source = nodes[0].getLookup().lookup(Matchable.class);

            int res = JOptionPane.showConfirmDialog(null, "Remove the findings for this file?");

            if (res != JOptionPane.YES_OPTION) {
                return;
            }

            source.getParent().removeChild(source);
        }

        @Override
        public String getName() {
            return "Remove findings";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
}
