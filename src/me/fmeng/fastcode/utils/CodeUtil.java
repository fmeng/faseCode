package me.fmeng.fastcode.utils;

import com.google.common.collect.Maps;
import com.intellij.psi.*;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import me.fmeng.fastcode.action.LanguageSelection;
import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by fmeng on 06/08/2017.
 */
public final class CodeUtil {

    /***********************常量***********************/
    public static final String BUILD_METHOD_STRING
            = "public static BuilderT builder() {\n"
            + "return new BuilderT();\n"
            + "}\n";
    public static final String RETURN_RES = "return res;\n";
    public static final String COMMENT_SLIPT = "        //";

    private CodeUtil() {
    }

    /***********************工具方法***********************/
    public static String wraprMethod(PsiMethod psiMethod, String innerCode) {
        return wraprMethod(psiMethod, null, innerCode);
    }

    public static String getComments(String str) {
        return new StringBuilder().append("        //").append(str).append("\n").toString();
    }

    public static String wraprMethod(PsiMethod psiMethod, String methodName, String innerCode) {
        StringBuilder res = new StringBuilder();
        String psiMethodText = psiMethod.getText();
        String bodyText = psiMethod.getBody().getText();
        String nameText = psiMethod.getName();
        // 1. 去除body
        psiMethodText = psiMethodText.replace(bodyText, "");
        // 2. 替换方法名
        if (CUtil.str.isNotBlank(methodName)){
            psiMethodText = psiMethodText.replace(nameText, methodName);
        }
        res.append(psiMethodText).append("{\n")
                .append(innerCode).append("}").append("\n");
        return res.toString();
    }

    public static String checkParamWraprMethod(PsiMethod psiMethod, String innerCode) {
        String params = getParamStr(psiMethod);
        String returnType = getReturnType(psiMethod);
        StringBuilder res = new StringBuilder();
        res.append(psiMethod.getModifierList().getText())
                .append(" ").append(returnType).append(" ");
        res.append(psiMethod.getName());
        String origBodyStr = psiMethod.getBody().getText();
        if (StringUtils.isNotBlank(origBodyStr)){
            // 去除开头的...{...
            origBodyStr = origBodyStr.replaceAll("^(\\s*\\{\\s*)", "");
            res.append("(").append(params).append("){")
                    .append(innerCode)
                    .append(origBodyStr).append("\n");
            return res.toString();
        }
        return psiMethod.getText();
    }

    public static String getNewInstance(String className) {
        StringBuilder res = new StringBuilder();
        res.append(className).append(" res = new ").append(className).append("();\n");
        return res.toString();
    }

    public static PsiClass getFieldBuildClass(Map<String, PsiClass> params, PsiClass dstPsiClass, LanguageSelection.LanguageEnum languageEnum) {
        List<PsiField> dstPsiFields = PsiUtil.getPsiFields(dstPsiClass);
        if (dstPsiFields == null || dstPsiFields.size() == 0) {
            return null;
        }
        // 构建PsiClass
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(dstPsiClass.getProject());
        StringBuilder classStr = new StringBuilder("static class BuilderT{\n}\n");
        PsiClass builderClass = elementFactory.createClassFromText(classStr.toString()
                , PsiUtil.getPsiJavaFile(dstPsiClass));

        // 添加属性
        PsiField resFiled = elementFactory.createFieldFromText(
                new StringBuilder("private ")
                        .append(dstPsiClass.getName())
                        .append(" res;\n")
                        .toString(),
                builderClass);
        PsiElement latestPsiElement = resFiled;
        builderClass.add(resFiled);
        // 添加无参构造器
        PsiMethod constructor = elementFactory.createMethodFromText(new StringBuilder()
                        .append("private BuilderT(){\n")
                        .append("this.res = new ").append(dstPsiClass.getName()).append("();\n")
                        .append("}")
                        .toString(),
                builderClass);
        builderClass.addAfter(constructor, latestPsiElement);
        latestPsiElement = constructor;
        // 初始化属性检查的map
        Map<String, Boolean> dstFieldApplayCheck = Maps.newHashMap();
        for (PsiField dstFiled : dstPsiFields) {
            dstFieldApplayCheck.put(dstFiled.getName(), Boolean.FALSE);
        }
        // 参数构建器
        for (String isrcParamName : params.keySet()) {
            StringBuilder bodySB = new StringBuilder();
            PsiClass isrcPsiClass = params.get(isrcParamName);
            String methodName = CodeUtil.firstCharLowerCase(isrcPsiClass.getName());
            // 构建方法Body的过程
            bodySB.append(CodeUtil.checkParamCode(languageEnum, isrcPsiClass, isrcParamName));
            if (dstPsiFields != null && !dstPsiFields.isEmpty()) {
                for (PsiField idstFieldName : dstPsiFields) {
                    String srcGetterName = PsiUtil.getGetterName(idstFieldName.getName(), isrcPsiClass);
                    String dstSetterName = PsiUtil.getSetterName(idstFieldName.getName(), dstPsiClass);
                    if (StringUtils.isNotBlank(srcGetterName)) {
                        String ifSetCode = CodeUtil.getIfSetCode(languageEnum, idstFieldName.getName(),
                                isrcParamName, srcGetterName, "res", dstSetterName);
                        bodySB.append(ifSetCode);
                        dstFieldApplayCheck.put(idstFieldName.getName(), Boolean.TRUE);
                    }
                }
            }
            // 包装方法
            StringBuilder methodSB = new StringBuilder();
            methodSB.append("public void ").append(methodName)
                    .append("(")
                    .append(isrcPsiClass.getName()).append(" ").append(isrcParamName)
                    .append("){\n").append(bodySB)
                    .append("}\n");
            // 添加到PsiClass
            PsiMethod psiMethod = elementFactory.createMethodFromText(methodSB.toString(), dstPsiClass);
            builderClass.addAfter(psiMethod, latestPsiElement);
            latestPsiElement = psiMethod;
        }
        String checkCode = CodeUtil.getUnMathedFiledComment("res", dstFieldApplayCheck);
        StringBuilder builderMethodStr = new StringBuilder();
        builderMethodStr.append("public ").append(dstPsiClass.getName()).append("build(){\n")
                .append("// checkParam of res\n")
                .append(checkCode)
                .append("return res;\n")
                .append("}\n");
        PsiMethod psiMethod = elementFactory.createMethodFromText(builderMethodStr.toString(), dstPsiClass);
        builderClass.addAfter(psiMethod, latestPsiElement);
        String text = builderClass.getText();
        System.out.print(text);
        return builderClass;
    }

    public static String getUnMathedFiledComment(String dstRefName, Map<String, Boolean> check) {
        StringBuilder comment = new StringBuilder("\n");
        for (String ifiledName : check.keySet()) {
            Boolean isMath = check.get(ifiledName);
            if (!isMath) {
                comment.append(COMMENT_SLIPT).append(ifiledName).append("\n");
            }
        }
        return comment.toString().isEmpty() ? null
                : new StringBuilder().append(COMMENT_SLIPT).append(dstRefName).append("未应用的属性:  ")
                .append(comment).toString();
    }

    public static String getIfSetCode(LanguageSelection.LanguageEnum languageEnum, String propName, String srcRefName, String srcGetName, String dstRefName, String dstSetName) {
        if (LanguageSelection.LanguageEnum.JDK7 == languageEnum) {
            return getIfSetCodeJava7(srcRefName, srcGetName, dstRefName, dstSetName);
        } else if (LanguageSelection.LanguageEnum.JDK8 == languageEnum) {
            return getIfSetCodeJava8(propName, srcRefName, srcGetName, dstRefName, dstSetName);
        } else {
            throw new IllegalClassException("未匹配到java的语言");
        }
    }

    public static String checkParamCode(LanguageSelection.LanguageEnum languageEnum, PsiClass psiClass, String... params) {
        if (LanguageSelection.LanguageEnum.JDK7 == languageEnum) {
            return checkParamCodeJava7(psiClass, params);
        } else if (LanguageSelection.LanguageEnum.JDK8 == languageEnum) {
            return checkParamCodeJava8(psiClass, params);
        } else {
            throw new IllegalClassException("未匹配到java的语言");
        }
    }

    public static String firstCharUpperCase(String oldStr) {
        return oldStr.substring(0, 1).toUpperCase() + oldStr.substring(1);
    }

    public static String firstCharLowerCase(String oldStr) {
        return oldStr.substring(0, 1).toLowerCase() + oldStr.substring(1);
    }

    public static String getReturnType(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return null;
        }
        return psiMethod.getReturnType().getPresentableText();
    }

    /***********************私有方法***********************/

    private static String getParamStr(PsiMethod psiMethod) {
        if (psiMethod == null
                || psiMethod.getParameterList() == null
                || psiMethod.getParameterList().getParameters() == null
                || psiMethod.getParameterList().getParameters().length < 1) {
            return null;
        }
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter p = parameters[i];
            String type = p.getType().getCanonicalText();
            String name = p.getName();
            if (i != parameters.length - 1) {
                sb.append(type).append(" ").append(name).append(",");
            } else {
                sb.append(type).append(" ").append(name);
            }

        }
        return sb.toString();
    }

    private static String getClassType(PsiClass psiClass) {
        final String ARRAY_CLASS_NAME = "_Dummy_.__Array__";
        String className = psiClass.getQualifiedName();
        if ("java.lang.String".equals(className)) {
            return "java.lang.String";
        } else if (ARRAY_CLASS_NAME.equals(className)) {
            return "java.lang.reflect.Array";
        } else if ("java.util.Collection".equals(className)) {
            return "java.util.Collection";
        } else if ("java.util.Map".equals(className)) {
            return "java.util.Map";
        }
        PsiClass[] psiInterfaces = psiClass.getInterfaces();
        for (PsiClass iinterface : psiInterfaces) {
            String interfaceName = iinterface.getQualifiedName();
            if ("java.lang.String".equals(interfaceName)) {
                return "java.lang.String";
            } else if (ARRAY_CLASS_NAME.equals(interfaceName)) {
                return "java.lang.reflect.Array";
            } else if ("java.util.Collection".equals(interfaceName)) {
                return "java.util.Collection";
            } else if ("java.util.Map".equals(interfaceName)) {
                return "java.util.Map";
            }
        }
        return "java.lang.Object";
    }

    private static String doCheckParamCode(String classType, LanguageSelection.LanguageEnum languageEnum, String... params) {
        if (classType == null
                || params == null
                || params.length < 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if ("java.lang.String".equals(classType)) {
            // 字符串
            for (String ip : params) {
                sb.append(checkParamCodeStringJava7(ip)).append("\n");
            }
            return sb.toString();
        } else if ("java.lang.reflect.Array".equals(classType)) {
            // 数组
            for (String ip : params) {
                sb.append(checkParamCodeArrayJava7(ip)).append("\n");
            }
            return sb.toString();
        } else if ("java.util.Collection".equals(classType)) {
            // List
            for (String ip : params) {
                sb.append(checkParamCodeListJava7(ip)).append("\n");
            }
            return sb.toString();
        } else if ("java.util.Map".equals(classType)) {
            // Map
            for (String ip : params) {
                sb.append(checkParamCodeMapJava7(ip)).append("\n");
            }
            return sb.toString();
        } else {
            // Object
            for (String ip : params) {
                if (LanguageSelection.LanguageEnum.JDK7 == languageEnum){
                    sb.append(checkParamCodeDefaultJava7(ip)).append("\n");
                }else if (LanguageSelection.LanguageEnum.JDK8 == languageEnum){
                    sb.append(checkParamCodeDefaultJava8(ip)).append("\n");
                }else {

                }
            }
        }
        return sb.toString();
    }

    private static String checkParamCodeStringJava7(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(StringUtils.isNotBlank(")
                .append(param).append("),\"String类型的%s,不能为Blank\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeArrayJava7(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(ArrayUtils.isNotEmpty(")
                .append(param).append("),\"Array类型的%s,不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeListJava7(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(CollectionUtils.isNotEmpty(")
                .append(param).append("),\"List类型的%s,不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeMapJava7(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(MapUtils.isNotEmpty(")
                .append(param).append("),\"Map类型的%s,不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeDefaultJava7(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(")
                .append(param).append("!= null,\"%s不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String checkParamCodeDefaultJava8(String param) {
        if (param == null
                || "".equals(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Preconditions.checkArgument(")
                .append("Objects.nonNull(").append(param).append("),\"%s不能为空\",\"")
                .append(param).append("\");");
        return sb.toString();
    }

    private static String getIfSetCodeJava7(String srcRefName, String srcGetName, String dstRefName, String dstSetName) {
        if (StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("if(").append(srcRefName).append(".").append(srcGetName).append("() != null){\n")
                .append(dstRefName).append(".").append(dstSetName).append("(").append(srcRefName).append(".").append(srcGetName).append("());\n")
                .append("}\n");
        return sb.toString();
    }

    private static String getIfSetCodeJava8(String propName, String srcRefName, String srcGetName, String dstRefName, String dstSetName) {
        if (StringUtils.isBlank(propName)
                || StringUtils.isBlank(srcRefName)
                || StringUtils.isBlank(srcGetName)
                || StringUtils.isBlank(dstRefName)
                || StringUtils.isBlank(dstSetName)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Optional.ofNullable(").append(srcRefName).append(".").append(srcGetName).append("())\n")
                .append(".ifPresent(").append("map").append(firstCharUpperCase(propName))
                .append(" -> ").append(dstRefName).append(".").append(dstSetName)
                .append("(map").append(firstCharUpperCase(propName)).append("));\n");
        return sb.toString();
    }

    private static String checkParamCodeJava7(PsiClass psiClass, String... params) {
        return doCheckParamCode(getClassType(psiClass), LanguageSelection.LanguageEnum.JDK7, params);
    }

    private static String checkParamCodeJava8(PsiClass psiClass, String... params) {
        return doCheckParamCode(getClassType(psiClass), LanguageSelection.LanguageEnum.JDK8, params);
    }

    private static double filedLikeWeight(String dst, String src){
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        return (1-l.distance(dst, src));
    }

    public static void main(String[] args) {
        //String res = getSetCodeJava8("name", "DTO", "getName", "DO", "setName");
        System.out.println("------");

    }
}
