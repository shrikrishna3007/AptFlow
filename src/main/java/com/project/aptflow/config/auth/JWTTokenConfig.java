package com.project.aptflow.config.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jwt.access-token.expiration")
public class JWTTokenConfig {
    private long user;
    private long admin;

}
