package utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction.Simple;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import constant.Constant;
import entitys.Element;
import views.GenerateDialog;

/**
 * 生成代码
 */
public class GenerateCreator extends Simple {

    private List<Element> mOnClickList = new ArrayList<>();
    private final GenerateDialog    mDialog;
    private final Editor            mEditor;
    private final PsiFile           mFile;
    private final Project           mProject;
    private final PsiClass          mClass;
    private final List<Element>     mElements;
    private final PsiElementFactory mFactory;
    private final String            mSelectedText;
    private final int               mFileType;

    /**
     * Builder模式
     */
    public static class Builder {

        private       GenerateDialog    mDialog;
        private       Editor            mEditor;
        private       PsiFile           mFile;
        private       Project           mProject;
        private       PsiClass          mClass;
        private final String            mCommand;
        private       List<Element>     mElements;
        private       PsiElementFactory mFactory;
        private       String            mSelectedText;
        private       int               mFileType;

        public Builder(String mCommand) {
            this.mCommand = mCommand;
        }

        public Builder setDialog(GenerateDialog mDialog) {
            this.mDialog = mDialog;
            return this;
        }

        public Builder setEditor(Editor mEditor) {
            this.mEditor = mEditor;
            return this;
        }

        public Builder setFile(PsiFile mFile) {
            this.mFile = mFile;
            return this;
        }

        public Builder setProject(Project mProject) {
            this.mProject = mProject;
            return this;
        }

        public Builder setClass(PsiClass mClass) {
            this.mClass = mClass;
            return this;
        }

        public Builder setElements(List<Element> mElements) {
            this.mElements = mElements;
            return this;
        }

        public Builder setFactory(PsiElementFactory mFactory) {
            this.mFactory = mFactory;
            return this;
        }

        public Builder setSelectedText(String mSelectedText) {
            this.mSelectedText = mSelectedText;
            return this;
        }

        public Builder setFileType(int mFileType) {
            this.mFileType = mFileType;
            return this;
        }

        public GenerateCreator build() {
            return new GenerateCreator(this);
        }
    }

    private GenerateCreator(Builder builder) {
        super(builder.mClass.getProject(), builder.mCommand);
        mDialog = builder.mDialog;
        mEditor = builder.mEditor;
        mFile = builder.mFile;
        mClass = builder.mClass;
        mProject = builder.mProject;
        mElements = builder.mElements;
        mFactory = builder.mFactory;
        mSelectedText = builder.mSelectedText;
        mFileType = builder.mFileType;
        // 添加有onclick的list
        mOnClickList.addAll(mElements.stream()
                                     .filter(element -> element.isEnable() && element.isClickable())
                                     .collect(Collectors.toList()));
    }

    @Override
    protected void run() throws Throwable {
        try {
            generateFindViewById();
        } catch (Exception e) {
            // 异常打印
            mDialog.cancelDialog();
            Util.showPopupBalloon(mEditor, e.getMessage(), 10);
            return;
        }
        // 重写class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
        //        Util.showPopupBalloon(mEditor, Constant.actions.selectedSuccess, 5);
    }

    /**
     * 设置变量的值FindViewById，Activity和Fragment
     */
    private void generateFindViewById() {
        if (mFileType == 0) {
            generateFindViewByIdFields();
            // 判断是否有onCreate方法
            if (mClass.findMethodsByName(Constant.psiMethodByOnCreate, false).length == 0) {
                mClass.add(mFactory.createMethodFromText(Util.createOnCreateMethod(mSelectedText), mClass));
            } else {
                PsiStatement setContentViewStatement = null; // 获取setContentView
                boolean hasInitViewStatement = false;// onCreate是否存在initView方法
                PsiMethod onCreate = mClass.findMethodsByName(Constant.psiMethodByOnCreate, false)[0];
                if (onCreate.getBody() != null) {
                    for (PsiStatement psiStatement : onCreate.getBody()
                                                             .getStatements()) {
                        // 查找setContentView
                        if (psiStatement.getFirstChild() instanceof PsiMethodCallExpression) {
                            PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) psiStatement.getFirstChild()).getMethodExpression();
                            if (methodExpression.getText()
                                                .equals(Constant.utils.creatorSetContentViewMethod)) {
                                setContentViewStatement = psiStatement;
                            } else if (methodExpression.getText()
                                                       .contains(Constant.utils.creatorInitViewName + "()")) {
                                hasInitViewStatement = true;
                            }
                        }
                    }
                    if (setContentViewStatement == null) {
                        onCreate.getBody()
                                .add(mFactory.createStatementFromText("setContentView(R.layout." + mSelectedText + ");", mClass));
                    }

                    if (!hasInitViewStatement) {
                        // 将initView()写到setContentView()后面
                        if (setContentViewStatement != null) {
                            onCreate.getBody()
                                    .addAfter(mFactory.createStatementFromText("initView();", mClass), setContentViewStatement);
                        } else {
                            onCreate.getBody()
                                    .add(mFactory.createStatementFromText("initView();", mClass));
                        }
                    }
                }
            }
            generateFindViewByIdLayoutCode();
        } else if (mFileType == 1) {
            generateFindViewByIdFields();
            // 判断是否有createView方法
            if (mClass.findMethodsByName(Constant.psiMethodByOnCreateView, false).length == 0) {
                mClass.add(mFactory.createMethodFromText(Util.createCreateViewMethod(), mClass));
            } else {
                PsiStatement lastStatement = null;
                boolean hasInitViewStatement = false; // createView是否存在initView方法
                PsiMethod onCreate = mClass.findMethodsByName(Constant.psiMethodByOnCreateView, false)[0];
                if (onCreate.getBody() != null) {
                    for (PsiStatement psiStatement : onCreate.getBody()
                                                             .getStatements()) {
                        if (psiStatement.getText()
                                        .contains("super.createView")) {
                            // 获取view的值
                            lastStatement = psiStatement;
                        } else if (psiStatement.getText()
                                               .contains(Constant.utils.creatorInitViewName + "()")) {
                            hasInitViewStatement = true;
                        }
                    }

                    if (!hasInitViewStatement && lastStatement != null) {
                        onCreate.getBody()
                                .addBefore(mFactory.createStatementFromText("initView();", mClass), lastStatement);
                    }
                }
            }
            generateFindViewByIdLayoutCode();
        } else {
            StringBuilder builder = new StringBuilder();
            for (Element element : mElements) {
                if (element.isEnable()) {
                    builder.append(element.getFieldName());
                    builder.append(" = getView(");
                    builder.append("holder, ");
                    builder.append(element.getFullID());
                    builder.append(");\n");
                }
            }


            if (mClass.findMethodsByName("setUpData", false).length == 0) {
                StringBuilder methodBuilder = new StringBuilder();
                methodBuilder.append("@Override public void setUpData(final CommonHolder holder, int position, int viewType, ClassCourseListReply.DataBean data) {\n");
                methodBuilder.append(builder);
                methodBuilder.append("}\n");
                mClass.add(mFactory.createMethodFromText(builder.toString(), mClass));
            } else {
                PsiMethod setUpDataBody = mClass.findMethodsByName("setUpData", false)[0];
                setUpDataBody.getBody()
                             .add(mFactory.createStatementFromText(builder.toString(), mClass));
            }
        }
    }

    /**
     * 创建变量
     */
    private void generateFindViewByIdFields() {
        for (Element element : mElements) {
            // 已存在的变量就不创建
            PsiField[] fields = mClass.getFields();
            boolean duplicateField = false;
            for (PsiField field : fields) {
                String name = field.getName();
                if (name != null && name.equals(element.getFieldName())) {
                    duplicateField = true;
                    break;
                }
            }
            // 已存在跳出
            if (duplicateField) {
                continue;
            }
            // 设置变量名，获取text里面的内容
            if (element.isEnable()) {
                // 添加到class
                mClass.add(mFactory.createFieldFromText(Util.createFieldByElement(Util.createFieldText(element, mProject), element), mClass));
            }
        }
    }

    /**
     * 写initView方法
     */
    private void generateFindViewByIdLayoutCode() {
        // 判断是否已有initView方法
        PsiMethod[] initViewMethods = mClass.findMethodsByName(Constant.utils.creatorInitViewName, false);
        // 有initView方法
        if (initViewMethods.length > 0 && initViewMethods[0].getBody() != null) {
            PsiCodeBlock initViewMethodBody = initViewMethods[0].getBody();
            // 获取initView方法里面的每条内容
            PsiStatement[] statements = initViewMethodBody.getStatements();
            for (Element element : mElements) {
                if (element.isEnable()) {
                    // 判断是否已存在findViewById
                    boolean isFdExist = false;

                    StringBuilder builder = new StringBuilder(element.getFieldName());
                    builder.append(" = getView(");
                    if (mFileType == 2) {
                        builder.append("holder, ");
                    }
                    builder.append(element.getFullID());
                    builder.append(");");
                    String findViewById = builder.toString();
                    for (PsiStatement statement : statements) {
                        if (statement.getText()
                                     .equals(findViewById)) {
                            isFdExist = true;
                            break;
                        } else {
                            isFdExist = false;
                        }
                    }
                    // 不存在就添加
                    if (!isFdExist) {
                        initViewMethodBody.add(mFactory.createStatementFromText(findViewById, initViewMethods[0]));
                    }
                }
            }
        } else {
            mClass.add(mFactory.createMethodFromText(
                    Util.createFieldsByInitViewMethod(mFileType, mElements), mClass));
        }

        if (mOnClickList.size() != 0) {
            generateZClickCode();
        }
    }


    /**
     * 写onClick方法
     */
    private void generateZClickCode() {
        PsiMethod psiMethodByZClick = getPsiMethodByZClick(mClass);
        // 有@ZClick注解
        if (psiMethodByZClick != null && psiMethodByZClick.getBody() != null) {
            List<String> psiMethodZClickValue = Util.getPsiMethodByZClickValue(mClass);
            if (mOnClickList.size() != 0) {
                List<Element> clickList = new ArrayList<>();
                for (Element element : mOnClickList) {
                    boolean flag = false;
                    for (String s : psiMethodZClickValue) {
                        if (element.getFullID()
                                   .equals(s)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        clickList.add(element);
                    }
                }

                for (Element element : clickList) {
                    mClass.add(mFactory.createMethodFromText(Util.createZClickMethod(element), mClass));
                }
            }
        } else {
            if (mOnClickList.size() != 0) {
                for (Element element : mOnClickList) {
                    mClass.add(mFactory.createMethodFromText(Util.createZClickMethod(element), mClass));
                }
            }
        }
    }

    public PsiMethod getPsiMethodByZClick(PsiClass mClass) {
        for (PsiMethod psiMethod : mClass.getMethods()) {
            // 获取方法的注解
            PsiModifierList modifierList = psiMethod.getModifierList();
            PsiAnnotation[] annotations = modifierList.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();
                if (qualifiedName != null && qualifiedName.endsWith(".ZClick")) {
                    // 包含@ZClick注解
                    return psiMethod;
                }
            }
        }
        return null;
    }
}
