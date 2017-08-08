package zrecyclerview;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;

import org.apache.commons.lang.StringUtils;

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

import common.FileUtil;
import common.Util;

/**
 * FindViewByIdFromJavaDialog
 */
public class ContentShowDialog extends JFrame implements ActionListener, ItemListener, DocumentListener {
    private JBScrollPane mContentJPanel;// 内容JPanel
    private JTextArea    jTextArea           = new JTextArea();
    private JPanel       mTopPannel          = new JPanel();
    private JTextField   mNameFiled          = new JTextField();
    private JTextField   mDataTypeFiled      = new JTextField();
    private Label        label1              = new Label("name");
    private Label        label2              = new Label("data type");
    private JPanel       mPanelButtonRight   = new JPanel();
    private JButton      mButtonConfirm      = new JButton("Copy");
    private JButton      mButtonCreateFile   = new JButton("CreateFile");
    private JPanel       mCreateFileGroup    = new JPanel(new FlowLayout(FlowLayout.LEFT));// 文件生成选择
    private JBCheckBox   mCheckLayout        = new JBCheckBox("Layout", true);
    private JBCheckBox   mCheckAdapter       = new JBCheckBox("Adapter", true);
    private JBCheckBox   mCheckAdapterLayout = new JBCheckBox("AdapterLayout", true);
    private JPanel       mPanelInflater      = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 底部JPanel
    private JRadioButton mActivityflater     = new JRadioButton("Activity", true);
    private JRadioButton mFragemntflater     = new JRadioButton("Fragment", false);
    private ButtonGroup  mFileTypeGroup      = new ButtonGroup();
    private int          fileType            = 0;//0 Activity 1 Fragment  
    private final Project mProject;
    private GridBagLayout      mLayout      = new GridBagLayout(); // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagConstraints mConstraints = new GridBagConstraints(); // GridBagConstraints用来控制添加进的组件的显示位置
    private VirtualFile dirFile;

    public ContentShowDialog(Project project, VirtualFile dirFile) {
        this.mProject = project;
        this.dirFile = dirFile;
        initTopPannel();
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    private void initTopPannel() {
        mNameFiled.getDocument()
                  .addDocumentListener(this);
        mDataTypeFiled.getDocument()
                      .addDocumentListener(this);
        mTopPannel.add(label1);
        mTopPannel.add(mNameFiled);
        mTopPannel.add(label2);
        mTopPannel.add(mDataTypeFiled);
        mTopPannel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagLayout bagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.weighty = 0;
        bagLayout.setConstraints(label1, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        bagLayout.setConstraints(label2, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;
        bagLayout.setConstraints(mNameFiled, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
        bagLayout.setConstraints(mDataTypeFiled, constraints);

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
        mCheckLayout.addActionListener(this);
        mCheckAdapter.addActionListener(this);
        mCheckAdapterLayout.addActionListener(this);

        // 右边
        mPanelButtonRight.add(mButtonConfirm);
        mPanelButtonRight.add(mButtonCreateFile);

        mActivityflater.addItemListener(this);
        mFragemntflater.addItemListener(this);

        mFileTypeGroup.add(mActivityflater);
        mFileTypeGroup.add(mFragemntflater);

        mPanelInflater.add(mActivityflater);
        mPanelInflater.add(mFragemntflater);

        mCreateFileGroup.setToolTipText("Create File");
        mCreateFileGroup.add(mCheckLayout);
        mCreateFileGroup.add(mCheckAdapter);
        mCreateFileGroup.add(mCheckAdapterLayout);

        getContentPane().add(mCreateFileGroup, 2);
        getContentPane().add(mPanelInflater, 3);
        getContentPane().add(mPanelButtonRight, 4);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel = new JBScrollPane(jTextArea);
        mContentJPanel.revalidate();
        getContentPane().add(mContentJPanel, 1);
        setContent();
    }

    private void setContent() {
        String str;
        if (fileType == 0) {
            str = FileUtil.readFileStr(getClass().getResourceAsStream("/ZRecyclerViewActivity.txt"));
        } else {
            str = FileUtil.readFileStr(getClass().getResourceAsStream("/ZRecyclerViewFramgnet.txt"));
        }
        str = str.replace("${name}", mNameFiled.getText());
        str = str.replace("${layout}", mNameFiled.getText()
                                                 .toLowerCase());
        str = str.replace("${type}", mDataTypeFiled.getText());
        jTextArea.setText(str);
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
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 2;
        mConstraints.weightx = 1;
        mConstraints.weighty = 0;
        mLayout.setConstraints(mCreateFileGroup, mConstraints);
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
        setTitle("Create ZRecyclerView");
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
        if (StringUtils.isEmpty(mNameFiled.getText())) {
            Util.showInfoNotification(mProject, mNameFiled, "请输入名称");
            return;
        } else if (StringUtils.isEmpty(mDataTypeFiled.getText())) {
            Util.showInfoNotification(mProject, mDataTypeFiled, "请输入Adapter的数据类型");
            return;
        }

        switch (e.getActionCommand()) {
            case "Copy":
                Clipboard clipboard = Toolkit.getDefaultToolkit()
                                             .getSystemClipboard();
                Transferable trandata = new StringSelection(jTextArea.getText());
                clipboard.setContents(trandata, null);
                cancelDialog();
                break;
            case "CreateFile":
                cancelDialog();
                String strName = mNameFiled.getText();
                String strLayout = strName.toLowerCase();
                final PsiDirectory psiFile = PsiManager.getInstance(mProject)
                                                       .findDirectory(dirFile);
                if (psiFile == null) {
                    return;
                }

                //创建Activity或者Fragment的文件
                PsiDirectory psiActivityDir = psiFile.findSubdirectory("activity");
                if (psiActivityDir == null) {
                    psiActivityDir = psiFile.createSubdirectory("activity");
                }
                String activityFileName = strName + (fileType == 0 ? "Activity.java" : "Fragment.java");
                Util.createJavaFile(mProject, psiActivityDir, activityFileName, jTextArea.getText());

                if (mCheckLayout.isSelected()) {
                    String str = FileUtil.readFileStr(getClass().getResourceAsStream("/ZRecyclerViewLayout.txt"));
                    Util.createXMLfile(mProject, (fileType == 0 ? "activity_" : "fragment_") + strLayout + ".xml", str);
                }

                if (mCheckAdapter.isSelected()) {
                    PsiDirectory psiAcapterFile = psiFile.findSubdirectory("adapter");
                    if (psiAcapterFile == null) {
                        psiAcapterFile = psiFile.createSubdirectory("adapter");
                    }
                    String adapterFileName = strName + "Adapter.java";
                    String str = FileUtil.readFileStr(getClass().getResourceAsStream("/ZRecyclerAdapter.txt"));
                    str = str.replace("${name}", strName);
                    str = str.replace("${type}", mDataTypeFiled.getText());
                    str = str.replace("${layout}", strLayout);
                    Util.createJavaFile(mProject, psiAcapterFile, adapterFileName, str, false);
                }

                if (mCheckAdapterLayout.isSelected()) {
                    String str = FileUtil.readFileStr(getClass().getResourceAsStream("/ZRecyclerAdapterLayout.txt"));
                    Util.createXMLfile(mProject, "recycleritem_" + strLayout + ".xml", str);
                }

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
