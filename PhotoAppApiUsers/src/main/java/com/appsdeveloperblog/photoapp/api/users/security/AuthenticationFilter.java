package com.appsdeveloperblog.photoapp.api.users.security;

import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.User;

import com.appsdeveloperblog.photoapp.api.users.service.UsersService;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private UsersService usersService;
	private Environment env;

	public AuthenticationFilter(AuthenticationManager authMgr,
								UsersService usersService,
								Environment env) {
		super(authMgr);
		this.env = env;
		this.usersService = usersService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
												HttpServletResponse res) throws AuthenticationException {
		try {
			LoginRequestModel creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequestModel.class);

			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req,
											HttpServletResponse res,
											FilterChain chain,
											Authentication auth) {

		String userName = ((User) auth.getPrincipal()).getUsername();
		UserDto userDetails = usersService.getUserDetailsByEmail(userName);
		String token = configureJwt(auth, userDetails);
		log.info(token);

		res.addHeader("token", token);
		res.addHeader("userId", userDetails.getUserId());
	}

	private String configureJwt(Authentication auth, UserDto userDetails) {
		Instant now = Instant.now();
		Key hmacKey = new SecretKeySpec(
				Base64.getDecoder().decode(env.getProperty("token.secret")), SignatureAlgorithm.HS256.getJcaName());

		return Jwts.builder()
				.claim("scope", auth.getAuthorities())
				.setSubject(userDetails.getUserId())
				.setIssuedAt(Date.from(now))
				.setExpiration(
						Date.from(now.plusMillis(Long.parseLong(env.getProperty("token.expiration_time")))))
				.signWith(hmacKey)
				.compact();
	}
}
