package views;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSwitchStatement;

import java.util.List;

import entitys.Element;
import utils.Util;

/**
 * FindViewByIdDialog
 */
public class FindViewByIdDialog extends GenerateDialog {

    /**
     * FindViewByIdDialog
     *
     * @param builder Builder
     */
    public FindViewByIdDialog(Builder builder) {
        super(builder);
        initExist();
        initTopPanel();
        initContentPanel();
        setCheckAll();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    /**
     * 判断已存在的变量，设置全选
     * 判断onclick是否写入
     */
    private void initExist() {
        PsiClass mClass = getPsiClass();
        List<Element> mElements = getElements();
        // 判断是否已存在的变量
        boolean isFdExist = false;
        // 判断是否已存在setOnClickListener
        boolean isClickExist = false;
        // 判断是否存在case R.id.id:
        boolean isCaseExist = false;
        PsiField[] fields = mClass.getFields();
        // 获取initView方法的内容
        PsiStatement[] statements = Util.getInitViewBodyStatements(mClass);
        //获取ZClick注解内容
        PsiElement[] onClickStatement = null;
        List<String> psiMethodByZClickValue = Util.getPsiMethodByZClickValue(mClass);
        PsiMethod butterKnifZClick = Util.getPsiMethodByZClick(mClass);
        if (butterKnifZClick != null && butterKnifZClick.getBody() != null) {
            onClickStatement = butterKnifZClick.getBody()
                                               .getStatements();
        }
        for (Element element : mElements) {
            if (statements != null) {
                isFdExist = checkFieldExist(statements, element);
            }
            if (onClickStatement != null) {
                isCaseExist = checkCaseExist(onClickStatement, element);
            }
            if (psiMethodByZClickValue.size() > 0) {
                isClickExist = psiMethodByZClickValue.contains(element.getFullID());
            }
            setElementProperty(getElementSize(), isFdExist, isClickExist, isCaseExist, fields, element);
        }
    }

    /**
     * 判断onClick方法里面是否包含field的case
     *
     * @param onClickStatement onClick方法
     * @param element          element
     * @return boolean
     */
    private boolean checkCaseExist(PsiElement[] onClickStatement, Element element) {
        String cass = "case " + element.getFullID() + ":";
        for (PsiElement psiElement : onClickStatement) {
            if (psiElement instanceof PsiSwitchStatement) {
                PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) psiElement;
                // 获取switch的内容
                PsiCodeBlock psiSwitchStatementBody = psiSwitchStatement.getBody();
                if (psiSwitchStatementBody != null) {
                    for (PsiStatement statement : psiSwitchStatementBody.getStatements()) {
                        if (statement.getText()
                                     .replace("\n", "")
                                     .replace("break;", "")
                                     .equals(cass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断initView方法里面是否包含field的findViewById
     *
     * @param statements initView方法
     * @param element    element
     * @return boolean
     */
    private boolean checkFieldExist(PsiStatement[] statements, Element element) {
        for (PsiStatement statement : statements) {
            if (statement.getText()
                         .contains(element.getFieldName())
                    && statement.getText()
                                .contains("findViewById(" + element.getFullID() + ");")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 为已存在的变量设置checkbox
     *
     * @param mElementSize mElementSize
     * @param isFdExist    判断是否已存在的变量
     * @param isClickExist 判断是否已存在setOnClickListener
     * @param isCaseExist  判断是否存在case R.id.id:
     * @param fields       fields
     * @param element      element
     */
    private void setElementProperty(int mElementSize, boolean isFdExist, boolean isClickExist,
                                    boolean isCaseExist, PsiField[] fields, Element element) {
        for (PsiField field : fields) {
            String name = field.getName();
            if (name != null && name.equals(element.getFieldName()) && isFdExist) {
                // 已存在的变量设置checkbox为false
                element.setEnable(false);
                mElementSize = mElementSize - 1;
                setElementSize(mElementSize);
                if (element.isClickEnable() && (!isClickExist || !isCaseExist)) {
                    element.setClickable(true);
                    element.setEnable(true);
                    mElementSize = getElementSize() + 1;
                    setElementSize(mElementSize);
                }
                break;
            }
        }
    }
}
