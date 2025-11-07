package com.example.demo.utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResponseUtils {

    /**
     * 包装响应结果为指定结构的Map
     * @param ok 响应状态（true/false）
     * @param list 数据列表
     * @return 包含"ok"和"list"的Map
     */
    public static Map<String, Object> wrapResponse(boolean ok, List<?> list) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("ok", ok);   // 放入"ok"字段
        responseMap.put("list", list); // 放入"list"字段
        return responseMap;
    }
}