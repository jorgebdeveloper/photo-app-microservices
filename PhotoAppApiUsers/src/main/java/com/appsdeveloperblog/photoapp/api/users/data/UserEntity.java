package com.appsdeveloperblog.photoapp.api.users.data;

import java.io.Serializable;
import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="users")
public class UserEntity implements Serializable {

	private static final long serialVersionUID = -2731425678149216053L;

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable=false, length=50)
	private String firstName;

	@Column(nullable=false, length=50)
	private String lastName;

	@Column(nullable=false, length=120, unique=true)
	private String email;

	@Column(nullable=false, unique=true)
	private String userId;

	@Column(nullable=false, unique=true)
	private String encryptedPassword;

	@ManyToMany(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER)
	@JoinTable(name="users_roles",
				joinColumns=@JoinColumn(name="users_id", referencedColumnName="id"),
				inverseJoinColumns=@JoinColumn(name="roles_id", referencedColumnName="id"))
	Collection<RoleEntity> roles;

}
