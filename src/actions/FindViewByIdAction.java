package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import constant.Constant;
import entitys.Element;
import utils.Util;
import views.FindViewByIdDialog;
import views.GenerateDialog;

public class FindViewByIdAction extends AnAction {
    private FindViewByIdDialog mDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取project
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取选中内容
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (mEditor == null) {
            return;
        }
        String mSelectedText = mEditor.getSelectionModel()
                                      .getSelectedText();
        //如果选中的内容为空，将当前光标所在位置选中
        if (StringUtils.isEmpty(mSelectedText)) {
            mEditor.getSelectionModel()
                   .selectWordAtCaret(true);
            mSelectedText = mEditor.getSelectionModel()
                                   .getSelectedText();
        }

        // 如果选中内容还是为空，显示dialog
        int popupTime = 5;
        if (StringUtils.isEmpty(mSelectedText)) {
            mSelectedText = Messages.showInputDialog(project, Constant.actions.selectedMessage, Constant.actions.selectedTitle, Messages.getInformationIcon());
            if (StringUtils.isEmpty(mSelectedText)) {
                Util.showPopupBalloon(mEditor, Constant.actions.selectedErrorNoName, popupTime);
                return;
            }
        }
        // 获取布局文件，通过FilenameIndex.getFilesByName获取
        // GlobalSearchScope.allScope(project)搜索整个项目
        PsiFile[] psiFiles = new PsiFile[0];
        psiFiles = FilenameIndex.getFilesByName(project, mSelectedText + Constant.selectedTextSUFFIX, GlobalSearchScope.allScope(project));
        if (psiFiles.length <= 0) {
            Util.showPopupBalloon(mEditor, Constant.actions.selectedErrorNoSelected, popupTime);
            return;
        }
        XmlFile xmlFile = (XmlFile) psiFiles[0];
        List<Element> elements = new ArrayList<>();
        Util.getIDsFromLayout(xmlFile, elements);
        // 将代码写入文件，不允许在主线程中进行实时的文件写入
        if (elements.size() == 0) {
            Util.showPopupBalloon(mEditor, Constant.actions.selectedErrorNoId, popupTime);
            return;
        }

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, project);
        PsiClass psiClass = Util.getTargetClass(mEditor, psiFile);
        if (psiClass == null) {
            Util.showPopupBalloon(mEditor, Constant.actions.selectedErrorNoPoint, popupTime);
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancelDialog();
        }
        mDialog = new FindViewByIdDialog(new GenerateDialog.Builder(elements.size())
                .setEditor(mEditor)
                .setProject(project)
                .setPsiFile(psiFile)
                .setClass(psiClass)
                .setElements(elements)
                .setSelectedText(mSelectedText));
        mDialog.showDialog();
    }
}
