package findviewbyid.views;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiStatement;

import java.util.List;

import findviewbyid.entitys.Element;
import findviewbyid.utils.FindViewUtil;

/**
 * FindViewByIdFromJavaDialog
 */
public class FindViewByIdFromJavaDialog extends GenerateDialog {

    /**
     * FindViewByIdFromJavaDialog
     *
     * @param builder Builder
     */
    public FindViewByIdFromJavaDialog(Builder builder) {
        super(builder);
        if (mClass != null) {
            initExist();
        }
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
        // 判断是否已存在@ZClick
        boolean isClickExist = false;
        PsiField[] fields = mClass.getFields();
        // 获取initView方法的内容
        PsiStatement[] statements = FindViewUtil.getInitViewBodyStatements(mClass);
        //获取ZClick注解内容
        List<String> psiMethodByZClickValue = FindViewUtil.getPsiMethodByZClickValue(mClass);
        for (Element element : mElements) {
            if (statements != null) {
                isFdExist = checkFieldExist(statements, element);
            }
            if (psiMethodByZClickValue.size() > 0) {
                isClickExist = psiMethodByZClickValue.contains(element.getFullID());
            }
            setElementProperty(getElementSize(), isFdExist, isClickExist, fields, element);
        }
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
                                .contains("getView(" + element.getFullID() + ");")) {
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
     * @param fields       fields
     * @param element      element
     */
    private void setElementProperty(int mElementSize, boolean isFdExist, boolean isClickExist, PsiField[] fields, Element element) {
        for (PsiField field : fields) {
            String name = field.getName();
            if (name != null && name.equals(element.getFieldName()) && isFdExist) {
                // 已存在的变量设置checkbox为false
                element.setEnable(false);
                mElementSize = mElementSize - 1;
                setElementSize(mElementSize);
                if (element.isClickEnable() && (!isClickExist)) {
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
