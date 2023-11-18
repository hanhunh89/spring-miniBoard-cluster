package com.springmvc.domain;

import java.sql.Timestamp;

import org.springframework.security.core.GrantedAuthority;
//spring security������ ó���� ���Ͽ� GrantedAuthority�� �����Ѵ�. 
public class User implements GrantedAuthority{

	private String userId;
	private String password;
	private String auth;
	private Timestamp createdAt;

	public User() {
	
	}
	
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAuth() {
		return auth;
	}
	public void setAuth(String auth) {
		this.auth = auth;
	}
	@Override //GrantedAuthority ������ ���� method
	public String getAuthority() {
		return auth;
	}
}
