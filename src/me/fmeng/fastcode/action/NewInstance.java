package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.Conf;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class NewInstance extends MethodGenTemplate implements LanguageSelection {
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
        // Model res = new Model();
        res.append(CodeUtil.getNewInstance(thisPsiClass.getQualifiedName()));
        // 属性Copy
        for (String iParamName : params.keySet()) {
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
                String thisSetName = PsiUtil.getSetterName(fileName, thisPsiClass);
                String fromGetterName = PsiUtil.getGetterName(fileName, iFromPsiClass);
                if (StringUtils.isNotBlank(fileName)
                        && StringUtils.isNotBlank(thisSetName)
                        && StringUtils.isNotBlank(fromGetterName)) {
                    // 构建语句
                    String srcRefName = iParamName;
                    String srcGetName = fromGetterName;
                    String dstRefName = "res";
                    String dstSetName = thisSetName;
                    String code = CodeUtil.getIfSetCode(jdkLanguage, fileName, srcRefName, srcGetName, dstRefName, dstSetName);
                    res.append(code);
                    dstFieldApplayCheck.put(fileName, Boolean.TRUE);
                }
            }
        }
        // 注释语句
        res.append(CodeUtil.getUnMathedFiledComment("res", dstFieldApplayCheck));
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        // 返回结果
        String newInstanceMethodString = Conf.force_name ? CodeUtil.wraprMethod(psiMethod, Conf.new_instance_fanction_name, res.append(CodeUtil.RETURN_RES).toString())
                : CodeUtil.wraprMethod(psiMethod, res.append(CodeUtil.RETURN_RES).toString());
        PsiMethod newInstanceMethod = elementFactory.createMethodFromText(newInstanceMethodString, thisPsiClass);
        return newInstanceMethod;
    }

}
