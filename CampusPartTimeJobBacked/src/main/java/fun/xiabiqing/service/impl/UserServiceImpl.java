package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.UserMapper;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.model.domain.UserBusiness;
import fun.xiabiqing.model.domain.UserStudent;
import fun.xiabiqing.model.request.LoginUser;
import fun.xiabiqing.model.request.RegisterUser;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import fun.xiabiqing.service.UserBusinessService;
import fun.xiabiqing.service.UserService;
import fun.xiabiqing.service.UserStudentService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * @author 26324
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-12-29 21:46:35
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Autowired
    private UserStudentService userStudentService;
    @Autowired
    private UserBusinessService userBusinessService;

    @Override
    public Long register(RegisterUser registerUser) {
        String userAccount = registerUser.getUserAccount();
        String userPassword = registerUser.getUserPassword();
        if (!userPassword.equals(registerUser.getCheckPassword())) {
            throw new BusinessException(400010, "两次密码不一致");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        if (this.exists(userQueryWrapper)) {
            throw new BusinessException(400011, "用户名已存在");
        }
        /*
        密码加密单向
         */
        userPassword=userPassword+UserConstant.SCRET_PASSWORD;
        String newPassword = DigestUtils.md5DigestAsHex(userPassword.getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        this.save(user);
        /**
         * 给外键设值，与用户表关联起来，确认身份
         */
        UserStudent userStudent = new UserStudent();
        userStudent.setUserIdStu(user.getId());
        userStudentService.save(userStudent);
        return user.getId();
    }

    @Override
    public User login(LoginUser loginUser, HttpServletRequest request) {
        String newPassword = DigestUtils.md5DigestAsHex((loginUser.getUserPassword()+UserConstant.SCRET_PASSWORD).getBytes());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", loginUser.getUserAccount());
        userQueryWrapper.eq("user_password", newPassword);
        User user = this.getOne(userQueryWrapper);
        if (user == null) {
            throw new BusinessException(400012, "用户不存在");
        }
        User newUser = safeUser(user);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, newUser);
        return newUser;
    }

    @Override
    public Boolean updateDefaultInfo(UserWithStudent userWithStudent, HttpServletRequest request) {
        /**
         * 正常更新校验(登录和修改权限)
         */
        User attribute = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = userWithStudent.getId();
        if (!this.isAdmin(request) && !id.equals(attribute.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        /**
         * 前端填写信息赋值到用户对象和角色对象里面，更新数据库信息
         */
        User user = new User();
        user.setId(id);
        user.setUsername(userWithStudent.getUsername());
        user.setImage(userWithStudent.getImage());
        boolean b = this.updateById(user);
        UserStudent userStudent = new UserStudent();
        QueryWrapper<UserStudent> userStudentQueryWrapper = new QueryWrapper<>();
        userStudentQueryWrapper.eq("user_id_stu", id);
        userStudent.setAge(userWithStudent.getAge());
        userStudent.setGender(userWithStudent.getGender());
        userStudent.setMajor(userWithStudent.getMajor());
        userStudent.setEmail(userWithStudent.getEmail());
        userStudent.setSelfIntroduction(userWithStudent.getSelfIntroduction());
        boolean a = userStudentService.update(userStudent, userStudentQueryWrapper);
        if (!a || !b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return true;
    }

    @Override
    public Boolean updateBusInfo(UserWithBusiness userWithBusiness, HttpServletRequest request) {
        /**
         * 正常更新校验(登录和修改权限)
         */
        User attribute = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = userWithBusiness.getId();
        if (!this.isAdmin(request) && !id.equals(attribute.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        /**
         * 前端填写信息赋值到用户对象和角色对象里面，更新数据库信息
         */
        User user = new User();
        user.setId(id);
        user.setUsername(userWithBusiness.getUsername());
        user.setImage(userWithBusiness.getImage());
        boolean b = this.updateById(user);
        UserBusiness userBusiness = new UserBusiness();
        QueryWrapper<UserBusiness> userBusinessQueryWrapper = new QueryWrapper<>();
        userBusinessQueryWrapper.eq("user_id_bus", id);
        userBusiness.setPosition(userWithBusiness.getPosition());
        userBusiness.setLocation(userWithBusiness.getLocation());
        userBusiness.setCompany(userWithBusiness.getCompany());
        boolean a = userBusinessService.update(userBusiness, userBusinessQueryWrapper);
        if (!a || !b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return true;
    }

    @Override
    public Object currentInfo(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = user.getId();

        if (UserConstant.BUSINESS.equals(user.getRole())) {
              /*
        查找商家id关联的外键的信息
         */
            QueryWrapper<UserBusiness> userBusinessQueryWrapper = new QueryWrapper<>();
            userBusinessQueryWrapper.eq("user_id_bus", id);
            UserBusiness userbus = userBusinessService.getOne(userBusinessQueryWrapper);
            /**
             * 把2个对象的信息封装成一个对象的信息去返回给前端，
             * 注意这就是一个单纯的封装类，数据库表里面并不存在
             */
            UserWithBusiness userWithBusiness = new UserWithBusiness();
            userWithBusiness.setId(user.getId());
            userWithBusiness.setUsername(user.getUsername());
            userWithBusiness.setImage(user.getImage());
            userWithBusiness.setRole(user.getRole());
            userWithBusiness.setPosition(userbus.getPosition());
            userWithBusiness.setLocation(userbus.getLocation());
            userWithBusiness.setCompany(userbus.getCompany());
            return userWithBusiness;
        }
        /*
        查找学生id关联的外键的信息
         */
        QueryWrapper<UserStudent> userStudentQueryWrapper = new QueryWrapper<>();
        userStudentQueryWrapper.eq("user_id_stu", id);
        UserStudent userStudent = userStudentService.getOne(userStudentQueryWrapper);
        /**
         * 把2个对象的信息封装成一个对象的信息去返回给前端，
         * 注意这就是一个单纯的封装类，数据库表里面并不存在
         */

        UserWithStudent userWithStudent = new UserWithStudent();
        userWithStudent.setId(user.getId());
        userWithStudent.setUsername(user.getUsername());
        userWithStudent.setImage(user.getImage());
        userWithStudent.setRole(user.getRole());
        userWithStudent.setAge(userStudent.getAge());
        userWithStudent.setGender(userStudent.getGender());
        userWithStudent.setMajor(userStudent.getMajor());
        userWithStudent.setEmail(userStudent.getEmail());
        userWithStudent.setSelfIntroduction(userStudent.getSelfIntroduction());
        return userWithStudent;
    }

    @Override
    public User safeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setImage(user.getImage());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setRole(user.getRole());
        return safeUser;
    }

    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        User attribute = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (attribute == null) {
            return false;
        }
        return UserConstant.MANAGER.equals(attribute.getRole());
    }



}




