package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.java.JavaFileElement;

import java.util.Map;

/**
 * Created by fmeng on 19/08/2017.
 */
public abstract class JavaFileCodeGenTemplate extends AnAction implements JavaFileCodeGen {


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }

    @Override
    public Map.Entry<PsiMethod, JavaFileElement> getActionTargetElement(AnActionEvent event) {
        return null;
    }

    @Override
    public JavaFileElement createPsiElement(Map.Entry<PsiMethod, JavaFileElement> srcPair) {
        return null;
    }

    @Override
    public void replaceMethod(JavaFileElement dst, JavaFileElement src) {

    }

    @Override
    public void format(JavaFileElement dst) {

    }
}
