package com.example.demo.utils;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {
    public static Map<String, Object> wrapResponse(boolean ok, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", ok);
        response.put("data", data);
        return response;
    }
}
