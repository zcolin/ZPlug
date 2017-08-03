package findviewbyid.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;

import org.apache.commons.lang.StringUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import findviewbyid.constant.Constant;
import findviewbyid.entitys.Element;

public class Util {
    /**
     * 通过strings.xml获取的值
     */
    private static String StringValue;

    /**
     * 显示dialog
     *
     * @param editor editor
     * @param result 内容
     * @param time   显示时间，单位秒
     */
    public static void showPopupBalloon(final Editor editor, final String result, final int time) {
        ApplicationManager.getApplication()
                          .invokeLater(() -> {
                              JBPopupFactory factory = JBPopupFactory.getInstance();
                              factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)), null)
                                     .setFadeoutTime(time * 1000)
                                     .createBalloon()
                                     .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
                          });
    }

    /**
     * 驼峰
     *
     * @param fieldName fieldName
     * @param type      type
     * @return String
     */
    public static String getFieldName(String fieldName, int type) {
        if (!StringUtils.isEmpty(fieldName)) {
            String[] names = fieldName.split("_");
            if (type == 2) {
                // aaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append(names[i]);
                    } else {
                        sb.append(Util.firstToUpperCase(names[i]));
                    }
                }
                sb.append("View");
                fieldName = sb.toString();
            } else if (type == 3) {
                // mAaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append("m");
                    }
                    sb.append(Util.firstToUpperCase(names[i]));
                }
                sb.append("View");
                fieldName = sb.toString();
            } else {
                fieldName += "_view";
            }
        }
        return fieldName;
    }

    /**
     * 第一个字母大写
     *
     * @param key key
     * @return String
     */
    public static String firstToUpperCase(String key) {
        return key.substring(0, 1)
                  .toUpperCase(Locale.CHINA) + key.substring(1);
    }

    /**
     * 解析xml获取string的值
     *
     * @param psiFile psiFile
     * @param text    text
     * @return String
     */
    private static String getTextFromStringsXml(PsiFile psiFile, String text) {
        psiFile.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    if (tag.getName()
                           .equals("string")
                            && tag.getAttributeValue("name")
                                  .equals(text)) {
                        PsiElement[] children = tag.getChildren();
                        String value = "";
                        for (PsiElement child : children) {
                            value += child.getText();
                        }
                        // value = <string name="app_name">My Application</string>
                        // 用正则获取值
                        Pattern p = Pattern.compile("<string name=\"" + text + "\">(.*)</string>");
                        Matcher m = p.matcher(value);
                        while (m.find()) {
                            StringValue = m.group(1);
                        }
                    }
                }
            }
        });
        return StringValue;
    }

    /**
     * 获取所有id
     *
     * @param file     file
     * @param elements elements
     * @return List<Element>
     */
    public static List<Element> getIDsFromLayout(final PsiFile file, final List<Element> elements) {
        // To iterate over the elements in a file
        // 遍历一个文件的所有元素
        file.accept(new XmlRecursiveElementVisitor() {

            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                // 解析Xml标签
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    // 获取Tag的名字（TextView）或者自定义
                    String name = tag.getName();
                    // 如果有include
                    if (name.equalsIgnoreCase("include")) {
                        // 获取布局
                        XmlAttribute layout = tag.getAttribute("layout", null);
                        // 获取project
                        Project project = file.getProject();
                        // 布局文件
                        XmlFile include = null;
                        PsiFile[] psiFiles = new PsiFile[0];
                        if (layout != null) {
                            psiFiles = FilenameIndex.getFilesByName(project, getLayoutName(layout.getValue()) + Constant.selectedTextSUFFIX, GlobalSearchScope.allScope(project));
                        }
                        if (psiFiles.length > 0) {
                            include = (XmlFile) psiFiles[0];
                        }
                        if (include != null) {
                            // 递归
                            getIDsFromLayout(include, elements);
                            return;
                        }
                    }
                    // 获取id字段属性
                    XmlAttribute id = tag.getAttribute("android:id", null);
                    if (id == null) {
                        return;
                    }
                    // 获取id的值
                    String idValue = id.getValue();
                    if (idValue == null) {
                        return;
                    }
                    XmlAttribute aClass = tag.getAttribute("class", null);
                    if (aClass != null) {
                        name = aClass.getValue();
                    }
                    // 获取clickable
                    XmlAttribute clickableAttr = tag.getAttribute("android:clickable", null);
                    boolean clickable = false;
                    if (clickableAttr != null && !StringUtils.isEmpty(clickableAttr.getValue())) {
                        clickable = clickableAttr.getValue()
                                                 .equals("true");
                    }
                    if (!StringUtils.isEmpty(name) && name.equals("Button")) {
                        clickable = true;
                    }
                    // 添加到list
                    try {
                        Element e = new Element(name, idValue, clickable, tag);
                        elements.add(e);
                    } catch (IllegalArgumentException ignored) {

                    }
                }
            }
        });


        return elements;
    }

    /**
     * layout.getValue()返回的值为@layout/layout_view
     *
     * @param layout layout
     * @return String
     */
    private static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }

        // @layout layout_view
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        // layout_view
        return parts[1];
    }

    /**
     * 根据当前文件获取对应的class文件
     *
     * @param editor editor
     * @param file   file
     * @return PsiClass
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel()
                           .getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    /**
     * 创建onCreate方法
     *
     * @param mSelectedText mSelectedText
     * @return String
     */
    public static String createOnCreateMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override\nprotected void onCreate(Bundle savedInstanceState) {\n");
        method.append("\tsuper.onCreate(savedInstanceState);\n");
        method.append("\tsetContentView(R.layout." + mSelectedText + ");\n");
        method.append("\tinitView();\n");
        method.append("}\n");
        return method.toString();
    }


    /**
     * 创建onCreateView方法
     *
     * @return String
     */
    public static String createCreateViewMethod() {
        StringBuilder method = new StringBuilder();
        method.append("@Override\nprotected void createView(@Nullable Bundle savedInstanceState) {\n");
        method.append("\tinitView();\n");
        method.append("\tsuper.createView(savedInstanceState);\n");
        method.append("}\n");
        return method.toString();
    }

    public static String createGetRootLayoutId(String fileName) {
        StringBuilder method = new StringBuilder();
        method.append("@Override\nprotected int getRootViewLayId() {\n");
        method.append("\treturn R.layout.");
        method.append(fileName);
        method.append("\n}\n");
        return method.toString();
    }

    public static String createGetItemLayoutId(String fileName) {
        StringBuilder method = new StringBuilder();
        method.append("@Override\npublic int getItemLayoutId(int viewType) {\n");
        method.append("\treturn R.layout.");
        method.append(fileName);
        method.append("\n}\n");
        return method.toString();
    }

    public static String createSetUpData(List<Element> mElements, String dataType) {
        StringBuilder builder = new StringBuilder();
        for (Element element : mElements) {
            if (element.isEnable()) {
                builder.append("\t");
                builder.append(element.getName());
                builder.append(" ");
                builder.append(element.getFieldName());
                builder.append(" = getView(");
                builder.append("holder, ");
                builder.append(element.getFullID());
                builder.append(");\n");
            }
        }
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("@Override\npublic void setUpData(final CommonHolder holder, int position, int viewType, ");
        methodBuilder.append(dataType);
        methodBuilder.append(" data) {\n");
        methodBuilder.append(builder);
        methodBuilder.append("}\n");
        return methodBuilder.toString();
    }

    /**
     * 获取initView方法里面的每条数据
     *
     * @param mClass mClass
     * @return PsiStatement[]
     */
    public static PsiStatement[] getInitViewBodyStatements(PsiClass mClass) {
        // 获取initView方法
        PsiMethod[] method = mClass.findMethodsByName(Constant.utils.creatorInitViewName, false);
        PsiStatement[] statements = null;
        if (method.length > 0 && method[0].getBody() != null) {
            PsiCodeBlock methodBody = method[0].getBody();
            statements = methodBody.getStatements();
        }
        return statements;
    }

    /**
     * 获取包含@ZClick注解的方法
     *
     * @param mClass mClass
     * @return PsiMethod
     */
    public static PsiMethod getPsiMethodByZClick(PsiClass mClass) {
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

    /**
     * 获取包含@ZClick注解里面的值
     *
     * @return List<String>
     */
    public static List<String> getPsiMethodByZClickValue(PsiClass mClass) {
        List<String> onClickValue = new ArrayList<>();
        PsiMethod butterKnifeOnClickMethod = Util.getPsiMethodByZClick(mClass);
        if (butterKnifeOnClickMethod != null) {// 获取方法的注解
            PsiModifierList modifierList = butterKnifeOnClickMethod.getModifierList();
            PsiAnnotation[] annotations = modifierList.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                if (annotation.getQualifiedName() != null && annotation.getQualifiedName()
                                                                       .endsWith(".ZClick")) {
                    String text = annotation.getText()
                                            .replace("(", "")
                                            .replace(")", "")
                                            .replace("{", "")
                                            .replace("}", "")
                                            .replace(" ", "")
                                            .replace("@ZClick", "");
                    if (!StringUtils.isEmpty(text)) {
                        String[] split = text.split(",");
                        for (String value : split) {
                            if (!StringUtils.isEmpty(value)) {
                                onClickValue.add(value);
                            }
                        }
                    }
                    break;
                }
            }
        }
        return onClickValue;
    }


    /**
     * 根据方法名获取方法
     *
     * @param mClass     mClass
     * @param methodName methodName
     * @return PsiMethod
     */
    static PsiMethod getPsiMethodByName(PsiClass mClass, String methodName) {
        for (PsiMethod psiMethod : mClass.getMethods()) {
            if (psiMethod.getName()
                         .equals(methodName)) {
                return psiMethod;
            }
        }
        return null;
    }

    /**
     * 获取View类型的变量名
     *
     * @param mClass mClass
     * @return String
     */
    static String getPsiMethodParamsViewField(PsiClass mClass) {
        PsiMethod butterKnifeOnClickMethod = getPsiMethodByZClick(mClass);
        if (butterKnifeOnClickMethod != null) {
            // 获取方法的指定参数类型的变量名
            PsiParameterList parameterList = butterKnifeOnClickMethod.getParameterList();
            PsiParameter[] parameters = parameterList.getParameters();
            for (PsiParameter parameter : parameters) {
                if (parameter.getTypeElement() != null && parameter.getTypeElement()
                                                                   .getText()
                                                                   .equals("View")) {
                    return parameter.getName();
                }
            }
        }
        return null;
    }

    /**
     * 获取OnClickList里面的id集合
     *
     * @param mOnClickList clickable的Element集合
     * @return List<String>
     */
    static List<String> getOnClickListById(List<Element> mOnClickList) {
        return mOnClickList.stream()
                           .map(Element::getFullID)
                           .collect(Collectors.toList());
    }

    /**
     * 获取注解里面跟OnClickList的id集合
     *
     * @param annotationList OnClick注解里面的id集合
     * @param onClickIdList  clickable的Element集合
     * @return List<String>
     */
    static List<String> createOnClickValue(List<String> annotationList, List<String> onClickIdList) {
        for (String value : onClickIdList) {
            if (!annotationList.contains(value)) {
                annotationList.add(value);
            }
        }
        return annotationList;
    }

    /**
     * FindViewById，获取xml里面的text
     *
     * @param element  Element
     * @param mProject Project
     * @return String
     */
    public static String getFieldComments(Element element, Project mProject) {
        String text = element.getXml()
                             .getAttributeValue("android:text");
        if (StringUtils.isEmpty(text)) {
            // 如果是text为空，则获取hint里面的内容
            text = element.getXml()
                          .getAttributeValue("android:hint");
        }
        // 如果是@string/app_name类似
        if (!StringUtils.isEmpty(text) && text.contains("@string/")) {
            text = text.replace("@string/", "");
            // 获取strings.xml
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(mProject, "strings.xml", GlobalSearchScope.allScope(mProject));
            if (psiFiles.length > 0) {
                for (PsiFile psiFile : psiFiles) {
                    // 获取src\main\res\values下面的strings.xml文件
                    if (psiFile.getParent() != null && psiFile.getParent()
                                                              .toString()
                                                              .contains("src\\main\\res\\values")) {
                        text = Util.getTextFromStringsXml(psiFile, text);
                    }
                }
            }
        }
        return text;
    }

    /**
     * FindViewById，创建字段
     *
     * @param text    注释内容
     * @param element Element
     * @return String
     */
    public static String createFieldByElement(String text, Element element) {
        StringBuilder fromText = new StringBuilder();
        fromText.append("private ");
        fromText.append(element.getName());
        fromText.append(" ");
        fromText.append(element.getFieldName());
        fromText.append(";");
        if (!StringUtils.isEmpty(text)) {
            fromText.append("//");
            fromText.append(text);
        }
        fromText.append("\n");
        return fromText.toString();
    }

    /**
     * FindViewById，创建findViewById代码到initView方法里面
     *
     * @param mElements Element的List
     * @return String
     */
    public static String createFieldsByInitViewMethod(List<Element> mElements) {
        StringBuilder initView = new StringBuilder();
        initView.append("private void initView() {\n");
        for (Element element : mElements) {
            if (element.isEnable()) {
                initView.append("\t");
                initView.append(element.getFieldName());
                initView.append(" = getView(");
                initView.append(element.getFullID());
                initView.append(");\n");
            }
        }
        initView.append("}\n");
        return initView.toString();
    }

    /**
     * ZClick方法
     *
     * @param mOnClick 可onclick的Element
     * @return String
     */
    public static String createZClickMethod(Element mOnClick) {
        StringBuilder onClick = new StringBuilder();
        onClick.append("@ZClick(");
        onClick.append(mOnClick.getFullID());
        onClick.append(")\n");
        onClick.append("public void on");
        onClick.append(mOnClick.getFirstUpperCaseFieldName());
        onClick.append("Click(View v) {\n");
        onClick.append("}\n");
        return onClick.toString();
    }

    /**
     * 判断mClass是继承的Activity还是Fragment还是View
     *
     * @return 0 Activity  1Framgnet 2Adapter 3 其他
     */
    public static int getFileType(Project mProject, PsiClass mClass) {
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject)
                                              .findClass("android.support.v7.app.AppCompatActivity", new EverythingGlobalScope(mProject));
        PsiClass fragmentClass = JavaPsiFacade.getInstance(mProject)
                                              .findClass("android.support.v4.app.Fragment", new EverythingGlobalScope(mProject));
        PsiClass adapterClass = JavaPsiFacade.getInstance(mProject)
                                             .findClass("android.support.v7.widget.RecyclerView", new EverythingGlobalScope(mProject));

        if (activityClass != null && mClass.isInheritor(activityClass, true)) {
            return 0;
        } else if (fragmentClass != null && mClass.isInheritor(fragmentClass, true)) {
            return 1;
        } else if (adapterClass != null && mClass.isInheritor(adapterClass, true)) {
            return 2;
        }
        return 3;
    }
}
