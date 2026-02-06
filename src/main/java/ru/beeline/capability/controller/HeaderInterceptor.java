/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.beeline.capability.exception.ForbiddenException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.*;

public class HeaderInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);
    private static final Set<String> HEADERS_PATHS = Set.of(
            "/maps",
            "/v2/business-capability",
            "/capabilities-subscribed",
            "/v1/business-capability/order",
            "/recount-quality"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String uri = request.getRequestURI();
            if (!isHeaderPath(uri)
                    || uri.contains("/api/v1/business-capability/order/domains")
                    || (uri.contains("/api/v1/business-capability/order") && !uri.contains("/draft")
                    && request.getMethod().equals("GET"))
            ) {
                Map<String, Object> headers = new HashMap<>();
                headers.put(OPENAI_HOST, request.getHeader(OPENAI_HOST) != null ? request.getHeader(OPENAI_HOST).toString() : "");
                headers.put(OPENAI_TOKEN, request.getHeader(OPENAI_TOKEN) != null ? request.getHeader(OPENAI_TOKEN).toString() : "");
                headers.put(OPENAI_MODEL, request.getHeader(OPENAI_MODEL) != null ? request.getHeader(OPENAI_MODEL).toString() : "");
                RequestContext.setHeaders(headers);
                logger.info("without check headers");
                return true;
            }
            logger.info("check headers");
            Map<String, Object> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                logger.debug(headerName + ": " + headerValue);
            }
            logger.info(USER_ID_HEADER + request.getHeader(USER_ID_HEADER));
            headers.put(USER_ID_HEADER, request.getHeader(USER_ID_HEADER).toString());
            logger.info(USER_PERMISSION_HEADER + toList(request.getHeader(USER_PERMISSION_HEADER)));
            headers.put(USER_PERMISSION_HEADER, toList(request.getHeader(USER_PERMISSION_HEADER).toString()));
            logger.info(USER_PRODUCTS_IDS_HEADER + toList(request.getHeader(USER_PRODUCTS_IDS_HEADER)));
            headers.put(USER_PRODUCTS_IDS_HEADER, toList(request.getHeader(USER_PRODUCTS_IDS_HEADER).toString()));
            logger.info(USER_ROLES_HEADER + toList(request.getHeader(USER_ROLES_HEADER)));
            headers.put(USER_ROLES_HEADER, toList(request.getHeader(USER_ROLES_HEADER).toString()));
            RequestContext.setHeaders(headers);
            logger.info("Set headers complete");
            return true;
        } catch (Exception e) {
            throw new ForbiddenException("Отсутсвуют необходимые хэдеры. ");
        }
    }

    private List<String> toList(String value) {
        return Arrays.stream(value.split(","))
                .map(str -> str.substring(0))
                .map(str -> str.replaceAll("\"", ""))
                .map(str -> str.replaceAll("]", ""))
                .map(str -> str.replaceAll("\\[", ""))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private boolean isHeaderPath(String path) {
        return HEADERS_PATHS.stream().anyMatch(path::contains);
    }
}