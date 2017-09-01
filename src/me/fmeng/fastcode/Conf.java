package me.fmeng.fastcode;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by fmeng on 13/08/2017.
 */
public final class Conf {

    public static final boolean enable_lombok = Boolean.TRUE;
    public static final boolean force_name = Boolean.TRUE;
    public static final boolean enable_auto_import = Boolean.TRUE;
    public static final String auto_check_import_class = "me.fmeng.pakage, qunarp";
    public static final double str_like_weight=0.5D;
    public static final boolean enable_set_check=Boolean.TRUE;

    public static final String new_instance_fanction_name = "newInstance";
    public static final String copy_props_function_name = "copyProps";
    public static final String params_legal_status = "legalStatus";
    public static final String props_legal_status = "legalStatus";
    public static final String static_builder_method_name = "builder";
    public static final String inner_builder_class_name = "BuilderT";

    public static final boolean mybatis_enable_happy_lock = Boolean.TRUE;
    public static final String mybaits_version_name = "version";
    public static final String mybatis_update_time_name = "update_time";
    public static final String mybatis_create_time_name = "create_time";
    public static final String mybatis_id_name = "id";
    public static final String mybatis_save_name = "save";
    public static final String mybatis_update_name = "update";
    public static final String mybatis_delete_name = "delete";
    public static final String mybatis_query_name = "query";
    public static final String mybatis_batch_name = "batch";
    public static final String mybatis_mapper_path = "project/mapper/";

    /**
     * 自动导入
     */
    public static final Map<String, String> import_check_map = Maps.newLinkedHashMap();
    static {
        // value可重复，插件可根据工程是否引用了Jar筛选导入；若导入了两个相关的Jar，后面的覆盖前面的
        // 例如：工程同时引入了com.google...Preconditions和com.qunar...Preconditions,会导入后者
        import_check_map.put("com.google.common.base.Preconditions","Preconditions");
        import_check_map.put("com.qunar.hotel.ihotel.common.base.Preconditions","Preconditions");
        import_check_map.put("org.apache.commons.lang.StringUtils","StringUtils");
        import_check_map.put("org.apache.commons.lang3.ArrayUtils","ArrayUtil");
        import_check_map.put("org.apache.commons.collections4.CollectionUtils","CollectionUtils");
        import_check_map.put("org.apache.commons.collections4.MapUtils","MapUtils");
        import_check_map.put("java.util.Optional","Optional");
        import_check_map.put("java.util.Objects","Objects");
    }
}
