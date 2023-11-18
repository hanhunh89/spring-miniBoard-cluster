package com.springmvc.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.springmvc.domain.User;
import com.springmvc.service.UserService;


@Controller
@RequestMapping(value="/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/join", method=RequestMethod.GET)
	public String createUserController_get(@ModelAttribute("newUser") User user) {
		return "join";
	}
	@RequestMapping(value="/existUserId", method=RequestMethod.GET)
	public String createUserController_existUserId(@ModelAttribute("newUser") User user, Model model) {
		// "�̹� �����ϴ� �����Դϴ�" ����
		model.addAttribute("userExist","true");
		return "join";
	}
	
	@RequestMapping(value="/join", method=RequestMethod.POST)
	public String createUserController_post(@ModelAttribute("newUser") User user, Model model) {
		// user�� �����ϸ� redirect
		User u = userService.getUser(user.getUserId());
		if(u!=null) {
			return "redirect:/user/existUserId";
		}
		
		//user �������� ������ ����
		userService.createUser(user);
		return "redirect:/post";
	}
	@RequestMapping(value="/delete/{userId}", method=RequestMethod.GET)
	public String deleteUserController(@PathVariable String userId) {
		userService.deleteUser(userId);
		return "redirect:/post";
	}
}

