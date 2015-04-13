package com.basic4gl.desktop;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.MainWindow;
import com.basic4gl.desktop.util.EditorSourceFile;
import com.basic4gl.lib.util.*;
import com.basic4gl.vm.TomVM;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by Nate on 2/5/2015.
 */
public class ExportDialog {
    private MainWindow mMainWindow;
    private TomBasicCompiler mComp;
    private Preprocessor mPreprocessor;
    private TomVM mVM;
    private Vector<FileEditor> mFileEditors;

    private JDialog mDialog;
    private JTabbedPane mTabs;
    private JComboBox mTargetComboBox;
    private JLabel mTargetRunnableLabel;

    private JTextField mFilePathTextField;

    private JTextPane mTargetInfoTextPane;
    private JPanel mTargetConfigPane;

    private JButton mExportButton;
    //Libraries
    private java.util.List<Library> mLibraries;
    private java.util.List<Integer> mTargets;        //Indexes of libraries that can be launch targets
    private int mCurrentTarget;            //Index value of target in mTargets

    private java.util.List<JComponent> mSettingComponents = new ArrayList<JComponent>();
    private Configuration mCurrentConfig;
    public ExportDialog(MainWindow window, TomBasicCompiler compiler, Preprocessor preprocessor, Vector<FileEditor> editors) {
        mMainWindow = window;
        mComp = compiler;
        mPreprocessor = preprocessor;
        mVM = mComp.VM();
        mFileEditors = editors;

        mDialog = new JDialog(window.getFrame());

        mDialog.setTitle("Export Project");
        mDialog.setResizable(false);
        mDialog.setModal(true);

        mTabs = new JTabbedPane();
        mDialog.add(mTabs);

        JPanel buttonPane = new JPanel();
        mDialog.add(buttonPane, BorderLayout.SOUTH);
        mExportButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");
        mExportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportDialog.this.setVisible(false);
            }
        });
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(mExportButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);


        SwingUtilities.updateComponentTreeUI(mTabs);
        mTabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        mTabs.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        mTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        //File tab
        JPanel filePane = new JPanel();
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.LINE_AXIS));
        mTabs.addTab("File", filePane);

        //Settings tab; duplicate of the build tab in ProjectSettingsDialog
        JPanel targetPane = new JPanel();
        targetPane.setLayout(new BorderLayout());
        mTabs.addTab("Settings", targetPane);

        //Configure File tab
        filePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mFilePathTextField = new JTextField();
        mFilePathTextField.setMaximumSize(new Dimension((int)mFilePathTextField.getMaximumSize().getWidth(), 28));
        JButton fileButton = new JButton("...");
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter currentFilter = null;
                JFileChooser dialog = new JFileChooser();
                dialog.setAcceptAllFileFilterUsed(false);
                for (int i = 0; i < mTargets.size(); i++) {
                    Target target = (Target)mLibraries.get(mTargets.get(i));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(target.getFileDescription(),
                            target.getFileExtension());
                    if (i == mCurrentTarget)
                        currentFilter = filter;
                    dialog.addChoosableFileFilter(filter);
                }
                if (currentFilter != null)
                    dialog.setFileFilter(currentFilter);

                int result = dialog.showSaveDialog(mDialog);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = dialog.getSelectedFile().getAbsolutePath();
                    if(((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions().length > 0) {
                        //Append extension if needed
                        String extension = ((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions()[0];
                        if (!path.endsWith("." + extension))
                            path += "." + extension;
                    }

                    //Update file path
                    mFilePathTextField.setText(path);

                    //Change current target to match file extension if applicable
                    int index = Arrays.asList(dialog.getChoosableFileFilters()).indexOf(dialog.getFileFilter());
                    if (index != mCurrentTarget)
                        mTargetComboBox.setSelectedIndex(index);
                }
            }
        });

        filePane.add(new JLabel("File name:"));
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(mFilePathTextField);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(fileButton);

        //Configure Settings tab
        JPanel targetSelectionPane = new JPanel();
        targetPane.add(targetSelectionPane, BorderLayout.NORTH);
        targetSelectionPane.setLayout(new BoxLayout(targetSelectionPane, BoxLayout.LINE_AXIS));
        targetSelectionPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        targetSelectionPane.add(new JLabel("Target"));
        mTargetComboBox = new JComboBox();
        mTargetComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(mTargetComboBox);

        mTargetRunnableLabel = new JLabel(MainWindow.APPLICATION_DESCRIPTION);
        mTargetRunnableLabel.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));
        mTargetRunnableLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        targetSelectionPane.add(mTargetRunnableLabel);

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
        mTargetInfoTextPane = new JTextPane();
        //mTargetInfoTextPane.setBackground(Color.LIGHT_GRAY);
        mTargetInfoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(mTargetInfoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Properties:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        mTargetConfigPane = new JPanel();
        //mTargetConfigPane.setBackground(Color.LIGHT_GRAY);

        mTargetConfigPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(mTargetConfigPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        mTargetConfigPane.setLayout(new BoxLayout(mTargetConfigPane, BoxLayout.Y_AXIS));
        mTargetConfigPane.setAlignmentX(0f);


        mTargetComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                if (cb == null)
                    return;
                mCurrentTarget = cb.getSelectedIndex();
                Library target = mLibraries.get(mTargets.get(mCurrentTarget));

                mTargetRunnableLabel.setText(((Target)target).isRunnable() ?
                        "Build target is runnable":
                        "Build target is NOT runnable");

                //TODO Display target info
                mTargetInfoTextPane.setText(target.description());
                //Load settings
                mSettingComponents.clear();
                mTargetConfigPane.removeAll();
                mCurrentConfig = new Configuration(((Target) target).getConfiguration());
                String[] field;
                int param;
                String val;
                for (int i = 0; i < mCurrentConfig.getSettingCount(); i++){
                    JLabel label;
                    field = mCurrentConfig.getField(i);
                    param = mCurrentConfig.getParamType(i);
                    val = mCurrentConfig.getValue(i);

                    //Override parameter type if field contains multiple values
                    if (field.length > 1)
                        param = Configuration.PARAM_CHOICE;

                    switch (param){
                        case Configuration.PARAM_HEADING:
                            label = new JLabel(field[0]);
                            Font font = label.getFont();
                            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                            label.setBorder(new EmptyBorder(6, 6, 6, 6));
                            mTargetConfigPane.add(label);
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_DIVIDER:
                            mTargetConfigPane.add(Box.createVerticalStrut(16));
                            mTargetConfigPane.add(new JSeparator(JSeparator.HORIZONTAL));
                            mTargetConfigPane.add(Box.createVerticalStrut(2));
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_STRING:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mTargetConfigPane.add(label);
                            JTextField textField = new JTextField(val);
                            textField.setBorder(new EmptyBorder(4, 4,4,4));
                            mSettingComponents.add(textField);
                            mTargetConfigPane.add(textField);
                            break;
                        case Configuration.PARAM_BOOL:
                            JCheckBox checkBox = new JCheckBox(field[0]);
                            checkBox.setAlignmentX(0f);
                            checkBox.setSelected(Boolean.valueOf(val));
                            checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(checkBox);
                            mTargetConfigPane.add(checkBox);
                            break;
                        case Configuration.PARAM_INT:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            mTargetConfigPane.add(label);
                            JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(val).intValue(), 0, Short.MAX_VALUE, 1 ));
                            mSettingComponents.add(spinner);
                            mTargetConfigPane.add(spinner);
                            break;
                        case Configuration.PARAM_CHOICE:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4,4,4,4));
                            mTargetConfigPane.add(label);
                            label.setHorizontalAlignment(SwingConstants.LEFT);
                            JComboBox comboBox = new JComboBox();
                            for (int j = 1; j<field.length; j++)
                                comboBox.addItem(field[j]);
                            comboBox.setSelectedIndex(Integer.valueOf(val));
                            mSettingComponents.add(comboBox);
                            mTargetConfigPane.add(comboBox);
                            break;
                    }
                }
            }
        });
        //JScrollPane scrollPane = new ScrollPane(textLicenses);
        mDialog.pack();
        mDialog.setSize(new Dimension(464, 346));
        mDialog.setLocationRelativeTo(mMainWindow.getFrame());
    }

    private void applyConfig(){
        String val;
        if (mCurrentConfig == null)
            return;
        Target target = (Target)mLibraries.get(mTargets.get(mCurrentTarget));
        int param;
        for (int i = 0; i < mCurrentConfig.getSettingCount(); i++) {
            param = mCurrentConfig.getParamType(i);
            val = "0";
            if (mSettingComponents.get(i) == null)
                continue;
            switch (param) {
                case Configuration.PARAM_STRING:
                    val = ((JTextField)mSettingComponents.get(i)).getText();
                    break;
                case Configuration.PARAM_INT:
                    val = String.valueOf(((JSpinner) mSettingComponents.get(i)).getValue());
                    break;
                case Configuration.PARAM_BOOL:
                    val = String.valueOf(((JCheckBox) mSettingComponents.get(i)).isSelected());
                    break;
                case Configuration.PARAM_CHOICE:
                    val = String.valueOf(((JComboBox)mSettingComponents.get(i)).getSelectedIndex());
                    break;
            }
            mCurrentConfig.setValue(i, val);
        }
        target.setConfiguration(mCurrentConfig);
    }
    public void setVisible(boolean visible){
        mDialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, java.util.List<Integer> targets, int currentTarget){
        mLibraries = libraries;
        mTargets = targets;
        mCurrentTarget = currentTarget;

        mTargetComboBox.removeAllItems();
        for (Integer i: mTargets)
            mTargetComboBox.addItem(mLibraries.get(i).name());
        mTargetComboBox.setSelectedIndex(currentTarget);
    }


    public int getCurrentTarget(){
        return mCurrentTarget;
    }

    private void export(){
        try {
            File dest;
            int decision;

            Target target;
            Library lib = mLibraries.get(mTargets.get(mCurrentTarget));
            if (lib instanceof Target) {
                target = (Target) lib;
            } else {
                JOptionPane.showMessageDialog(mDialog,"Cannot build application. \n" + lib.name() + " is not a valid build target.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            applyConfig();
            if (!mFilePathTextField.getText().equals("")) {
                dest = new File(mFilePathTextField.getText());
                if (dest.isDirectory()){
                    JOptionPane.showMessageDialog(mDialog,"Please enter a filename.");
                    return;
                }
                if (!mFilePathTextField.getText().endsWith("." + target.getFileExtension()))
                    dest = new File(mFilePathTextField.getText() + "." + target.getFileExtension());

                if (dest.exists()){
                    Object[] options = {"Yes",
                            "No"};
                    decision = JOptionPane.showOptionDialog(mDialog, "File already exists! Overwrite?",
                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            options, options[1]);
                    if (decision == 1)
                        return;
                }
                enableComponents(mTabs, false);
                mExportButton.setEnabled(false);
                ExportWorker export = new ExportWorker(target, dest, new ExportCallback());
                export.execute();
            } else {
                JOptionPane.showMessageDialog(mDialog,"Please enter a filename.");
                return;
            }
        } catch (Exception e1) {
            enableComponents(mTabs, true);
            mExportButton.setEnabled(true);
            e1.printStackTrace();
        }
    }
    private class ExportWorker extends SwingWorker<Object, CallbackMessage>{
        private Target mTarget;
        private File mDest;
        private ExportCallback mCallback;
        private CallbackMessage mMessage;
        public ExportWorker(Target target, File dest, ExportCallback callback){
            mTarget = target;
            mDest = dest;
            mCallback = callback;
        }
        @Override
        protected void done(){
            int success;
            if (mCallback != null) {
                publish(mMessage);
            }
        }
        @Override
        protected void process(java.util.List<CallbackMessage> chunks) {
            for (CallbackMessage message : chunks) {
                mCallback.message(message);
            }
        }

        @Override
        protected Object doInBackground() throws Exception {
            mMessage = new CallbackMessage(CallbackMessage.WORKING, "");

            if (!Compile())
                return null; //TODO Throw error
            //Export to file
            FileOutputStream stream = new FileOutputStream(mDest);
            mTarget.export(stream, mCallback);
            mMessage.status = CallbackMessage.SUCCESS;
            mMessage.text = "Exported successful";
            return null;
        }

        // Program control
        private boolean Compile() {

            if (mFileEditors.isEmpty()) {
                mMessage.status = CallbackMessage.FAILED;
                mMessage.text = "No files are open";
                return false;
            }

            // Clear source code from parser
            mComp.Parser().SourceCode().clear();

            // Load code into preprocessor; may be unnecessary
            if (!LoadProgramIntoCompiler()) {
                mMessage.status = CallbackMessage.FAILED;
                mMessage.text = mPreprocessor.getError();
                return false;
            }

            // Compile
            mComp.clearError();
            mComp.Compile();

            // Return result
            if (mComp.hasError()) {
                mMessage.status = CallbackMessage.FAILED;
                mMessage.text = mComp.getError();
                return false;
            }

            // Reset Virtual machine
            //mVM.Reset ();

            mMessage.status = CallbackMessage.WORKING;
            mMessage.text = "User's code compiled";
            return true;
        }
        // Compilation and execution routines
        private boolean LoadProgramIntoCompiler (){
            //TODO Get editor assigned as main file
            return mPreprocessor.Preprocess(
                    new EditorSourceFile(mFileEditors.get(0).editorPane, mFileEditors.get(0).getFilePath()),
                    mComp.Parser());
        }
        private void LoadParser(RSyntaxTextArea editorPane) // Load editor text into parser
        {
            int start, stop; // line offsets
            String line; // line to add
            // Load editor text into parser (appended to bottom)
            try {
                for (int i = 0; i < editorPane.getLineCount(); i++) {
                    start = editorPane.getLineStartOffset(i);
                    stop = editorPane.getLineEndOffset(i);

                    line = editorPane.getText(start, stop - start);

                    mComp.Parser().SourceCode().add(line);

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
                enableComponents((Container)component, enable);
            }
        }
    }
    public class ExportCallback implements TaskCallback {

        @Override
        public void message(CallbackMessage message) {
            if (message.status == CallbackMessage.WORKING){
                //TODO display build progress
                System.out.println("Exporting...");
                return;
            }
            if (message.status == CallbackMessage.SUCCESS) {
                System.out.println("Export successful.");
                JOptionPane.showMessageDialog(mDialog, "Export successful!","Success", JOptionPane.INFORMATION_MESSAGE);
                mDialog.setVisible(false);
            } else if (message.status == CallbackMessage.FAILED){
                System.out.println("Export failed.");
                JOptionPane.showMessageDialog(mDialog, message.text,"Export failed.", JOptionPane.ERROR_MESSAGE);
            }
            enableComponents(mTabs, true);
            mExportButton.setEnabled(true);
        }


    }
}
