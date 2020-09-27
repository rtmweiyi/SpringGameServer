package com.weiyi.zhumao.service.impl;

import com.weiyi.zhumao.entity.User;
import com.weiyi.zhumao.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User getUserById(long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found by id.");
        }
        return user;
    }

    public User getUserByToken(String token) {
        User user = userMapper.getByToken(token);
        if (user == null) {
            throw new RuntimeException("User not found by token.");
        }
        return user;
    }

    public User register(String name, String token,String cars, String currentCar,int coins) {
		User user = new User();
        user.setName(name);
        user.setToken(token);
        user.setCars(cars);
        user.setCurrentCar(currentCar);
        user.setCoins(coins);
		userMapper.insert(user);
		return user;
    }
    public void updateUser(User user) {
		userMapper.update(user);
    }
}