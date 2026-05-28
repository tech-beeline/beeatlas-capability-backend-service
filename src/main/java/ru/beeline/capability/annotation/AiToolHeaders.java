/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ru.beeline.capability.utils.Constants.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(name = OPENAI_HOST, in = ParameterIn.HEADER, schema = @Schema(type = "string")),
        @Parameter(name = OPENAI_TOKEN, in = ParameterIn.HEADER, schema = @Schema(type = "string")),
        @Parameter(name = OPENAI_MODEL, in = ParameterIn.HEADER, schema = @Schema(type = "string"))
})
public @interface AiToolHeaders {
}

