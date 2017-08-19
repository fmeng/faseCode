package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.impl.source.tree.java.JavaFileElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;

import java.util.Map;

/**
 * Created by fmeng on 20/08/2017.
 */
public class XmlCodeGenTemplate extends AnAction implements XmlCodeGen {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }

    @Override
    public Map.Entry<XmlElement, XmlDocument> getActionTargetElement(AnActionEvent event) {
        return null;
    }

    @Override
    public JavaFileElement createPsiElement(Map.Entry<XmlElement, XmlDocument> srcPair) {
        return null;
    }

    @Override
    public void replaceMethod(XmlDocument dst, XmlDocument src) {

    }

    @Override
    public void format(XmlDocument dst) {

    }
}
