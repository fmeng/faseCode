package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class NewInstance extends MethodGenTemplate implements LanguageSelection{
    private LanguageEnum jdkLanguage;
    @Override
    public List<PsiElement> doCreatePsiElement(PsiMethod psiMethod) {
        jdkLanguage = selectCallBack();
        Map<String, PsiClass> params = PsiUtil.getParams(psiMethod);
        if (params == null || params.size() == 0){
            return null;
        }
        PsiClass thisPsiClass = PsiUtil.getPsiClass(psiMethod);
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(thisPsiClass);
        StringBuilder res = new StringBuilder();
        // initCheck
        Map<String, Boolean> dstFieldApplayCheck = new HashMap<>();
        for (PsiField dstFiled: dstPsiFields){
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }
        // new res
        res.append(CodeUtil.getNewInstance(thisPsiClass.getName()));
        for (String iParamName : params.keySet()){
            PsiClass iFromPsiClass = params.get(iParamName);
            List<PsiField> psiFields = PsiUtil.getPsiFields(iFromPsiClass);
            for (PsiField ipsiField : psiFields){
                String fileName = ipsiField.getName();
                String thisSetName = PsiUtil.getSetterName(fileName, thisPsiClass);
                String fromGetterName = PsiUtil.getGetterName(fileName, iFromPsiClass);
                if (StringUtils.isNotBlank(fileName)
                        && StringUtils.isNotBlank(thisSetName)
                        && StringUtils.isNotBlank(fromGetterName)){
                    // 构建语句
                    String srcRefName = iParamName;
                    String srcGetName = fromGetterName;
                    String dstRefName = "res";
                    String dstSetName = thisSetName;
                    String code = genCode(fileName, srcRefName, srcGetName, dstRefName, dstSetName);
                    res.append(code);
                    dstFieldApplayCheck.put(fileName, Boolean.TRUE);
                }
            }
        }
        // 注释语句
        res.append(CodeUtil.getUnMathedFiledComment(dstFieldApplayCheck));
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        String newInstanceMethodString = CodeUtil.wraprMethod(psiMethod,res.toString());
        PsiMethod newInstanceMethod = elementFactory.createMethodFromText(newInstanceMethodString, thisPsiClass);
        List<PsiElement> resPsi = new ArrayList<>();
        resPsi.add(newInstanceMethod);
        return resPsi;
    }

    private String genCode(String filedName, String srcRefName, String srcGetName, String dstRefName, String dstSetName){
       if (LanguageEnum.JDK7 == this.jdkLanguage){
           return CodeUtil.getIfSetCodeJava7(srcRefName,srcGetName,dstRefName,dstSetName);
       }else if (LanguageEnum.JDK8 == this.jdkLanguage){
           return CodeUtil.getIfSetCodeJava8(filedName, srcRefName,srcGetName,dstRefName,dstSetName);
       }else {
           throw new IllegalArgumentException("JDK语言类型不支持");
       }
    }

}