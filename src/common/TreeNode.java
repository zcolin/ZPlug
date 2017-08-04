package common;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class TreeNode extends DefaultMutableTreeNode {
    public TreeNode(Object userObject) {
        super(userObject);
    }

    @Override
    public String toString() {
        if (userObject == null) {
            return "";
        } else if (userObject instanceof String) {
            return (String) userObject;
        } else if (userObject instanceof VirtualFile) {
            return ((VirtualFile) userObject).getName();
        }
        return "";
    }
}
