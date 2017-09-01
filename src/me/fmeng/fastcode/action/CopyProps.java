package me.fmeng.fastcode.action;

import com.google.common.collect.Maps;
import com.intellij.psi.*;
import me.fmeng.fastcode.Conf;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class CopyProps extends MethodGenTemplate implements LanguageSelection {
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
        String dstRefName = dstParam.getKey();
        PsiClass dstPsiClass = dstParam.getValue();
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(dstPsiClass);
        if (dstPsiFields == null || dstPsiFields.size() == 0) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        // 参数非空校验
        for (String iParamName : params.keySet()) {
            PsiClass iFromPsiClass = params.get(iParamName);
            res.append(CodeUtil.checkParamCode(jdkLanguage,iFromPsiClass, iParamName));
        }
        // 初始化属性检查的map
        Map<String, Boolean> dstFieldApplayCheck = Maps.newHashMap();
        for (PsiField dstFiled : dstPsiFields) {
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }
        // 属性Copy
        for (String iParamName : srcParams.keySet()) {
            // 按参数循环
            // 每个参数添加注释
            res.append(CodeUtil.getComments(iParamName));
            PsiClass iFromPsiClass = params.get(iParamName);
            List<PsiField> psiFields = PsiUtil.getPsiFields(iFromPsiClass);
            if (psiFields == null || psiFields.isEmpty()) {
                continue;
            }
            // 按参数的属性循环
            for (PsiField ipsiField : psiFields) {
                String fileName = ipsiField.getName();
                String thisSetName = PsiUtil.getSetterName(fileName, dstPsiClass);
                String fromGetterName = PsiUtil.getGetterName(fileName, iFromPsiClass);
                if (StringUtils.isNotBlank(fileName)
                        && StringUtils.isNotBlank(thisSetName)
                        && StringUtils.isNotBlank(fromGetterName)) {
                    // 构建语句
                    String srcRefName = iParamName;
                    String srcGetName = fromGetterName;
                    String dstSetName = thisSetName;
                    String code = CodeUtil.getIfSetCode(jdkLanguage, fileName, srcRefName, srcGetName, dstRefName, dstSetName);
                    res.append(code);
                    dstFieldApplayCheck.put(fileName, Boolean.TRUE);
                }
            }
        }
        // 注释语句
        res.append(CodeUtil.getUnMathedFiledComment(dstRefName, dstFieldApplayCheck));
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        // 构建方法
        String copyValuesStr = Conf.force_name ? CodeUtil.wraprMethod(psiMethod, Conf.copy_props_function_name, res.toString())
                : CodeUtil.wraprMethod(psiMethod, res.toString());
        PsiMethod copyValuesMethod = elementFactory.createMethodFromText(copyValuesStr, dstPsiClass);
        return copyValuesMethod;
    }
}
