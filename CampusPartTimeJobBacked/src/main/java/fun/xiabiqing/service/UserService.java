package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.model.domain.UserBusiness;
import fun.xiabiqing.model.request.LoginUser;
import fun.xiabiqing.model.request.RegisterUser;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 26324
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-12-29 21:46:35
*/
public interface UserService extends IService<User> {
    /**
     * 注册
     * @param registerUser 注册所需信息
     * @return 用户的id
     */
    Long register(RegisterUser registerUser);

    /**
     * 登录
     * @param loginUser 登录所需信息
     * @return 用户信息
     */
    User login(LoginUser loginUser, HttpServletRequest request);

    /**
     * 更新默认信息(学生)
     * @param userWithStudent 要修改的学生信息
     * @param request 请求
     * @return boolean
     */
    Boolean updateDefaultInfo(UserWithStudent userWithStudent, HttpServletRequest request);
    Boolean updateBusInfo(UserWithBusiness userWithBusiness, HttpServletRequest request);

    /**
     * 展示用户前端关联的角色信息
     * @param request 请求
     * @return 返回角色关联信息
     */
    Object currentInfo(HttpServletRequest request);

    /**
     * 用户的安全信息
     * @param user 原始信息
     * @return 安全信息
     */
    User safeUser(User user);

    /**
     * 是否是管理员
     * @param request 请求
     * @return boolean
     */
    Boolean isAdmin(HttpServletRequest request);

    /**
     * 获取用户申请的联系人
     * @param request 请求
     * @return 户申请的联系人
     */


}
