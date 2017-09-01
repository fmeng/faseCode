package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.Conf;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 14/08/2017.
 */
public abstract class CheckParam extends MethodGenTemplate implements LanguageSelection{
    private LanguageEnum jdkLanguage;

    @Override
    public PsiMethod doCreatePsiMethod(PsiMethod psiMethod) {
        // 回调子类JDK版本
        jdkLanguage = selectCallBack();
        Map<String, PsiClass> params = PsiUtil.getParams(psiMethod);
        if (params == null || params.size() == 0) {
            return null;
        }
        PsiClass thisPsiClass = PsiUtil.getPsiClass(psiMethod);
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(thisPsiClass);
        if (dstPsiFields == null || dstPsiFields.isEmpty()) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        // 是否匹配到属性，用Map标示（用于注释结果统计）
        Map<String, Boolean> dstFieldApplayCheck = new HashMap<>();
        for (PsiField dstFiled : dstPsiFields) {
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }
        // 参数非空校验
        for (String iParamName : params.keySet()) {
            PsiClass iFromPsiClass = params.get(iParamName);
            res.append(CodeUtil.checkParamCode(jdkLanguage, iFromPsiClass, iParamName));
        }
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        // 返回结果
        String newInstanceMethodString = Conf.force_name ? CodeUtil.checkParamWraprMethod(psiMethod, res.toString())
                : CodeUtil.wraprMethod(psiMethod, res.append(CodeUtil.RETURN_RES).toString());
        PsiMethod newInstanceMethod = elementFactory.createMethodFromText(newInstanceMethodString, thisPsiClass);
        return newInstanceMethod;
    }
}
