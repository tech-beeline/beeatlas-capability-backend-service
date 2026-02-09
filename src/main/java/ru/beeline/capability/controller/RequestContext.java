/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.*;

public class RequestContext {
    private static final ThreadLocal<Map<String, Object>> headersThreadLocal = new ThreadLocal<>();

    public static void setHeaders(Map<String, Object> headers) {
        headersThreadLocal.set(headers);
    }

    public static Map<String, Object> getHeaders() {
        return headersThreadLocal.get();
    }

    public static List<String> getUserPermissions() {
        return (List<String>) getHeaders().get(USER_PERMISSION_HEADER);
    }

    public static List<String> getRoles() {
        return (List<String>) getHeaders().get(USER_ROLES_HEADER);
    }

    public static String getUserId() {
        return getHeaders().get(USER_ID_HEADER).toString();
    }

    public static String getOpenaiHost() {
        if (getHeaders() == null || getHeaders().get(OPENAI_HOST).toString() == null) {
            return null;
        }
        return getHeaders().get(OPENAI_HOST).toString();
    }

    public static String getOpenaiToken() {
        if (getHeaders() == null || getHeaders().get(OPENAI_TOKEN).toString() == null) {
            return null;
        }
        return getHeaders().get(OPENAI_TOKEN).toString();
    }

    public static String getOpenaiModel() {
        if (getHeaders() == null || getHeaders().get(OPENAI_MODEL).toString() == null) {
            return null;
        }
        return getHeaders().get(OPENAI_MODEL).toString();
    }


    public static List<Long> getUserProducts() {
        List<String> stringList = (List<String>) getHeaders().get(USER_PRODUCTS_IDS_HEADER);
        if (stringList == null || stringList.isEmpty()) {
            return List.of();
        }
        return stringList.stream()
                .filter(str -> str != null && !str.trim().isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
