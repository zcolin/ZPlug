package common;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

/**
 * The base file generator, provide basic functions for child-class.
 *
 * @author Administrator
 * @since 2017/4/24.
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class JavaFileGenerator {
    //    private final org.apache.log4j.Logger logger;
    protected Project              myProject;//current java project
    protected PsiDirectory         myFileDir;//the contract package dir
    protected String               myFileName;//the prefix used to identify each other
    protected PsiElementFactory    myFactory;//the factory used to generate interface/class/innerClass/classReference
    protected JavaDirectoryService myDirectoryService;//the dirService used to generate files under particular dir(package)
    protected PsiShortNamesCache   myShortNamesCache;//used to search a class in particular scope
    protected GlobalSearchScope    myProjectScope;//just this project is enough

    public JavaFileGenerator(Project project, PsiDirectory fileDir, String fileName) {
        this.myProject = project;
        this.myFileDir = fileDir;
        this.myFileName = fileName;
        myShortNamesCache = PsiShortNamesCache.getInstance(project);
        myFactory = JavaPsiFacade.getElementFactory(project);
        myDirectoryService = JavaDirectoryService.getInstance();
        myProjectScope = GlobalSearchScope.projectScope(project);
    }

    public void generateFile(final onFileGeneratedListener listener) {
        generateFile(myFileDir, myFileName, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, listener);
    }

    /**
     * Generate a java file
     *
     * @param directory the directory to place the file
     * @param fileName  the name of the file tobe generate
     * @param type      the type of the file
     * @param listener  when the file has been generated, then the listener will be called.
     * @see JavaTemplateUtil#INTERNAL_CLASS_TEMPLATES
     */
    protected void generateFile(final PsiDirectory directory, final String fileName, final String type, final onFileGeneratedListener listener) {
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            String fixedFileName = fileName;
            PsiClass[] psiClasses = myShortNamesCache.getClassesByName(fixedFileName, myProjectScope);//NotNull
            PsiClass psiClass;
            PsiJavaFile javaFile;
            if (psiClasses.length != 0) {//if the class already exist.
                psiClass = psiClasses[0];
                javaFile = (PsiJavaFile) psiClass.getContainingFile();
                javaFile.delete();//then delete the old one
            }//and re-generate one
            psiClass = myDirectoryService.createClass(directory, fixedFileName, type);
            javaFile = (PsiJavaFile) psiClass.getContainingFile();
            PsiPackage psiPackage = myDirectoryService.getPackage(directory);
            javaFile.setPackageName(psiPackage.getQualifiedName());
            listener.onJavaFileGenerated(javaFile, psiClass);
        });
    }


    @FunctionalInterface
    public interface onFileGeneratedListener {
        /**
         * When the file has been generated, then the listener will be called.
         *
         * @param javaFile the PsiJavaFile generated just now
         * @param psiClass the corresponding PsiClass
         */
        void onJavaFileGenerated(PsiJavaFile javaFile, PsiClass psiClass);
    }
}
