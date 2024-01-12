package com.appsdeveloperblog.photoapp.api.users.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.appsdeveloperblog.photoapp.api.users.service.UsersService;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class WebSecurity {

	private Environment env;
	private UsersService usersService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public WebSecurity(Environment env,
					   UsersService usersService,
					   BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.env = env;
		this.usersService = usersService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

		AuthenticationManagerBuilder authMgrBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		AuthenticationManager authMgr = configureAuthMgr(authMgrBuilder);
		AuthenticationFilter authFilter = configureAuthFilter(authMgr);

		http.csrf(csrf -> csrf.disable())
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.authorizeHttpRequests(configureAllowedPaths())
//        	.addFilter(new AuthorizationFilter(authenticationManager, environment))
			.addFilter(authFilter)
			.authenticationManager(authMgr)
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	@NotNull
	private static Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>
			.AuthorizationManagerRequestMatcherRegistry> configureAllowedPaths() {

		return authz -> authz.requestMatchers(new AntPathRequestMatcher("/users/**")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/actuator/**", HttpMethod.GET.name())).permitAll();
	}

	private AuthenticationManager configureAuthMgr(AuthenticationManagerBuilder authMgrBuilder) throws Exception {

		authMgrBuilder.userDetailsService(usersService)
						.passwordEncoder(bCryptPasswordEncoder);

		return authMgrBuilder.build();
	}

	private AuthenticationFilter configureAuthFilter(AuthenticationManager authMgr) {
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(authMgr, usersService, env);
		authenticationFilter.setFilterProcessesUrl(env.getProperty("login.url.path"));
		return authenticationFilter;
	}
}
