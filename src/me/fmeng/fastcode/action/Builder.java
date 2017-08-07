package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;

import java.util.*;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class Builder extends MethodGenTemplate implements LanguageSelection{

    private LanguageEnum jdkLanguage;
    @Override
    public List<PsiElement> doCreatePsiElement(PsiMethod psiMethod) {
        jdkLanguage = selectCallBack();
        // params 是有序map
        SortedMap<String, PsiClass> params = PsiUtil.getParams(psiMethod);
        Map<String, PsiClass> srcParams = getSrcParams(params);
        Map.Entry<String, PsiClass> dstParam = getDstParam(params);
        if (params == null || params.size() < 2){
            return null;
        }
        String dstRefName = dstParam.getKey();
        PsiClass dstPsiClass = dstParam.getValue();
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(dstPsiClass);
        if (dstPsiFields == null || dstPsiFields.size() == 0){
            return null;
        }
        // initCheck
        Map<String, Boolean> dstFieldApplayCheck = new HashMap<>();
        for (PsiField dstFiled: dstPsiFields){
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }

        // static build method
        String staticBuildMethodStr = CodeUtil.getStaticBuildMethod();

        // build class (已经包含未匹配的属性注释)
        String buildClassStr = CodeUtil.getFieldBuildClass(srcParams, dstPsiClass, this.jdkLanguage);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        PsiMethod staticBuildMethod = elementFactory.createMethodFromText(staticBuildMethodStr, dstPsiClass);

        PsiMethod buildClass = elementFactory.createMethodFromText(buildClassStr, dstPsiClass);
        List<PsiElement> resPsi = new ArrayList<>();
        resPsi.add(staticBuildMethod);
        resPsi.add(buildClass);
        return resPsi;
    }

    private Map<String, PsiClass> getSrcParams(SortedMap<String, PsiClass> params){
        return params.subMap(params.firstKey(), params.lastKey());
    }

    private Map.Entry<String, PsiClass> getDstParam(SortedMap<String, PsiClass> params){
        Map.Entry<String,PsiClass> res =new AbstractMap.SimpleEntry<>(params.lastKey(), params.get(params.lastKey()));
        return res;
    }
}
