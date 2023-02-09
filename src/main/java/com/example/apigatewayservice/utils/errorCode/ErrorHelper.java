package com.example.apigatewayservice.utils.errorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
@Slf4j
public class ErrorHelper {
    private final Environment env;

    public ErrorHelper(Environment env) {
        this.env = env;
    }

    public String getCode(String Error) {
        try {
            return String.format(Objects.requireNonNull(env.getProperty(Error + ".code")));
        }
        catch (NullPointerException e) {
            log.info(Error + ".code");
            log.info(env.getProperty(Error + ".code"));
            return String.format(Objects.requireNonNull(env.getProperty("Unexpected.code")));
        }
    }

    public String getMsg(String Error) {
        try {
            return String.format(Objects.requireNonNull(env.getProperty(Error + ".msg")));
        }
        catch (NullPointerException e) {
            log.info(Error + ".code");
            log.info(env.getProperty(Error + ".code"));
            return String.format(Objects.requireNonNull(env.getProperty("Unexpected.msg")));
        }
    }
}
