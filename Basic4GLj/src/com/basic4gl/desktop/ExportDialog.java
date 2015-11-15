package com.basic4gl.desktop;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
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
    private JComboBox mBuilderComboBox;

    private JTextField mFilePathTextField;

    private JTextPane mInfoTextPane;
    private JPanel mConfigPane;

    private JButton mExportButton;
    //Libraries
    private java.util.List<Library> mLibraries;
    private java.util.List<Integer> mBuilders;  //Indexes of libraries that can be launch targets
    private int mCurrentBuilder;                 //Index value of target in mTargets

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
                for (int i = 0; i < mBuilders.size(); i++) {
                    Builder builder = (Builder)mLibraries.get(mBuilders.get(i));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(builder.getFileDescription(),
                            builder.getFileExtension());
                    if (i == mCurrentBuilder)
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
                    if (index != mCurrentBuilder)
                        mBuilderComboBox.setSelectedIndex(index);
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
        mBuilderComboBox = new JComboBox();
        mBuilderComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(mBuilderComboBox);

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
        mInfoTextPane = new JTextPane();
        //mInfoTextPane.setBackground(Color.LIGHT_GRAY);
        mInfoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(mInfoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Properties:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        mConfigPane = new JPanel();
        //mConfigPane.setBackground(Color.LIGHT_GRAY);

        mConfigPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(mConfigPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        mConfigPane.setLayout(new BoxLayout(mConfigPane, BoxLayout.Y_AXIS));
        mConfigPane.setAlignmentX(0f);


        mBuilderComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                if (cb == null)
                    return;
                mCurrentBuilder = cb.getSelectedIndex();
                Library target = mLibraries.get(mBuilders.get(mCurrentBuilder));

                //TODO Display target info
                mInfoTextPane.setText(target.description());
                //Load settings
                mSettingComponents.clear();
                mConfigPane.removeAll();
                mCurrentConfig = new Configuration(((Builder) target).getConfiguration());
                String[] field;
                int param;
                String val;
                for (int i = 0; i < mCurrentConfig.getSettingCount(); i++) {
                    JLabel label;
                    field = mCurrentConfig.getField(i);
                    param = mCurrentConfig.getParamType(i);
                    val = mCurrentConfig.getValue(i);

                    //Override parameter type if field contains multiple values
                    if (field.length > 1)
                        param = Configuration.PARAM_CHOICE;

                    switch (param) {
                        case Configuration.PARAM_HEADING:
                            label = new JLabel(field[0]);
                            Font font = label.getFont();
                            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                            label.setBorder(new EmptyBorder(6, 6, 6, 6));
                            mConfigPane.add(label);
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_DIVIDER:
                            mConfigPane.add(Box.createVerticalStrut(16));
                            mConfigPane.add(new JSeparator(JSeparator.HORIZONTAL));
                            mConfigPane.add(Box.createVerticalStrut(2));
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_STRING:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mConfigPane.add(label);
                            JTextField textField = new JTextField(val);
                            textField.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(textField);
                            mConfigPane.add(textField);
                            break;
                        case Configuration.PARAM_BOOL:
                            JCheckBox checkBox = new JCheckBox(field[0]);
                            checkBox.setAlignmentX(0f);
                            checkBox.setSelected(Boolean.valueOf(val));
                            checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(checkBox);
                            mConfigPane.add(checkBox);
                            break;
                        case Configuration.PARAM_INT:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            mConfigPane.add(label);
                            JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(val).intValue(), 0, Short.MAX_VALUE, 1));
                            mSettingComponents.add(spinner);
                            mConfigPane.add(spinner);
                            break;
                        case Configuration.PARAM_CHOICE:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mConfigPane.add(label);
                            label.setHorizontalAlignment(SwingConstants.LEFT);
                            JComboBox comboBox = new JComboBox();
                            for (int j = 1; j < field.length; j++)
                                comboBox.addItem(field[j]);
                            comboBox.setSelectedIndex(Integer.valueOf(val));
                            mSettingComponents.add(comboBox);
                            mConfigPane.add(comboBox);
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
        Builder builder = (Builder)mLibraries.get(mBuilders.get(mCurrentBuilder));
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
        builder.setConfiguration(mCurrentConfig);
    }
    public void setVisible(boolean visible){
        mDialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, int currentBuilder){
        mBuilderComboBox.removeAllItems();
        mCurrentBuilder = currentBuilder;
        mLibraries = libraries;
        mBuilders = new ArrayList<>();
        int i = 0;
        for (Library lib : mLibraries) {
            mComp.AddConstants(lib.constants());
            mComp.AddFunctions(lib, lib.specs());
            if (lib instanceof Builder) {
                mBuilders.add(i);
                mBuilderComboBox.addItem(mLibraries.get(i).name());
            }
            i++;
        }
        mBuilderComboBox.setSelectedIndex(currentBuilder);
    }


    public int getCurrentBuilder(){
        return mCurrentBuilder;
    }

    private void export(){
        try {
            File dest;
            int decision;

            Builder builder;
            Library lib = mLibraries.get(mBuilders.get(mCurrentBuilder));
            if (lib instanceof Builder) {
                builder = (Builder) lib;
            } else {
                JOptionPane.showMessageDialog(mDialog,"Cannot build application. \n" + lib.name() + " is not a valid builder.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            applyConfig();
            if (!mFilePathTextField.getText().equals("")) {
                dest = new File(mFilePathTextField.getText());
                if (dest.isDirectory()){
                    JOptionPane.showMessageDialog(mDialog,"Please enter a filename.");
                    return;
                }
                if (!mFilePathTextField.getText().endsWith("." + builder.getFileExtension()))
                    dest = new File(mFilePathTextField.getText() + "." + builder.getFileExtension());

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
                ExportWorker export = new ExportWorker(builder, dest, new ExportCallback());
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
        private Builder mBuilder;
        private File mDest;
        private ExportCallback mCallback;
        private CallbackMessage mMessage;
        public ExportWorker(Builder builder, File dest, ExportCallback callback){
            mBuilder = builder;
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
            mBuilder.export(mDest.getName(), stream, mCallback);
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
