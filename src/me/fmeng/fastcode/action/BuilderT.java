package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class BuilderT extends MethodGenTemplate implements LanguageSelection {

    private LanguageEnum jdkLanguage;

    @Override
    public PsiMethod doCreatePsiMethod(PsiMethod psiMethod) {
        jdkLanguage = selectCallBack();
        // params 是有序map
        Map<String, PsiClass> params = PsiUtil.getParams(psiMethod);
        if (params == null || params.size() < 2) {
            return null;
        }
        // 有序map
        Map<String, PsiClass> srcParams = PsiUtil.getSrcParams(psiMethod);
        Map.Entry<String, PsiClass> dstParam = PsiUtil.getDstParam(psiMethod);
        PsiClass dstPsiClass = dstParam.getValue();
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(dstPsiClass);
        if (dstPsiFields == null || dstPsiFields.size() == 0) {
            return null;
        }
        // initCheck
        Map<String, Boolean> dstFieldApplayCheck = new HashMap<>();
        for (PsiField dstFiled : dstPsiFields) {
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }

        // build
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        // static method
        PsiMethod staticBuildMethod = elementFactory.createMethodFromText(CodeUtil.BUILD_METHOD_STRING, dstPsiClass);
        // BuilderT Class
        PsiClass buildClass = CodeUtil.getFieldBuildClass(srcParams, dstPsiClass, this.jdkLanguage);
        List<PsiElement> resPsi = new ArrayList<>();
        resPsi.add(staticBuildMethod);
        resPsi.add(buildClass);
        return null;
    }
}
