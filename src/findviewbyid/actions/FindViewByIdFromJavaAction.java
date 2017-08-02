package findviewbyid.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import findviewbyid.constant.Constant;
import findviewbyid.entitys.Element;
import findviewbyid.utils.Util;
import findviewbyid.views.FindViewByIdFromJavaDialog;
import findviewbyid.views.GenerateDialog;

public class FindViewByIdFromJavaAction extends AnAction {
    private FindViewByIdFromJavaDialog mDialog;
    private String                     mSelectedText;

    @Override
    public void update(AnActionEvent e) {
        if (isJava(e)) {
            PsiFile[] psiFiles = getPsiFile(e);
            e.getPresentation()
             .setEnabled(psiFiles != null && psiFiles.length > 0);
        } else {
            e.getPresentation()
             .setVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile[] psiFiles = getPsiFile(e);
        if (psiFiles == null || psiFiles.length <= 0) {
            return;
        }

        int popupTime = 5;
        Project project = e.getProject();
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        XmlFile xmlFile = (XmlFile) psiFiles[0];
        List<Element> elements = new ArrayList<>();
        Util.getIDsFromLayout(xmlFile, elements);
        // 将代码写入文件，不允许在主线程中进行实时的文件写入
        if (elements.size() == 0) {
            Util.showPopupBalloon(editor, Constant.actions.selectedErrorNoId, popupTime);
            return;
        }

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass psiClass = Util.getTargetClass(editor, psiFile);
        if (psiClass == null) {
            Util.showPopupBalloon(editor, Constant.actions.selectedErrorNoPoint, popupTime);
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancelDialog();
        }
        mDialog = new FindViewByIdFromJavaDialog(new GenerateDialog.Builder(elements.size())
                .setEditor(editor)
                .setProject(project)
                .setPsiFile(psiFile)
                .setClass(psiClass)
                .setElements(elements)
                .setSelectedText(mSelectedText));
        mDialog.showDialog();
    }

    /**
     * @return 0 java  1 xml
     */
    private boolean isJava(AnActionEvent e) {
        final PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        if (psiFile == null) {
            return false;
        }
        return ("JAVA".equalsIgnoreCase(psiFile.getFileType()
                                               .getName()));
    }

    private PsiFile[] getPsiFile(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        // 获取选中内容
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }

        mSelectedText = editor.getSelectionModel()
                              .getSelectedText();
        //如果选中的内容为空，将当前光标所在位置选中
        if (StringUtils.isEmpty(mSelectedText)) {
            editor.getSelectionModel()
                  .selectWordAtCaret(true);
            mSelectedText = editor.getSelectionModel()
                                  .getSelectedText();
        }

        // 获取布局文件，通过FilenameIndex.getFilesByName获取
        // GlobalSearchScope.allScope(project)搜索整个项目
        PsiFile[] psiFiles = new PsiFile[0];
        psiFiles = FilenameIndex.getFilesByName(project, mSelectedText + Constant.selectedTextSUFFIX, GlobalSearchScope.allScope(project));
        return psiFiles;
    }
}
