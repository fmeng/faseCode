package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.java.JavaFileElement;

import java.util.Map;

/**
 * Created by fmeng on 19/08/2017.
 */
public interface JavaFileCodeGen {

    /**
     * 1. 获得事件击中的方法体和javaFile
     *
     * @param event
     * @return
     */
    public Map.Entry<PsiMethod, JavaFileElement> getActionTargetElement(AnActionEvent event);

    /**
     * 2. 生成要替换的JavaFile
     *
     * @return
     */
    public JavaFileElement createPsiElement(Map.Entry<PsiMethod, JavaFileElement> srcPair);

    /**
     * 3. 替换javaFile
     */
    public void replaceMethod(JavaFileElement dst, JavaFileElement src);

    /**
     * 4. 优化导入
     */
    public void format(JavaFileElement dst);
}
