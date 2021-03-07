package com.anonym.spring.controller;

import com.anonym.spring.model.ResultSet;
import com.anonym.spring.service.ShopOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/provider")
@RestController
public class ShopProviderOperationController {

    private static Logger logger = LoggerFactory.getLogger(ShopProviderOperationController.class);

    @Resource
    private ShopOperationService shopOperationService;

    @RequestMapping("/selectShop")
    public ResultSet selectShop(String type,Long userId){
        ResultSet resultSet = new ResultSet();
        try {
            resultSet = shopOperationService.selectShop(type,userId);
        } catch (Exception e) {
            logger.error("execute shopOperationService.selectShop exception!",e);
            resultSet.setRetCode("0");
            resultSet.setRetVal("execute shopOperationService.selectShop exception!");
            return resultSet;
        }
        return resultSet;
    }

}
