package ru.beeline.capability.annotation;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ru.beeline.capability.utils.Constants.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = OPENAI_HOST, paramType = "header", dataType = "string", dataTypeClass = String.class),
        @ApiImplicitParam(name = OPENAI_TOKEN, paramType = "header", dataType = "string", dataTypeClass = String.class),
        @ApiImplicitParam(name = OPENAI_MODEL, paramType = "header", dataType = "string", dataTypeClass = String.class)
})
public @interface AiToolHeaders {
}
