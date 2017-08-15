package me.fmeng.fastcode;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by fmeng on 13/08/2017.
 */
public final class Conf {
    public static final boolean ENABLE_LOMBOK = Boolean.TRUE;
    /**
     * 强制命名
     */
    public static final boolean FORCED_NAME = Boolean.TRUE;
    public static final String NEW_INSTANCE_FUNCTION_NAME = "newInstance";
    public static final String COPY_PROPS_FUNCTION_NAME = "copyProps";
    public static final String PARAM_LEGAL_STATUS = "legalStatus";
    /**
     * 自动导入
     */
    public static final Map<String, String> IMPORT_CHECK_MAP = Maps.newLinkedHashMap();
    static {
        // value可重复，插件可根据工程是否引用了Jar筛选导入；若导入了两个相关的Jar，后面的覆盖前面的
        // 例如：工程同时引入了com.google...Preconditions和com.qunar...Preconditions,会导入后者
        IMPORT_CHECK_MAP.put("com.google.common.base.Preconditions","Preconditions");
        IMPORT_CHECK_MAP.put("com.qunar.hotel.ihotel.common.base.Preconditions","Preconditions");
        IMPORT_CHECK_MAP.put("org.apache.commons.lang.StringUtils","StringUtils");
        IMPORT_CHECK_MAP.put("org.apache.commons.lang3.ArrayUtils","ArrayUtil");
        IMPORT_CHECK_MAP.put("org.apache.commons.collections4.CollectionUtils","CollectionUtils");
        IMPORT_CHECK_MAP.put("org.apache.commons.collections4.MapUtils","MapUtils");
        IMPORT_CHECK_MAP.put("java.util.Optional","Optional");
        IMPORT_CHECK_MAP.put("java.util.Objects","Objects");
    }
}
