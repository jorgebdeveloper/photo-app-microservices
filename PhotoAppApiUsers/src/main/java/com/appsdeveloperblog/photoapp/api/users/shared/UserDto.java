package com.appsdeveloperblog.photoapp.api.users.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;
import lombok.Data;

@Data
public class UserDto implements Serializable {

	@Serial
	private static final long serialVersionUID = -953297098295050686L;

	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String userId;
	private String encryptedPassword;
	private List<AlbumResponseModel> albums;

}
