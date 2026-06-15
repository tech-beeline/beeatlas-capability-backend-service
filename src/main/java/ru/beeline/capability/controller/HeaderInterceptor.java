/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static ru.beeline.capability.utils.Constants.*;

public class HeaderInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(OPENAI_HOST, request.getHeader(OPENAI_HOST) != null ? request.getHeader(OPENAI_HOST) : "");
        headers.put(OPENAI_TOKEN, request.getHeader(OPENAI_TOKEN) != null ? request.getHeader(OPENAI_TOKEN) : "");
        headers.put(OPENAI_MODEL, request.getHeader(OPENAI_MODEL) != null ? request.getHeader(OPENAI_MODEL) : "");
        RequestContext.setHeaders(headers);
        logger.debug("Request: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
