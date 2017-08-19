package me.fmeng.fastcode.core;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.impl.source.tree.java.JavaFileElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;

import java.util.Map;

/**
 * Created by fmeng on 19/08/2017.
 */
public interface XmlCodeGen {

    /**
     * 1. 获得事件击中的XmlElement和XmlDocument
     *
     * @param event
     * @return
     */
    public Map.Entry<XmlElement, XmlDocument> getActionTargetElement(AnActionEvent event);

    /**
     * 2. 生成要替换的XmlDocument
     *
     * @return
     */
    public JavaFileElement createPsiElement(Map.Entry<XmlElement, XmlDocument> srcPair);

    /**
     * 3. 替换javaFile
     */
    public void replaceMethod(XmlDocument dst, XmlDocument src);

    /**
     * 4. 优化导入
     */
    public void format(XmlDocument dst);
}
