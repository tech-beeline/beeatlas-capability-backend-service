/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import java.util.Map;

import static ru.beeline.capability.utils.Constants.*;

public class RequestContext {
    private static final ThreadLocal<Map<String, Object>> headersThreadLocal = new ThreadLocal<>();

    public static void setHeaders(Map<String, Object> headers) {
        headersThreadLocal.set(headers);
    }

    public static Map<String, Object> getHeaders() {
        return headersThreadLocal.get();
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
}
