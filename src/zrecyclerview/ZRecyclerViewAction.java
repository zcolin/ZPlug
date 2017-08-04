package zrecyclerview;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ZRecyclerViewAction extends AnAction {
    private ContentShowDialog mDialog;

    @Override
    public void update(AnActionEvent e) {

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancelDialog();
        }
        mDialog = new ContentShowDialog(project);
        mDialog.showDialog();
    }
}
