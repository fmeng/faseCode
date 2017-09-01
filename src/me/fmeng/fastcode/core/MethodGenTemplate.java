package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import me.fmeng.fastcode.Conf;
import me.fmeng.fastcode.utils.PsiUtil;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class MethodGenTemplate extends AnAction implements MethodCodeGen {
    /**
     * 整个构建流程
     */
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // 1. 事件击中的方法、类
        PsiMethod psiMethod = getActionTargetMethod(anActionEvent);
        PsiClass psiClass = PsiUtil.getPsiClass(psiMethod);
        if (psiMethod == null) {
            return;
        }
        // 2. 获得生成结果
        PsiMethod resPsiMethod = createPsiElement(psiMethod);
        if (resPsiMethod == null) {
            return;
        }
        // 编辑文件需要开启线程
        new WriteCommandAction.Simple(psiMethod.getProject(), psiMethod.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                // 3. 替换方法
                replaceMethod(resPsiMethod, psiMethod);
                // 4. 优化导入
                format(psiClass);
            }
        }.execute();
    }

    @Override
    public PsiMethod getActionTargetMethod(AnActionEvent event) {
        return PsiUtil.getPsiMethod(event);
    }

    @Override
    public PsiMethod createPsiElement(PsiMethod psiMethod) {
        return doCreatePsiMethod(psiMethod);
    }

    @Override
    public void replaceMethod(PsiMethod dstPsiMethod, PsiMethod srcPsiMethod) {
        srcPsiMethod.replace(dstPsiMethod);
    }

    @Override
    public void format(PsiClass psiClass) {
        PsiUtil.ensureImport(psiClass, Conf.import_check_map);
    }

    public abstract PsiMethod doCreatePsiMethod(PsiMethod psiMethod);
}
