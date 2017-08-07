package me.fmeng.fastcode.action;

import com.intellij.psi.*;
import me.fmeng.fastcode.core.MethodGenTemplate;
import me.fmeng.fastcode.utils.CodeUtil;
import me.fmeng.fastcode.utils.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by fmeng on 06/08/2017.
 */
public abstract class CopyValues extends MethodGenTemplate implements LanguageSelection{
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
        StringBuilder res = new StringBuilder();
        for (PsiField idstField : dstPsiFields){
            String idstFieldName = idstField.getName();
            Map.Entry<String, String> findRes = searchGetNameFromSrcParams(idstFieldName, srcParams);
            String filedName = idstFieldName;
            if (findRes!=null){
                String dstSetName = PsiUtil.getSetterName(idstFieldName, dstPsiClass);
                String srcRefName = findRes.getKey();
                String srcGetName = findRes.getValue();
                String code = genCode(filedName, srcRefName, srcGetName, dstRefName, dstSetName);
                res.append(code);
                dstFieldApplayCheck.put(filedName, Boolean.TRUE);
            }
        }
        // 注释语句
        res.append(CodeUtil.getUnMathedFiledComment(dstFieldApplayCheck));
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        String copyValuesStr = CodeUtil.wraprMethod(psiMethod,res.toString());
        PsiMethod copyValuesMethod = elementFactory.createMethodFromText(copyValuesStr, dstPsiClass);
        List<PsiElement> resPsi = new ArrayList<>();
        resPsi.add(copyValuesMethod);
        return resPsi;
    }

    private Map<String, PsiClass> getSrcParams(SortedMap<String, PsiClass> params){
        return params.subMap(params.firstKey(), params.lastKey());
    }

    private Map.Entry<String, PsiClass> getDstParam(SortedMap<String, PsiClass> params){
        Map.Entry<String,PsiClass> res =new AbstractMap.SimpleEntry<>(params.lastKey(), params.get(params.lastKey()));
        return res;
    }

    /**
     * @return key: refName; value: getName
     */
    private Map.Entry<String, String> searchGetNameFromSrcParams(String filedName, Map<String, PsiClass> srcParams){
        Iterator<String> paramIt = srcParams.keySet().iterator();
        while (paramIt.hasNext()){
            String iparam = paramIt.next();
            PsiClass iSrcPsiClass = srcParams.get(iparam);
            List<PsiField> psiFields = PsiUtil.getPsiFields(iSrcPsiClass);
            if (psiFields == null || psiFields.size() == 0){
                continue;
            }
            for (PsiField ipsiField : psiFields){
                String ipsiFieldName = ipsiField.getName();
                if(filedName.equals(ipsiFieldName)){
                    String getterName = PsiUtil.getGetterName(filedName, iSrcPsiClass);
                    if (StringUtils.isNotBlank(getterName)){
                        return new AbstractMap.SimpleEntry(iparam, getterName);
                    }
                }
            }
        }
        return null;
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
