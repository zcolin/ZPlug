package zrecyclerview;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import common.ChooseDirDialog;
import common.Util;
import constant.ZRecyclerViewActivity;
import constant.ZRecyclerViewFramgnet;
import constant.ZRecyclerViewLayout;
import findviewbyid.constant.Constant;

/**
 * FindViewByIdFromJavaDialog
 */
public class ContentShowDialog extends JFrame implements ActionListener, ItemListener, DocumentListener {
    private JBScrollPane mContentJPanel;// 内容JPanel
    private JTextArea jTextArea = new JTextArea();
    private JBScrollPane mContentJPanel1;// 内容JPanel
    private JTextArea    jTextArea1        = new JTextArea();
    private JPanel       mTopPannel        = new JPanel();
    private JTextField   mNameFiled        = new JTextField();
    private Label        label1            = new Label("name");
    private JPanel       mPanelButtonRight = new JPanel();
    private JButton      mButtonConfirm    = new JButton("Copy");
    private JButton      mButtonCreateFile = new JButton("CreateFile");
    private JPanel       mPanelInflater    = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 底部JPanel
    private JRadioButton mActivityflater   = new JRadioButton("Activity", true);
    private JRadioButton mFragemntflater   = new JRadioButton("Fragment", false);
    private ButtonGroup  mFileTypeGroup    = new ButtonGroup();
    private int          fileType          = 0;//0 Activity 1 Fragment  
    private final Project mProject;
    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout      mLayout      = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();
    private ChooseDirDialog mSelectDirDialog;

    public ContentShowDialog(Project project) {
        this.mProject = project;
        initTopPannel();
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    private void initTopPannel() {
        mNameFiled.getDocument()
                  .addDocumentListener(this);
        mTopPannel.add(label1);
        mTopPannel.add(mNameFiled);
        mTopPannel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagLayout bagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.weighty = 0;
        bagLayout.setConstraints(label1, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;
        bagLayout.setConstraints(mNameFiled, constraints);

        mTopPannel.setLayout(bagLayout);
        getContentPane().add(mTopPannel, 0);
    }

    /**
     * 添加底部
     */
    private void initBottomPanel() {
        // 添加监听
        mButtonConfirm.addActionListener(this);
        mButtonCreateFile.addActionListener(this);
        // 右边
        mPanelButtonRight.add(mButtonConfirm);
        mPanelButtonRight.add(mButtonCreateFile);

        //添加listener
        mActivityflater.addItemListener(this);
        mFragemntflater.addItemListener(this);
        //添加到group
        mFileTypeGroup.add(mActivityflater);
        mFileTypeGroup.add(mFragemntflater);
        //添加到JPanel
        mPanelInflater.add(mActivityflater);
        mPanelInflater.add(mFragemntflater);
        // 添加到JFrame
        getContentPane().add(mPanelInflater, 3);

        // 添加到JFrame
        getContentPane().add(mPanelButtonRight, 4);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel = new JBScrollPane(jTextArea);
        mContentJPanel.revalidate();
        mContentJPanel1 = new JBScrollPane(jTextArea1);
        mContentJPanel1.revalidate();
        getContentPane().add(mContentJPanel, 1);
        getContentPane().add(mContentJPanel1, 2);
        setContent();
    }

    private void setContent() {
        String str;
        if (fileType == 0) {
            str = ZRecyclerViewActivity.str;
        } else {
            str = ZRecyclerViewFramgnet.str;
        }
        str = str.replace("${name}", mNameFiled.getText());
        str = str.replace("${layout}", mNameFiled.getText()
                                                 .toLowerCase());
        jTextArea.setText(str);
        jTextArea1.setText(ZRecyclerViewLayout.str);
    }

    /**
     * 设置Constraints
     */
    private void setConstraints() {
        // 使组件完全填满其显示区域
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 0;
        mConstraints.weightx = 1;
        mConstraints.weighty = 0;
        mLayout.setConstraints(mTopPannel, mConstraints);
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
        mConstraints.weightx = 1;
        mConstraints.weighty = 2;
        mLayout.setConstraints(mContentJPanel, mConstraints);
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 2;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(mContentJPanel1, mConstraints);
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 3;
        mConstraints.weightx = 1;
        mConstraints.weighty = 0;
        mLayout.setConstraints(mPanelInflater, mConstraints);
        mConstraints.fill = GridBagConstraints.NONE;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 4;
        mConstraints.weightx = 0;
        mConstraints.weighty = 0;
        mConstraints.anchor = GridBagConstraints.EAST;
        mLayout.setConstraints(mPanelButtonRight, mConstraints);
    }

    /**
     * 设置JFrame参数
     */
    private void setDialog() {
        // 设置标题
        setTitle(Constant.dialogs.titleFindViewById);
        // 设置布局管理
        setLayout(mLayout);
        // 设置大小
        setSize(810, 705);
        // 自适应大小
        // pack();
        // 设置居中，放在setSize后面
        setLocationRelativeTo(null);
        // 显示最前
        setAlwaysOnTop(true);
    }

    /**
     * 关闭dialog
     */
    public void cancelDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * 显示dialog
     */
    public void showDialog() {
        // 显示
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Copy":
                Clipboard clipboard = Toolkit.getDefaultToolkit()
                                             .getSystemClipboard();
                Transferable trandata = new StringSelection(jTextArea.getText());
                clipboard.setContents(trandata, null);
                cancelDialog();
                break;
            case "CreateFile":
                if (mSelectDirDialog != null && mSelectDirDialog.isShowing()) {
                    mSelectDirDialog.cancelDialog();
                }
                mSelectDirDialog = new ChooseDirDialog(mProject);
                mSelectDirDialog.setOnPathSelectedListener((file, psiFile) -> {
                    if (psiFile != null) {
                        cancelDialog();
                        String fileName = mNameFiled.getText() + (fileType == 0 ? "Activity.java" : "Fragment.java");
                        Util.createJavaFile(mProject, file.getPath(), psiFile, fileName, jTextArea.getText());

                        Util.createXMLfile(mProject, (fileType == 0 ? "activity_" : "fragment_") + mNameFiled.getText()
                                                                                                             .toLowerCase() + ".xml", ZRecyclerViewLayout.str);
                    }
                });
                mSelectDirDialog.showDialog();
                break;
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fileType = e.getSource() == mFragemntflater ? 1 : 0;
        setContent();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        setContent();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setContent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }
}
