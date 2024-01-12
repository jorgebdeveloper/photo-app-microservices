package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

//import feign.FeignException;

import com.appsdeveloperblog.photoapp.api.users.data.*;

@Slf4j
@Service
public class UsersServiceImpl implements UsersService {

	UsersRepository usersRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	RestTemplate restTemplate;
	Environment environment;
	AlbumsServiceClient albumsServiceClient;

	@Autowired
	public UsersServiceImpl(UsersRepository usersRepository,
							BCryptPasswordEncoder bCryptPasswordEncoder,
							RestTemplate restTemplate,
							Environment environment,
							AlbumsServiceClient albumsServiceClient) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.restTemplate = restTemplate;
		this.environment = environment;
		this.albumsServiceClient = albumsServiceClient;
	}

	@Transactional
	@Override
	public UserDto createUser(UserDto userDetails) {

		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

		usersRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = usersRepository.findByEmail(username);

		if(userEntity == null) throw new UsernameNotFoundException(username);

		return new User(userEntity.getEmail(),
						userEntity.getEncryptedPassword(),
						true, true, true, true,
						getGrantedAuthorities(userEntity));
	}

	@NotNull
	private static Collection<GrantedAuthority> getGrantedAuthorities(UserEntity userEntity) {
		Collection<GrantedAuthority> authorities = new ArrayList<>();

		for (RoleEntity role : userEntity.getRoles()) {
			authorities.add(new SimpleGrantedAuthority(role.getName()));

			for (AuthorityEntity authorityEntity : role.getAuthorities()) {
				authorities.add(new SimpleGrantedAuthority(authorityEntity.getName()));
			}
		}
		return authorities;
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = usersRepository.findByEmail(email);

		if(userEntity == null) throw new UsernameNotFoundException(email);

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId, String authorization) {

        UserEntity userEntity = usersRepository.findByUserId(userId);
        if(userEntity == null) throw new UsernameNotFoundException("User not found");
        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//        String albumsUrl = String.format(environment.getProperty("albums.url"), userId);
//        ResponseEntity<List<AlbumResponseModel>> albumsListResponse =
//				restTemplate.exchange(albumsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
//        List<AlbumResponseModel> albumsList = albumsListResponse.getBody();

		List<AlbumResponseModel> albumsList = null;
		try {
			log.debug("Start of albumsServiceClient.getAlbums");
			albumsList = albumsServiceClient.getAlbums(userId, authorization);
			log.debug("End of albumsServiceClient.getAlbums");
		} catch (FeignException e) {
			log.error(e.getLocalizedMessage());
		}

		userDto.setAlbums(albumsList);
		return userDto;
	}




}
