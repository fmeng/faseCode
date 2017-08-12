package me.fmeng.fastcode.core;

import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import me.fmeng.fastcode.utils.PsiUtil;

import java.util.List;
import java.util.SortedMap;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class MethodGenTemplate extends AnAction implements CodeGen {

    private static final SortedMap<String, String> importMap = Maps.newTreeMap();
    static {
        // value可重复，插件可根据工程是否引用了Jar筛选导入；若导入了两个相关的Jar，后面的覆盖前面的
        // 例如：工程同时引入了com.google...Preconditions和com.qunar...Preconditions,会导入后者
        importMap.put("com.google.common.base.Preconditions","Preconditions");
        importMap.put("com.qunar.hotel.ihotel.common.base.Preconditions","Preconditions");
        importMap.put("org.apache.commons.lang.StringUtils","StringUtils");
        importMap.put("org.apache.commons.lang3.ArrayUtils","ArrayUtil");
        importMap.put("org.apache.commons.collections4.CollectionUtils","CollectionUtils");
        importMap.put("org.apache.commons.collections4.MapUtils;","MapUtils");
    }

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
        List<PsiElement> psiElements = createPsiElement(psiMethod);
        if (psiElements == null || psiElements.size() == 0) {
            return;
        }
        // 编辑文件需要开启线程
        new WriteCommandAction.Simple(psiMethod.getProject(), psiMethod.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                // 3. 替换方法
                replaceMethod(psiElements, psiMethod);
                // 4. 优化倒入
                format(psiClass);
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
    public void replaceMethod(List<PsiElement> psiElements, PsiMethod srcPsiMethod) {
        PsiClass psiClass = PsiUtil.getPsiClass(srcPsiMethod);
        if (psiElements != null && psiElements.size() > 0) {
            PsiElement latestPsiElement = srcPsiMethod;
            for (PsiElement ipsiElemet : psiElements) {
                // 逐条添加Element
                psiClass.addAfter(ipsiElemet, latestPsiElement);
                latestPsiElement = ipsiElemet;
            }
        }
        // 删除原方法
        psiClass.deleteChildRange(srcPsiMethod, srcPsiMethod);
    }

    @Override
    public void format(PsiClass psiClass) {
        PsiUtil.ensureImport(psiClass, importMap);
    }

    public abstract List<PsiElement> doCreatePsiElement(PsiMethod psiMethod);
}
