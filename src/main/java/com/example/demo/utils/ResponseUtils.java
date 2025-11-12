package com.example.demo.utils;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {

    public static <T> Map<String, Object> wrapResponse(boolean ok, Page<T> page, int currentPage, int pageSize) {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("page", currentPage);
        data.put("pageSize", pageSize);
        data.put("totalPages", page.getTotalPages());

        response.put("ok", ok);
        response.put("data", data);
        return response;
    }

    public static Map<String, Object> wrapResponse(boolean ok, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", ok);
        response.put("data", data);
        return response;
    }
}
