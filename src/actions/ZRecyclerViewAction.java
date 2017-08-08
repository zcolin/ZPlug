package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

import common.Util;
import zrecyclerview.ContentShowDialog;

public class ZRecyclerViewAction extends AnAction {
    private ContentShowDialog mDialog;

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        String packageName = Util.getPackageName(e.getProject());
        packageName = packageName.replace(".", "/");

        boolean flag = false;
        final PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement != null && psiElement instanceof PsiDirectory) {
            VirtualFile file = ((PsiDirectory) psiElement).getVirtualFile();
            if (file.getPath()
                    .contains(packageName)) {
                flag = true;
            }
        }
        e.getPresentation()
         .setVisible(flag);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement != null && psiElement instanceof PsiDirectory) {
            VirtualFile dirFile = ((PsiDirectory) psiElement).getVirtualFile();

            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancelDialog();
            }
            mDialog = new ContentShowDialog(project, dirFile);
            mDialog.showDialog();
        }
    }
}
