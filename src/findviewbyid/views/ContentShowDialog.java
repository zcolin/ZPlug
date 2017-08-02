package findviewbyid.views;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import findviewbyid.constant.Constant;
import findviewbyid.entitys.Element;
import findviewbyid.utils.Util;

/**
 * FindViewByIdFromJavaDialog
 */
public class ContentShowDialog extends JFrame implements ActionListener, ItemListener {
    private JBScrollPane mContentJPanel;// 内容JPanel
    private JTextArea    jTextArea         = new JTextArea();
    private JPanel       mPanelButtonRight = new JPanel();
    private JButton      mButtonConfirm    = new JButton("copy");
    private JButton      mButtonCancel     = new JButton("cancel");
    private JPanel       mPanelInflater    = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 底部JPanel
    private JRadioButton mActivityflater   = new JRadioButton("Activity", true);
    private JRadioButton mFragemntflater   = new JRadioButton("Fragment", false);
    private JRadioButton mAdapterflater    = new JRadioButton("BaseAdapter", false);
    private ButtonGroup  mFileTypeGroup    = new ButtonGroup();
    private int          fileType          = 0;//0 Activity 1 Fragment  2 BaseAdapter
    private final List<Element> mElements;
    private final PsiFile       mPsiFile;// 获取当前文件
    private final Project       mProject;
    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout      mLayout      = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();

    public ContentShowDialog(List<Element> elements, PsiFile psiFile, Project project) {
        this.mElements = elements;
        this.mPsiFile = psiFile;
        this.mProject = project;
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    /**
     * 添加底部
     */
    private void initBottomPanel() {
        // 添加监听
        mButtonConfirm.addActionListener(this);
        mButtonCancel.addActionListener(this);
        // 右边
        mPanelButtonRight.add(mButtonConfirm);
        mPanelButtonRight.add(mButtonCancel);

        //添加listener
        mActivityflater.addItemListener(this);
        mFragemntflater.addItemListener(this);
        mAdapterflater.addItemListener(this);
        //添加到group
        mFileTypeGroup.add(mActivityflater);
        mFileTypeGroup.add(mFragemntflater);
        mFileTypeGroup.add(mAdapterflater);
        //添加到JPanel
        mPanelInflater.add(mActivityflater);
        mPanelInflater.add(mFragemntflater);
        mPanelInflater.add(mAdapterflater);
        // 添加到JFrame
        getContentPane().add(mPanelInflater, 1);

        // 添加到JFrame
        getContentPane().add(mPanelButtonRight, 2);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        jTextArea.setBackground(Color.getColor("#CCEED0"));
        mContentJPanel = new JBScrollPane(jTextArea);
        mContentJPanel.revalidate();
        getContentPane().add(mContentJPanel, 0);
        setContent();
    }

    private void setContent() {
        StringBuilder builder = new StringBuilder();
        if (fileType == 0) {
            builder.append("public class Activity extends BaseActivity{\n");
            for (Element element : mElements) {
                // 设置变量名，获取text里面的内容
                if (element.isEnable()) {
                    builder.append(Util.createFieldByElement(Util.getFieldComments(element, mProject), element));
                }
            }
            builder.append(Util.createOnCreateMethod(mPsiFile.getName()));
            builder.append(Util.createFieldsByInitViewMethod(mElements));
            builder.append("}");
        } else if (fileType == 1) {
            builder.append("public class Fragment extends BaseFragment{\n");
            for (Element element : mElements) {
                // 设置变量名，获取text里面的内容
                if (element.isEnable()) {
                    builder.append(Util.createFieldByElement(Util.getFieldComments(element, mProject), element));
                }
            }
            builder.append(Util.createGetRootLayoutId(mPsiFile.getName()));
            builder.append(Util.createCreateViewMethod());
            builder.append(Util.createFieldsByInitViewMethod(mElements));
            builder.append("}");
        } else if (fileType == 2) {
            builder.append("public class Adapter extends BaseRecyclerAdapter<T> {\n");
            builder.append(Util.createGetItemLayoutId(mPsiFile.getName()));
            builder.append(Util.createSetUpData(mElements));
        }
        jTextArea.setText(builder.toString());
    }

    /**
     * 设置Constraints
     */
    private void setConstraints() {
        // 使组件完全填满其显示区域
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(mContentJPanel, mConstraints);
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 2;
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

    protected void actionConfirm() {
        Clipboard clipboard = Toolkit.getDefaultToolkit()
                                     .getSystemClipboard();
        Transferable trandata = new StringSelection(jTextArea.getText());
        clipboard.setContents(trandata, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "copy":
                actionConfirm();
                cancelDialog();
                break;
            case "cancel":
                cancelDialog();
                break;

        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fileType = e.getSource() == mAdapterflater ? 2 : e.getSource() == mFragemntflater ? 1 : 0;
        setContent();
    }
}
