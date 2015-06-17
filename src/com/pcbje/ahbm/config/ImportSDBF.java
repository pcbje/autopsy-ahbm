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
package com.pcbje.ahbm.config;

import com.pcbje.ahbm.SdbfSet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.pcbje.ahbm.config.ImportSDBF")
@ActionRegistration(
        displayName = "#CTL_ImportSDBF")
@ActionReference(path = "Menu/Tools", position = 1800, separatorBefore = 1750)
@Messages("CTL_ImportSDBF=Import SDBF file")
public final class ImportSDBF implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        SdbfSet sdbfSet = new SdbfSet(null);

        final JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            try {
                sdbfSet.importSdbfSet(file);

                JOptionPane.showMessageDialog(null, "SDBF imported");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
