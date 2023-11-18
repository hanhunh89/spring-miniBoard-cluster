package com.springmvc.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.springmvc.repository.UserRepository;
import com.springmvc.domain.User;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;
	
	public void createUser(User user) {
		userRepository.createUser(user);
	}
	public User getUser(String userId) {
		//if(userRepository.getUser(userId)==null) {
		//	System.out.println("UserSErviceImpl userId : "+userId+" �� null�̴�. �ش� ���� ����");
		//}
		//else {
		//	System.out.println("UserSErviceImpl userId : "+userId+" �� null �ƴϴ�. ���� ����");
		//}
		
		return userRepository.getUser(userId); 
	}
	public void deleteUser(String userId) {
		userRepository.deleteUser(userId);
	}
}
