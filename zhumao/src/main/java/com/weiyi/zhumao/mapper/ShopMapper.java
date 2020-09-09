package com.weiyi.zhumao.mapper;
import com.weiyi.zhumao.entity.Shop;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ShopMapper {
    @Select("SELECT * FROM shop WHERE id = #{id}")
    Shop getById(@Param("id") long id);

    @Update("UPDATE shop SET cars = #{shop.cars}, currentCar = #{shop.currentCar} WHERE id = #{shop.id}")
    void update(@Param("shop") Shop shop);

    @Delete("DELETE FROM shop WHERE id = #{id}")
    void deleteById(@Param("id") long id);
    
	@Insert("INSERT INTO shop (id, cars, currentCar) VALUES (#{shop.id}, #{shop.cars}, #{shop.currentCar})")
	void insert(@Param("shop") Shop shop);
}