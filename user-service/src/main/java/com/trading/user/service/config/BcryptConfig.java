package com.trading.user.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration that provides the password encoder used to hash user passwords.
 */
@Configuration
public class BcryptConfig {

    /**
     * Creates the BCrypt password encoder bean.
     *
     * @return a configured {@link BCryptPasswordEncoder}. Never null.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
