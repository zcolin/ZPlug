package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.List;

import common.Util;
import findviewbyid.constant.Constant;
import findviewbyid.entitys.Element;
import findviewbyid.views.FindViewByIdFromJavaDialog;
import findviewbyid.views.FindViewByIdFromXMLDialog;
import findviewbyid.views.GenerateDialog;

public class FindViewByIdFromXmlAction extends AnAction {
    private FindViewByIdFromJavaDialog mDialog;
    private String                     mSelectedText;

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation()
         .setVisible(isXML(e));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getProject();
        if (psiFile == null || editor == null || project == null) {
            return;
        }

        int popupTime = 5;
        String psiFileName = psiFile.getName();
        XmlFile xmlFile = (XmlFile) psiFile;
        List<Element> elements = new ArrayList<>();
        Util.getIDsFromLayout(xmlFile, elements);
        if (elements.size() == 0) {
            Util.showPopupBalloon(editor, Constant.actions.selectedErrorNoId, popupTime);
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancelDialog();
        }
        mDialog = new FindViewByIdFromXMLDialog(new GenerateDialog.Builder(elements.size())
                .setEditor(editor)
                .setProject(project)
                .setPsiFile(psiFile)
                .setElements(elements)
                .setSelectedText(mSelectedText));
        mDialog.showDialog();
    }

    /**
     * @return 0 java  1 xml
     */
    private boolean isXML(AnActionEvent e) {
        final PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        if (psiFile == null) {
            return false;
        }
        return ("XML".equalsIgnoreCase(psiFile.getFileType()
                                              .getName()));
    }
}
