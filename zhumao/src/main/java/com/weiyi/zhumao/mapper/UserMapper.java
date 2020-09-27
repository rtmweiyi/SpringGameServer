package com.weiyi.zhumao.mapper;

import com.weiyi.zhumao.entity.User;

import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User getById(@Param("id") long id);

    @Select("SELECT * FROM users WHERE token = #{token}")
    User getByToken(@Param("token") String token);
    
    @Update("UPDATE users SET name = #{user.name},cars = #{user.cars}, currentCar = #{user.currentCar},coins = #{user.coins},createdAt = #{user.createdAt} WHERE id = #{user.id}")
    void update(@Param("user") User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") long id);
    
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	@Insert("INSERT INTO users (name, token, cars, currentCar, coins, createdAt) VALUES (#{user.name}, #{user.token},#{user.cars},#{user.currentCar},#{user.coins},#{user.createdAt})")
	void insert(@Param("user") User user);
}