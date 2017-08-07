package me.fmeng.fastcode.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.CollectionListModel;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by fmeng on 06/08/2017.
 */
public class PsiUtil {
    private PsiUtil(){}

    public static String getGetterName(String fieldName, PsiClass psiClass){
        if (StringUtils.isBlank(fieldName) || psiClass == null){
            return null;
        }
        String matchingGetMethodName = "get"+CodeUtil.firstCharUpperCase(fieldName);
        String matchingIsMethodName = "is"+CodeUtil.firstCharUpperCase(fieldName);
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod iPsiMethod : methods){
            String iPsiMethodName = iPsiMethod.getName();
            if (matchingGetMethodName.equals(iPsiMethodName)){
                return matchingGetMethodName;
            }else if (matchingIsMethodName.equals(iPsiMethodName)){
                return matchingIsMethodName;
            }
        }
        return null;
    }

    public static String getSetterName(String fieldName, PsiClass psiClass){
        if (StringUtils.isBlank(fieldName) || psiClass == null){
            return null;
        }
        String matchingSetMethodName = "set"+CodeUtil.firstCharUpperCase(fieldName);
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod iPsiMethod : methods){
            String iPsiMethodName = iPsiMethod.getName();
            if (matchingSetMethodName.equals(iPsiMethodName)){
                return matchingSetMethodName;
            }
        }
        return null;
    }

    public static PsiClass getPsiClass(AnActionEvent event){
        PsiClass psiClass = PsiTreeUtil.getParentOfType(getPsiMethod(event), PsiClass.class);
        return psiClass;
    }

    public static PsiClass getPsiClass(PsiMethod psiMethod){
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
        return psiClass;
    }

    public static PsiClass getPsiClass(PsiType psiType){
        return psiType instanceof PsiClassType ? ((PsiClassType)psiType).resolve() : null;
    }

    public static PsiMethod getPsiMethod(AnActionEvent e) {
        PsiElement elementAt = getPsiElement(e);
        if (elementAt == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
    }

    public static List<PsiField> getPsiFields(PsiClass psiClass){
        List<PsiField> fields = new CollectionListModel<PsiField>(psiClass.getFields()).getItems();
        for (int i = 0; i < fields.size(); i++) {
            PsiField psiField = fields.get(i);
            if (isNotGenercModifierProperty(psiField)){
                fields.remove(i);
            }
        }
        return fields==null || fields.size()==0 ? null : fields;
    }

    public static List<PsiField> getPsiFields(PsiType psiType){
        if(psiType==null
                || psiType.equalsToText("void")){
            return null;
        }
        String classNameWithPackage = psiType.getInternalCanonicalText();
        Project project = psiType.getResolveScope().getProject();
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass psiClass = facade.findClass(classNameWithPackage, GlobalSearchScope.allScope(project));
        if (psiClass == null){
            return null;
        }
        return getPsiFields(psiClass);
    }

    public static PsiClass getReturnType(PsiMethod psiMethod){
        if(psiMethod==null){
            return null;
        }
        return getPsiClass(psiMethod.getReturnType());
    }

    public static String getMethodName(PsiMethod psiMethod){
        if(psiMethod==null){
            return null;
        }
        return psiMethod.getName();
    }

    /**
     * 返回有序的map
     */
    public static TreeMap<String, PsiClass> getParams(PsiMethod psiMethod){
        if(psiMethod==null
                || psiMethod.getParameterList()==null
                || psiMethod.getParameterList().getParameters()==null
                || psiMethod.getParameterList().getParameters().length<1){
            return null;
        }
        TreeMap<String, PsiClass> params = new TreeMap<>();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter p = parameters[i];
            if (p!=null){
                String name = p.getName();
                PsiClass type = getPsiClass(p.getType());
                params.put(name,type);
            }
        }
        return params;
    }

    private static PsiElement getPsiElement(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            e.getPresentation().setEnabled(false);
            return null;
        }
        //用来获取当前光标处的PsiElement
        int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }

    private static boolean isNotGenercModifierProperty(PsiField psiField){
        PsiModifierList modifierList = psiField.getModifierList();
        if (modifierList == null
                || modifierList.hasModifierProperty(PsiModifier.STATIC)
                || modifierList.hasModifierProperty(PsiModifier.FINAL)
                || modifierList.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {

    }
}
