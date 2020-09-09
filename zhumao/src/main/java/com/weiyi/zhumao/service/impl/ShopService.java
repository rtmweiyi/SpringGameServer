package com.weiyi.zhumao.service.impl;

import com.weiyi.zhumao.entity.Shop;
import com.weiyi.zhumao.mapper.ShopMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ShopService {
    @Autowired
    ShopMapper shopMapper;

    public Shop getShopById(long id) {
        Shop shop = shopMapper.getById(id);
        if (shop == null) {
            throw new RuntimeException("Shop not found by id.");
        }
        return shop;
    }

    public Shop register(long id, String cars, String currentCar) {
		Shop shop = new Shop();
        shop.setId(id);
        shop.setCars(cars);
        shop.setCurrentCar(currentCar);
		shopMapper.insert(shop);
		return shop;
    }

    public void updateShop(Shop shop) {
		shopMapper.update(shop);
    }
}