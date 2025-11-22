package com.rannett.fixplugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialog used to edit a FIX version to dictionary path mapping.
 */
public class DictionaryMappingEditDialog extends DialogWrapper {

    private final JTextField versionField;
    private final TextFieldWithBrowseButton pathField;
    private final JPanel contentPanel;

    /**
     * Creates a dialog for editing a FIX dictionary mapping.
     *
     * @param project         the current project hosting the dialog
     * @param currentVersion  the version currently stored in the mapping row
     * @param currentPath     the dictionary path currently stored in the mapping row
     * @param descriptor      descriptor describing valid dictionary files
     */
    public DictionaryMappingEditDialog(@Nullable Project project,
                                       @Nullable String currentVersion,
                                       @Nullable String currentPath,
                                       FileChooserDescriptor descriptor) {
        super(project, true);
        setTitle("Edit Mapping");
        versionField = new JTextField(currentVersion != null ? currentVersion : "");
        pathField = new TextFieldWithBrowseButton();
        if (descriptor != null) {
            pathField.addBrowseFolderListener(new TextBrowseFolderListener(descriptor, project));
        }
        pathField.setText(currentPath != null ? currentPath : "");
        contentPanel = buildContentPanel();
        init();
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 0, 4, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(new JLabel("FIX Version:"), constraints);

        constraints.gridy = 1;
        panel.add(versionField, constraints);

        constraints.gridy = 2;
        panel.add(new JLabel("Custom Dictionary Path:"), constraints);

        constraints.gridy = 3;
        panel.add(pathField, constraints);

        return panel;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPanel;
    }

    /**
     * @return the FIX version entered by the user.
     */
    public String getVersion() {
        return versionField.getText().trim();
    }

    /**
     * @return the dictionary path selected by the user.
     */
    public String getDictionaryPath() {
        return pathField.getText().trim();
    }
}
