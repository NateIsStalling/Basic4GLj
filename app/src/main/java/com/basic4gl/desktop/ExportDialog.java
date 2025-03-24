package com.basic4gl.desktop;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.util.EditorSourceFile;
import com.basic4gl.lib.util.*;
import com.basic4gl.runtime.TomVM;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Created by Nate on 2/5/2015.
 */
public class ExportDialog implements ConfigurationFormPanel.IOnConfigurationChangeListener {
    private final TomBasicCompiler compiler;
    private final Preprocessor preprocessor;
    private final TomVM vm;
    private final Vector<FileEditor> fileEditors;

    private final Dialog dialog;
    private final JTabbedPane tabs;
    private final JComboBox builderComboBox;

    private final JTextField filePathTextField;

    private final JTextPane infoTextPane;
    private final ConfigurationFormPanel configPane;
    private final JButton exportButton;

    // Libraries
    private java.util.List<Library> libraries;
    private java.util.List<Integer> builders; // Indexes of libraries that can be launch targets
    private int currentBuilder; // Index value of target in mTargets

    private final java.util.List<JComponent> settingComponents = new ArrayList<>();
    private Configuration currentConfig;

    public ExportDialog(
            Frame parent, TomBasicCompiler compiler, Preprocessor preprocessor, Vector<FileEditor> editors) {

        this.compiler = compiler;
        this.preprocessor = preprocessor;
        vm = this.compiler.getVM();
        fileEditors = editors;

        dialog = new JDialog(parent);

        dialog.setTitle("Export Project");
        dialog.setResizable(false);
        dialog.setModal(true);

        tabs = new JTabbedPane();
        dialog.add(tabs);

        JPanel buttonPane = new JPanel();
        dialog.add(buttonPane, BorderLayout.SOUTH);
        exportButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");
        exportButton.addActionListener(e -> export());
        cancelButton.addActionListener(e -> ExportDialog.this.setVisible(false));
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(exportButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        SwingUtilities.updateComponentTreeUI(tabs);
        tabs.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        tabs.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // File tab
        JPanel filePane = new JPanel();
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.LINE_AXIS));
        tabs.addTab("File", filePane);

        // Settings tab; duplicate of the build tab in ProjectSettingsDialog
        JPanel targetPane = new JPanel();
        targetPane.setLayout(new BorderLayout());
        tabs.addTab("Settings", targetPane);

        // Configure File tab
        filePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filePathTextField = new JTextField();
        filePathTextField.setMaximumSize(
                new Dimension((int) filePathTextField.getMaximumSize().getWidth(), 28));
        JButton fileButton = new JButton("...");
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter currentFilter = null;
                JFileChooser dialog = new JFileChooser();
                dialog.setAcceptAllFileFilterUsed(false);
                for (int i = 0; i < builders.size(); i++) {
                    Builder builder = (Builder) libraries.get(builders.get(i));
                    FileNameExtensionFilter filter =
                            new FileNameExtensionFilter(builder.getFileDescription(), builder.getFileExtension());
                    if (i == currentBuilder) {
                        currentFilter = filter;
                    }
                    dialog.addChoosableFileFilter(filter);
                }
                if (currentFilter != null) {
                    dialog.setFileFilter(currentFilter);
                }

                int result = dialog.showSaveDialog(ExportDialog.this.dialog);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = dialog.getSelectedFile().getAbsolutePath();
                    if (((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions().length > 0) {
                        // Append extension if needed
                        String extension = ((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions()[0];
                        if (!path.endsWith("." + extension)) {
                            path += "." + extension;
                        }
                    }

                    // Update file path
                    filePathTextField.setText(path);

                    // Change current target to match file extension if applicable
                    int index = Arrays.asList(dialog.getChoosableFileFilters()).indexOf(dialog.getFileFilter());
                    if (index != currentBuilder) {
                        builderComboBox.setSelectedIndex(index);
                    }
                }
            }
        });

        filePane.add(new JLabel("File name:"));
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(filePathTextField);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(fileButton);

        // Configure Settings tab
        JPanel targetSelectionPane = new JPanel();
        targetPane.add(targetSelectionPane, BorderLayout.NORTH);
        targetSelectionPane.setLayout(new BoxLayout(targetSelectionPane, BoxLayout.LINE_AXIS));
        targetSelectionPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        targetSelectionPane.add(new JLabel("Target"));
        builderComboBox = new JComboBox();
        builderComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(builderComboBox);

        JPanel buildInfoPane = new JPanel();
        targetPane.add(buildInfoPane, BorderLayout.CENTER);
        GridLayout buildInfoPaneLayout = new GridLayout(1, 2);
        buildInfoPane.setLayout(buildInfoPaneLayout);

        JPanel infoPanel = new JPanel();
        buildInfoPane.add(infoPanel);

        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel infoLabel = new JLabel("Library Info:");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoLabel, BorderLayout.PAGE_START);
        infoTextPane = new JTextPane();
        // mInfoTextPane.setBackground(Color.LIGHT_GRAY);
        infoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(infoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Properties:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        configPane = new ConfigurationFormPanel(this);
        // mConfigPane.setBackground(Color.LIGHT_GRAY);

        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        configPane.setLayout(new BoxLayout(configPane, BoxLayout.Y_AXIS));
        configPane.setAlignmentX(0f);

        builderComboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb == null) {
                return;
            }
            int builderIndex = cb.getSelectedIndex();
            selectBuilder(builderIndex);
        });
        // JScrollPane scrollPane = new ScrollPane(textLicenses);
        dialog.pack();
        dialog.setSize(new Dimension(464, 346));
        dialog.setLocationRelativeTo(parent);
    }

    private void selectBuilder(int builderIndex) {
        currentBuilder = builderIndex;
        Library target = libraries.get(builders.get(currentBuilder));

        // TODO Display target info
        infoTextPane.setText(target.description());
        configPane.setConfiguration(new Configuration(((Builder) target).getConfiguration()));
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, int currentBuilder) {
        builderComboBox.removeAllItems();
        this.currentBuilder = currentBuilder;
        this.libraries = libraries;
        builders = new ArrayList<>();
        int i = 0;
        for (Library lib : this.libraries) {
            if (lib instanceof FunctionLibrary) {
                compiler.addConstants(((FunctionLibrary) lib).constants());
                compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            }
            if (lib instanceof Builder) {
                builders.add(i);
                builderComboBox.addItem(this.libraries.get(i).name());
            }
            i++;
        }
        builderComboBox.setSelectedIndex(currentBuilder);
    }

    public int getCurrentBuilder() {
        return currentBuilder;
    }

    private void export() {
        try {
            File dest;
            int decision;

            Builder builder;
            Library lib = libraries.get(builders.get(currentBuilder));
            if (lib instanceof Builder) {
                builder = (Builder) lib;
            } else {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Cannot build application. \n" + lib.name() + " is not a valid builder.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            configPane.applyConfig();

            if (!filePathTextField.getText().isEmpty()) {
                dest = new File(filePathTextField.getText());
                if (dest.isDirectory()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a filename.");
                    return;
                }
                if (!filePathTextField.getText().endsWith("." + builder.getFileExtension())) {
                    dest = new File(filePathTextField.getText() + "." + builder.getFileExtension());
                }

                if (dest.exists()) {
                    Object[] options = {"Yes", "No"};
                    decision = JOptionPane.showOptionDialog(
                            dialog,
                            "File already exists! Overwrite?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                    if (decision == 1) {
                        return;
                    }
                }
                enableComponents(tabs, false);
                exportButton.setEnabled(false);
                ExportWorker export = new ExportWorker(builder, dest, new ExportCallback());
                export.execute();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please enter a filename.");
                return;
            }
        } catch (Exception e1) {
            enableComponents(tabs, true);
            exportButton.setEnabled(true);
            e1.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        Builder builder = (Builder) libraries.get(builders.get(currentBuilder));

        builder.setConfiguration(configuration);
    }

    private class ExportWorker extends SwingWorker<Object, CallbackMessage> {
        private final Builder builder;
        private final File dest;
        private final ExportCallback exportCallback;
        private CallbackMessage callbackMessage;

        public ExportWorker(Builder builder, File dest, ExportCallback callback) {
            this.builder = builder;
            this.dest = dest;
            exportCallback = callback;
        }

        @Override
        protected void done() {
            int success;
            if (exportCallback != null) {
                publish(callbackMessage);
            }
        }

        @Override
        protected void process(java.util.List<CallbackMessage> chunks) {
            for (CallbackMessage message : chunks) {
                exportCallback.message(message);
            }
        }

        @Override
        protected Object doInBackground() throws Exception {
            callbackMessage = new CallbackMessage(CallbackMessage.WORKING, "");

            if (!compile()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, compiler.getError());
                return null; // TODO Throw error
            }
            // Export to file
            FileOutputStream stream = new FileOutputStream(dest);

            try {
                boolean success = builder.export(dest.getName(), stream, exportCallback);

                if (success) {
                    callbackMessage.setMessage(CallbackMessage.SUCCESS, "Exported successful");
                } else {
                    callbackMessage.setMessage(CallbackMessage.FAILED, "Export failed");
                }
            } catch (Exception e) {
                e.printStackTrace();

                callbackMessage.setMessage(CallbackMessage.FAILED, e.getMessage());
            }

            return null;
        }

        // Program control
        private boolean compile() {

            if (fileEditors.isEmpty()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, "No files are open");
                return false;
            }

            // Clear source code from parser
            compiler.getParser().getSourceCode().clear();

            // Load code into preprocessor; may be unnecessary
            if (!loadProgramIntoCompiler()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, preprocessor.getError());
                return false;
            }

            // Compile
            compiler.clearError();
            compiler.compile();

            // Return result
            if (compiler.hasError()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, compiler.getError());
                return false;
            }

            // Reset Virtual machine
            // mVM.Reset ();

            callbackMessage.setMessage(CallbackMessage.WORKING, "User's code compiled");
            return true;
        }

        // Compilation and execution routines
        private boolean loadProgramIntoCompiler() {
            // TODO Get editor assigned as main file
            return preprocessor.preprocess(
                    new EditorSourceFile(
                            fileEditors.get(0).getEditorPane(),
                            fileEditors.get(0).getFilePath()),
                    compiler.getParser());
        }

        private void loadParser(RSyntaxTextArea editorPane) // Load editor text into parser
                {
            int start, stop; // line offsets
            String line; // line to add
            // Load editor text into parser (appended to bottom)
            try {
                for (int i = 0; i < editorPane.getLineCount(); i++) {
                    start = editorPane.getLineStartOffset(i);
                    stop = editorPane.getLineEndOffset(i);

                    line = editorPane.getText(start, stop - start);

                    compiler.getParser().getSourceCode().add(line);
                }
            } catch (BadLocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }

    public class ExportCallback implements TaskCallback {

        @Override
        public void message(CallbackMessage message) {
            if (message.getStatus() == CallbackMessage.WORKING) {
                // TODO display build progress
                System.out.println("Exporting...");
                return;
            }
            if (message.getStatus() == CallbackMessage.SUCCESS) {
                System.out.println("Export successful.");
                JOptionPane.showMessageDialog(dialog, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.setVisible(false);
            } else if (message.getStatus() == CallbackMessage.FAILED) {
                System.out.println("Export failed.");
                JOptionPane.showMessageDialog(dialog, message.getText(), "Export failed.", JOptionPane.ERROR_MESSAGE);
            }
            enableComponents(tabs, true);
            exportButton.setEnabled(true);
        }
    }
}
