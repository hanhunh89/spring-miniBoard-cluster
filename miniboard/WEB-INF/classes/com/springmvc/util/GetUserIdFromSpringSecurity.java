package com.springmvc.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class GetUserIdFromSpringSecurity {
	public static String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || !authentication.isAuthenticated()) {
	    	return null; // ���� ����ڰ� �������� ���� ��� �Ǵ� ����ڰ� ���� ���
	    }
	    Object principal = authentication.getPrincipal();
	    if (principal instanceof UserDetails) {
	     	return ((UserDetails) principal).getUsername();
	    } else {
	        return principal.toString();
	    }
	}
}