package common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
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
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static String getTextFromStringsXml(PsiFile psiFile, String text) {
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

    public static String getPackageName(Project project) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            return null;
        }
        File file = new File(baseDir.getPath() + "/app/src/main/AndroidManifest.xml");
        if (file.exists()) {
            String keyWord = "package=\"";
            String str = FileUtil.readFileStr(file.getAbsolutePath());
            int index = str.indexOf(keyWord);
            if (index >= 0) {
                int lastIndex = str.indexOf("\"", keyWord.length() + index);
                if (lastIndex >= 0) {
                    return str.substring(keyWord.length() + index, lastIndex);
                }
            }
        }
        return null;
    }

    public static void openFile(Project project, VirtualFile file) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, file);
        fileEditorManager.openTextEditor(fileDescriptor, true);//Open the Contract
    }

    public static void createJavaFile(Project project, String dirPath, PsiDirectory psiDir, String fileName, String content) {
        File newFile = new File(dirPath + "/" + fileName);
        PsiPackage psiPackage = JavaDirectoryService.getInstance()
                                                    .getPackage(psiDir);
        if (psiPackage != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("package ")
                   .append(psiPackage.getQualifiedName())
                   .append(";")
                   .append("\n\n")
                   .append(content);
            FileUtil.createFile(newFile.getPath());
            FileUtil.writeFileStr(newFile.getPath(), builder.toString());

            LocalFileSystem.getInstance()
                           .refresh(true);
            VirtualFile newVirtualFile = LocalFileSystem.getInstance()
                                                        .refreshAndFindFileByIoFile(newFile);
            if (newVirtualFile != null) {
                Util.openFile(project, newVirtualFile);
                final PsiFile newPsiFile = PsiManager.getInstance(project)
                                                     .findFile(newVirtualFile);
                if (newPsiFile != null) {
                    JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                    styleManager.optimizeImports(newPsiFile);
                }
            }
        }
    }

    public static void createXMLfile(Project project, String fileName, String content) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            return;
        }
        File file = new File(baseDir.getPath() + "/app/src/main/res/layout/" + fileName);
        FileUtil.createFile(file.getPath());
        FileUtil.writeFileStr(file.getPath(), content);
    }
}
