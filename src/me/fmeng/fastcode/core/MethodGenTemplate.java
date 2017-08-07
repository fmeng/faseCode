package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import me.fmeng.fastcode.utils.PsiUtil;

import java.util.List;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class MethodGenTemplate extends AnAction implements CodeGen {

    /**
     * 整个构建流程
     */
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // 1. 事件击中的方法
        PsiMethod psiMethod = getActionTargetMethod(anActionEvent);
        if (psiMethod == null){
            return;
        }
        // 2. 获得生成结果
        List<PsiElement> psiElements = createPsiElement(psiMethod);
        if (psiElements == null || psiElements.size() == 0){
            return;
        }
        // 3. 替换方法
        new WriteCommandAction.Simple(psiMethod.getProject(), psiMethod.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                replaceMethod(psiElements, psiMethod);
            }
        }.execute();
        replaceMethod(psiElements, psiMethod);
        // 4. 优化倒入，格式化代码
        new WriteCommandAction.Simple(psiMethod.getProject(), psiMethod.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                format(PsiUtil.getPsiClass(psiMethod));
            }
        }.execute();
    }

    @Override
    public PsiMethod getActionTargetMethod(AnActionEvent event) {
        return PsiUtil.getPsiMethod(event);
    }

    @Override
    public List<PsiElement> createPsiElement(PsiMethod psiMethod) {
        return doCreatePsiElement(psiMethod);
    }

    @Override
    public void replaceMethod(List<PsiElement> psiElements, PsiMethod psiMethod) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        PsiClass psiClass = PsiUtil.getPsiClass(psiMethod);
        if (psiElements !=null && psiElements.size() > 0){
            for (PsiElement ipsiElemet : psiElements){
                PsiMethod toPsiMethod = elementFactory.createMethodFromText(ipsiElemet.getText(), psiClass);
                psiClass.replace(toPsiMethod);
            }
        }

    }
    @Override
    public void format(PsiClass psiClass){
        //TODO
    }

    public abstract List<PsiElement> doCreatePsiElement(PsiMethod psiMethod);
}
