package me.fmeng.fastcode.action;

/**
 * Created by fmeng on 06/08/2017.
 */
public interface LanguageSelection {

    /**
     * callback 方法
     * @return
     */
    public LanguageEnum selectCallBack();

    enum LanguageEnum{
        JDK7,JDK8;
    }
}
