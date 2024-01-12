package com.appsdeveloperblog.photoapp.discovery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurity {

//    @Bean
//    SecurityFilterChain configure(HttpSecurity http) throws Exception {
//
//        http.csrf().disable()
//                .authorizeHttpRequests()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();
//
//        return http.build();
//    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authMatch -> authMatch.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

}
