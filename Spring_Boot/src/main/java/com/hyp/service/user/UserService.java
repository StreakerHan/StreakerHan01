package com.hyp.service.user;

import java.util.List;

import com.hyp.model.UserDomain;

/**
 * 

* @Title: UserService  

* @Description: 用户服务层  

* @author HanYupeng  

* @date 2018-06-15 10:34
 */
public interface UserService {

    /**
     * @Author: Donghua.Chen
     * @Description: 更改用户信息
     * @Date: 2018/4/20
     * @param user
     */
    int updateUserInfo(UserDomain user);

    /**
     * @Author: Donghua.Chen
     * @Description: 根据主键编号获取用户信息
     * @Date: 2018/4/20
     * @param uId 主键
     */
    UserDomain getUserInfoById(Integer uId);


    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return
     */
    UserDomain login(String username, String password);

}
