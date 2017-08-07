package me.fmeng.fastcode.action.jdk8;

import me.fmeng.fastcode.action.Builder;

/**
 * Created by fmeng on 06/08/2017.
 */
public class BuilderJDK8 extends Builder{
    @Override
    public LanguageEnum selectCallBack() {
        return LanguageEnum.JDK8;
    }
}
