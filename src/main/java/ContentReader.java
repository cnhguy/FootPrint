import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Retrieve the contents of a project
 */
public class ContentReader implements com.intellij.openapi.roots.ContentIterator {

    private ExecutionEnvironment env;

    public ContentReader(@NotNull ExecutionEnvironment env) {
        this.env = env;
    }
    @Override
    public boolean processFile(@NotNull VirtualFile virtualFile) {
        if (!virtualFile.isDirectory()) {
            PsiFile file = PsiManager.getInstance(env.getProject()).findFile(virtualFile);
            Document document = PsiDocumentManager.getInstance(env.getProject())
                    .getDocument(file);
            if (file.getFileType() instanceof JavaFileType) {
                file.accept(new JavaRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
//                                System.out.println(element);
                        super.visitElement(element);
                    }

                    @Override
                    public void visitLocalVariable(PsiLocalVariable variable) {
                        System.out.println(variable);

//                                FieldBreakpoint breakpoint = breakpointManager.addFieldBreakpoint
//                                        (document, document.getLineNumber(1));
//                                System.out.println(breakpoint);
                        super.visitLocalVariable(variable);
                    }
                });
            }
        }
        return true;
    }
}
