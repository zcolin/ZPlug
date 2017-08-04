package common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 */
public class ChooseDirDialog extends JFrame implements ActionListener {
    private JBScrollPane mContentJPanel;// 内容JPanel
    private JPanel  mPanelButtonRight = new JPanel();
    private JButton mButtonConfirm    = new JButton("confirm");
    private JButton mButtonCancel     = new JButton("cancel");
    private Project mProject;
    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout      mLayout      = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();
    private VirtualFile            selectedFile;
    private OnPathSelectedListener onPathSelectedListener;

    public ChooseDirDialog(Project project) {
        this.mProject = project;
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    public void setOnPathSelectedListener(OnPathSelectedListener listener) {
        this.onPathSelectedListener = listener;
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

        // 添加到JFrame
        getContentPane().add(mPanelButtonRight, 1);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel = new JBScrollPane(createTree(mProject));
        mContentJPanel.revalidate();
        getContentPane().add(mContentJPanel, 0);
    }

    private Tree createTree(Project project) {
        VirtualFile srcFile = getSrcFile(project.getBaseDir());
        TreeNode node = new TreeNode(srcFile);
        createNode(srcFile, node);

        final Tree tree = new Tree(node);
        tree.setBackground(new Color(204, 238, 208));
        tree.expandRow(0);
        tree.expandRow(1);
        tree.expandRow(2);
        tree.addTreeSelectionListener(e -> {
            TreeNode node1 = (TreeNode) tree.getLastSelectedPathComponent();
            if (node1 == null)
                return;

            Object object = node1.getUserObject();
            selectedFile = (VirtualFile) object;
        });
        return tree;
    }

    private VirtualFile getSrcFile(VirtualFile parent) {
        if (parent == null) {
            return null;
        }
        File file = new File(parent.getPath() + "/app/src/main/java/");
        if (file.exists()) {
            return LocalFileSystem.getInstance()
                                  .findFileByIoFile(file);
        }

        return parent;
    }

    private void createNode(VirtualFile parent, TreeNode parentNode) {
        if (parent == null) {
            return;
        }

        VirtualFile[] virtualFiles = parent.getChildren();
        if (virtualFiles != null) {
            for (VirtualFile virtualFile : virtualFiles) {
                if (virtualFile.isDirectory() && !virtualFile.getName()
                                                             .startsWith(".")) {
                    TreeNode node = new TreeNode(virtualFile);
                    parentNode.add(node);
                    createNode(virtualFile, node);
                }
            }
        }
    }

    /**
     * 设置Constraints
     */
    private void setConstraints() {
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 0;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(mContentJPanel, mConstraints);
        mConstraints.fill = GridBagConstraints.NONE;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
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
        setTitle("选择生成目录");
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
            case "confirm":
                if (selectedFile != null && onPathSelectedListener != null) {
                    cancelDialog();
                    final PsiDirectory psiFile = PsiManager.getInstance(mProject)
                                                           .findDirectory(selectedFile);
                    onPathSelectedListener.onPathSelected(selectedFile, psiFile);
                }
                break;
            case "cancel":
                cancelDialog();
                break;
        }
    }

    public interface OnPathSelectedListener {
        void onPathSelected(VirtualFile file, PsiDirectory psiFile);
    }
}
