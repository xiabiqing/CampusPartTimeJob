package fun.xiabiqing.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.AdminMapper;
import fun.xiabiqing.mapper.UserBusinessMapper;
import fun.xiabiqing.model.domain.*;
import fun.xiabiqing.model.request.LoginUser;
import fun.xiabiqing.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author 26324
* @description 针对表【admin(用户表)】的数据库操作Service实现
* @createDate 2025-12-31 12:31:06
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService {
    @Autowired
    private UserService userService;
    @Autowired
    @Lazy
    private RoleApplicationService roleApplicationService;
    @Autowired
    private UserBusinessService userBusinessService;
    @Autowired
    private UserStudentService userStudentService;

    @Override
    public Boolean dealApplication(HttpServletRequest request, Boolean choose,RoleApplication roleApplication) {
        /**
         * 基本校验
         */
        Admin admin = (Admin) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if(admin==null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if(!UserConstant.MANAGER.equals(admin.getRole())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        /**
         * 申请表查申请者id，定位到用户表的id，进行role的修改，并同时修改申请表的status
         */
        Long stuId = roleApplication.getStuId();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        QueryWrapper<UserStudent> userStudentQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", stuId);
        User user = userService.getOne(userQueryWrapper);
        Admin newAdmin = new Admin();
        if(choose){
           if(UserConstant.MANAGER.equals(roleApplication.getRole())){
               /**
                * 把用户表信息删除，添加到管理员表
                */
               BeanUtils.copyProperties(newAdmin,user);
               userService.removeById(user);
               this.save(newAdmin);
               roleApplication.setStatus(UserConstant.SUCCESS);
               roleApplicationService.updateById(roleApplication);

           }
           if(UserConstant.BUSINESS.equals(roleApplication.getRole())){

               UserBusiness userBusiness = new UserBusiness();
               userBusiness.setUserIdBus(user.getId());
               userBusinessService.save(userBusiness);
               /**
                * 把用户的关联学生外键删除了，信息展示不冲突
                */
               userStudentQueryWrapper.eq("user_id_stu", user.getId());
               userStudentService.remove(userStudentQueryWrapper);
               user.setRole(roleApplication.getRole());
               userService.updateById(user);
           }

            roleApplication.setStatus(UserConstant.SUCCESS);
            roleApplicationService.updateById(roleApplication);
        }else{
            roleApplication.setStatus(UserConstant.FLAIL);
            roleApplicationService.updateById(roleApplication);
        }
        return choose;

    }
    @Override
    public Admin login(LoginUser loginUser, HttpServletRequest request) {
        /**
         * 密码单向加密
         */
        String newPassword = DigestUtils.md5DigestAsHex(loginUser.getUserPassword().getBytes());
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.eq("user_account",loginUser.getUserAccount());
        adminQueryWrapper.eq("user_password",newPassword);
        Admin admin = this.getOne(adminQueryWrapper);
        if(admin == null){
            throw new BusinessException(400012,"用户不存在");
        }
        /**
         * 过滤敏感信息
         */
        Admin newAdmin = this.safeAdmin(admin);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS,newAdmin);
        return newAdmin;
    }

    @Override
    public Admin safeAdmin(Admin admin) {
        Admin safeadmin = new Admin();
        safeadmin.setId(admin.getId());
        safeadmin.setUsername(admin.getUsername());
        safeadmin.setImage(admin.getImage());
        safeadmin.setRole(admin.getRole());
        return safeadmin;
    }
}




