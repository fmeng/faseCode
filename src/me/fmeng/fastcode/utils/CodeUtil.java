package me.fmeng.fastcode.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import me.fmeng.fastcode.action.LanguageSelection;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public class CodeUtil {
    private static final String BUILD_METHOD_STRING
            = "public static Builder builder() {\n"
            +    "return new Builder();\n"
            +  "}";

    private CodeUtil(){}

    public static String wraprMethod(PsiMethod psiMethod, String innerCode){
        String name = psiMethod.getName();
        String params = getParam(psiMethod);
        String returnType = getReturnType(psiMethod);
        StringBuilder res = new StringBuilder();
        res.append("public static ").append(returnType).append(" ").append(name)
                .append("(").append(params).append("){")
                .append(innerCode).append("}\n");
        return res.toString();
    }
    public static String getStaticBuildMethod(){
        return BUILD_METHOD_STRING;
    }

    public static String getNewInstance(String className){
        StringBuilder res = new StringBuilder();
        res.append(className).append(" res = new ").append(className).append("();\n");
        return res.toString();
    }

    public static String getFieldBuildClass(Map<String, PsiClass> params, PsiClass dstPsiClass, LanguageSelection.LanguageEnum languageEnum){
        StringBuilder res = new StringBuilder();
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(dstPsiClass);
        if (dstPsiFields==null || dstPsiFields.size() == 0){
            return null;
        }
        res.append("static class Builder {\nprivate ")
                .append(dstPsiClass.getName()).append("res;\n\nBuilder() {\nthis.res = new ")
                .append(dstPsiClass.getName()).append("();\n}\n\n");
        Iterator<String> it = params.keySet().iterator();
        // initCheck
        Map<String, Boolean> dstFieldApplayCheck = new HashMap<>();
        for (PsiField dstFiled: dstPsiFields){
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }
        while (it.hasNext()){
            String srcRefName = it.next();
            PsiClass srcPsiClass = params.get(srcRefName);
            res.append("public Builder ").append(srcRefName).append("(String ").append(srcRefName).append(") {\n");
            List<PsiField> srcPsiFields = PsiUtil.getPsiFields(srcPsiClass);
            for (PsiField idstPsiFiled : dstPsiFields){
                String dstFiledName = idstPsiFiled.getName();
                String dstSetName = PsiUtil.getSetterName(dstFiledName, dstPsiClass);
                String dstRefName = "res";
                String srcGetName = PsiUtil.getGetterName(dstFiledName, srcPsiClass);
                if (StringUtils.isNotBlank(srcGetName)){
                    if (LanguageSelection.LanguageEnum.JDK7 == languageEnum){
                        String code = getIfSetCodeJava7(srcRefName, srcGetName, dstRefName, dstSetName);
                        res.append(code);
                    }else if (LanguageSelection.LanguageEnum.JDK8 == languageEnum){
                        String code = getSetCodeJava8(dstFiledName, srcRefName, srcGetName, dstRefName, dstSetName);
                        res.append(code);
                    }else {
                       throw new IllegalArgumentException("JDK语言类型不支持");
                    }
                    dstFieldApplayCheck.put(dstFiledName, Boolean.TRUE);
                    break;// 找到属性跳出内循环
                }
            }
            res.append("return this;\n}\n\n");
        }
        // build method
        res.append("public ").append(dstPsiClass.getName()).append(" build() {\n")
                .append("// TODO 校验\n").append("return res;\n}\n");
        // unchecked filed
        String check = getUnMathedFiledComment(dstFieldApplayCheck);
        res.append(check);
        return null;
    }

    public static String getUnMathedFiledComment(Map<String, Boolean> check){
        StringBuilder comment = new StringBuilder();
        for (String ifiledName : check.keySet()){
            Boolean isMath = check.get(ifiledName);
            if (!isMath){
                comment.append("\n//").append(ifiledName);
            }
        }
        return comment.toString().isEmpty()?null:comment.toString();
    }

    public static String getIfSetCodeJava7(String srcRefName, String srcGetName, String dstRefName, String dstSetName){
        if (StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("if(").append(srcRefName).append(".").append(srcGetName).append("() != null){\n")
                .append(dstRefName).append(".").append(dstSetName).append("(").append(srcRefName).append(".").append(srcGetName).append("());\n")
                .append("}");
        return sb.toString();
    }

    public static String getSetCodeJava7(String srcRefName, String srcGetName, String dstRefName, String dstSetName){
        if (StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(dstRefName).append(".").append(dstSetName).append("(").append(srcRefName).append(".").append(srcGetName).append("());\n");
        return sb.toString();
    }

    public static String getIfSetCodeJava8(String propName, String srcRefName, String srcGetName, String dstRefName, String dstSetName){
        if (StringUtils.isBlank(propName)
                || StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Optional.ofNullable(").append(srcRefName).append(".").append(srcGetName).append("())\n")
                .append(".ifPresent(").append("map").append(firstCharUpperCase(propName))
                .append(" -> ").append(dstRefName).append(".").append(dstSetName)
                .append("(map").append(firstCharUpperCase(propName)).append("));");
        return sb.toString();
    }

    public static String getSetCodeJava8(String propName, String srcRefName, String srcGetName, String dstRefName, String dstSetName){
        if (StringUtils.isBlank(propName)
                || StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Optional.ofNullable(").append(srcRefName).append(".").append(srcGetName).append("())\n")
                .append(".map(").append("map").append(firstCharUpperCase(propName))
                .append(" -> ").append(dstRefName).append(".").append(dstSetName)
                .append("(map").append(firstCharUpperCase(propName)).append("));");
        return sb.toString();
    }

    public static String checkParamCodeJava7(Class clazz, String ... params){
        if (clazz == null
                || params == null
                || params.length < 0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (clazz.isAssignableFrom(String.class)){
            for (String ip : params){
                sb.append(checkParamCodeStringJava7(ip)).append("\n");
            }
            return sb.toString();
        }else if (clazz.isAssignableFrom(List.class)){
            for (String ip : params){
                sb.append(checkParamCodeListJava7(ip)).append("\n");
            }
            return sb.toString();
        } else if (clazz.isAssignableFrom(Map.class)){
            for (String ip : params){
                sb.append(checkParamCodeMapJava7(ip)).append("\n");
            }
            return sb.toString();
        }
        for (String ip : params){
            sb.append(checkParamCodeDefaultJava7(ip)).append("\n");
        }
        return sb.toString();
    }

    public static String firstCharUpperCase(String oldStr) {
        return oldStr.substring(0, 1).toUpperCase() + oldStr.substring(1);
    }

    public static String firstCharLowerCase(String oldStr) {
        return oldStr.substring(0, 1).toLowerCase() + oldStr.substring(1);
    }

    private static String checkParamCodeStringJava7(String param){
        if (param == null
                || "".equals(param)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(StringUtils.isNotBlank(")
                .append(param).append("),\"String类型的%s,不能为Blank\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeListJava7(String param){
        if (param == null
                || "".equals(param)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(CollectionUtils.isNotEmpty(")
                .append(param).append("),\"List类型的%s,不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeMapJava7(String param){
        if (param == null
                || "".equals(param)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(MapUtils.isNotEmpty(")
                .append(param).append("),\"Map类型的%s,不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeDefaultJava7(String param){
        if (param == null
                || "".equals(param)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(")
                .append(param).append("!= null,\"%s不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }
    public static String getParam(PsiMethod psiMethod){
        if(psiMethod==null
                || psiMethod.getParameterList()==null
                || psiMethod.getParameterList().getParameters()==null
                || psiMethod.getParameterList().getParameters().length<1){
            return null;
        }
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter p = parameters[i];
            String type = p.getType().getCanonicalText();
            String name = p.getName();
            if(i != parameters.length-1){
                sb.append(type).append(" ").append(name).append(",");
            }else {
                sb.append(type).append(" ").append(name);
            }

        }
        return sb.toString();
    }
    public static String getReturnType(PsiMethod psiMethod){
        if(psiMethod==null){
            return null;
        }
        return psiMethod.getReturnType().getPresentableText();
    }
    public static void main(String[] args) {
        String res = getSetCodeJava8("name", "DTO", "getName", "DO", "setName");
        System.out.println(res);
        System.out.println("------");
        String str = checkParamCodeJava7(Map.class, "str");
        System.out.println(str);
    }
}