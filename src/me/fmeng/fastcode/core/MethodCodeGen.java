package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * Created by fmeng on 06/08/2017.
 */
public interface MethodCodeGen {

    /**
     * 1. 获得事件击中的方法体
     * @param event
     * @return
     */
    public PsiMethod getActionTargetMethod(AnActionEvent event);

    /**
     * 2. 生成要替换的Elements
     * @return
     */
    public PsiMethod createPsiElement(PsiMethod psiMethod);

    /**
     * 3. 替换原方法
     */
    public void replaceMethod(PsiMethod dst, PsiMethod src);

    /**
     * 4. 优化导入
     */
    public void format(PsiClass psiClass);
}
