package com.anonym.spring.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.anonym.spring.mapper.UserMapper;
import com.anonym.spring.model.DateUtils;
import com.anonym.spring.model.MD5Util;
import com.anonym.spring.model.ResultSet;
import com.anonym.spring.model.SnowflakeAlgorithm;
import com.anonym.spring.pojo.User;
import com.anonym.spring.util.RedisUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 王培忠
 * @date 2021/02/10
 * @email 805705089@qq.com
 * @Description
 * @Reason ADDREASON
 * @since JDK 1.8
 */

@Service
public class UserOperaionServiceImpl implements UserOperaionService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Transactional
    @Override
    public ResultSet login(User user) throws IOException, ClassNotFoundException {
        ResultSet resultSet = new ResultSet();
        User returnUser = new User();
        String password = MD5Util.encrypt(user.getPassword());
        /*redis中如果存在此key，比较密码*/
        if(redisUtils.exists(user.getName())){
            /*String passwordSplit = (String) redisUtils.get(user.getName());
            String password = passwordSplit.split(",")[0];
            String md5password = MD5Util.encrypt(user.getPassword());
            returnUser.setPhone(user.getName());
            returnUser.setType(passwordSplit.split(",")[1]);
            if(md5password.equals(password)){
                resultSet.setRetCode("1");
                resultSet.setRetVal("");
                resultSet.setDataRows(returnUser);
                return resultSet;
            }else{
                resultSet.setRetCode("0");
                resultSet.setRetVal("用户名或密码错误");
                return resultSet;
            }*/
            User redisUser = (User) redisUtils.get(user.getName());
            if(password.equals(redisUser.getPassword())){
                resultSet.setRetCode("1");
                resultSet.setRetVal("");
                resultSet.setDataRows(redisUser);
                return resultSet;
            }else{
                resultSet.setRetCode("0");
                resultSet.setRetVal("用户名或密码错误");
                return resultSet;
            }
        }else{
            /*此key不存在，可能因为没用手机号登录，也有可能因为没有这个用户*/
            /*先判断是否用邮箱登录*/
            User emilUser = userMapper.verificationLongin(user);
            if(emilUser != null){
                /*证明使用了邮箱登录*/

                if(password.equals(emilUser.getPassword())){
                    /*证明密码一样*/
                    resultSet.setRetCode("1");
                    resultSet.setRetVal("");
                    resultSet.setDataRows(emilUser);
                    return resultSet;
                }else{
                    /*证明数据库中也没有此用户*/
                    resultSet.setRetCode("0");
                    resultSet.setRetVal("用户名或密码错误");
                    return resultSet;
                }
            }else{
                /*证明数据库中也没有此用户*/
                resultSet.setRetCode("0");
                resultSet.setRetVal("用户名或密码错误");
                return resultSet;
            }
        }
    }

    @Override
    public ResultSet registered(User user) throws Exception {
        ResultSet resultSet = new ResultSet();
        /*先去数据库查询一下有没有此用户*/
        User userExists = userMapper.isExistsUser(user);
        user.setId(SnowflakeAlgorithm.uniqueLong());
        /*存入数据库或redis之前需要判断*/
        if(!redisUtils.exists(user.getPhone()) & userExists == null){
            user.setPassword(MD5Util.encrypt(user.getPassword()));
            redisUtils.set(user.getPhone(),user);
            if (redisUtils.exists(user.getPhone())){
                user.setCreateTime(DateUtils.getDate());
                int temp = userMapper.registered(user);
                if (temp < 1 ){
                    throw new Exception("网络繁忙，请稍候重试");
                }else {
                    resultSet.setRetCode("1");
                    resultSet.setRetVal("");
                    resultSet.setDataRows(user);
                    return resultSet;
                }
            }else {
                throw new Exception("网络繁忙，请稍候重试");
            }
        }else{
            resultSet.setRetCode("0");
            resultSet.setRetVal("用户名或邮箱已存在");
            return resultSet;
        }
    }

    @Override
    public ResultSet verificationEmailOrphone(User user) {
        ResultSet resultSet = new ResultSet();
        if(!StringUtils.isEmpty(user.getPhone())){
            if(redisUtils.exists(user.getPhone())){
                resultSet.setRetCode("0");
                resultSet.setRetVal("手机号已存在");
                return resultSet;
            }else{
                User userPhone = userMapper.verificationPhoneOrEmail(user);
                if(userPhone != null){
                    resultSet.setRetCode("0");
                    resultSet.setRetVal("手机号已存在");
                    return resultSet;
                }
            }
        } else if(!StringUtils.isEmpty(user.getEmail())){
            /*去数据库查找相关邮箱*/
            User userEmail = userMapper.verificationPhoneOrEmail(user);
            if(userEmail != null){
                resultSet.setRetCode("0");
                resultSet.setRetVal("邮箱已存在");
                return resultSet;
            }
        }
        resultSet.setRetCode("1");
        return resultSet;
    }

    @Override
    public ResultSet selectUserById(Long id) throws Exception {
        ResultSet resultSet = new ResultSet();
        /*根据id查单条*/
        User user = userMapper.selectUserById(id);
        if(user != null){
            resultSet.setRetCode("1");
            resultSet.setRetVal("");
            resultSet.setDataRows(user);
            return resultSet;
        }else{
            throw new Exception("网络繁忙，请稍后再试");
        }
    }
}
