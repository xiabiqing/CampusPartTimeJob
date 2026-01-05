package fun.xiabiqing.service.impl;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.RoleApplicationMapper;
import fun.xiabiqing.model.domain.Admin;
import fun.xiabiqing.model.domain.RoleApplication;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.AdminService;
import fun.xiabiqing.service.RoleApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author 26324
* @description 针对表【role_application(角色申请表)】的数据库操作Service实现
* @createDate 2025-12-31 11:41:52
*/
@Service
public class RoleApplicationServiceImpl extends ServiceImpl<RoleApplicationMapper, RoleApplication>
    implements RoleApplicationService {
    @Autowired
    private AdminService adminService;

    @Override
    public Integer identityPorting(RoleApplication roleApplication, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if(user == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        /**
         *判断申请表是否合法，比如申请角色重复或为空
         */
        Integer role = user.getRole();
        Integer toDoRole = roleApplication.getRole();
        if(roleApplication == null){
            throw new BusinessException(ErrorCode.PARAM_NULL,"申请表单不能为空");
        }
        if(role.equals(toDoRole)){
            throw new BusinessException(400021,"申请的身份和现身份不能一致");
        }
        /**
         * 把前端填写的申请信息存储在数据库中，并返回申请表的默认状态(处理中)
         */
        RoleApplication roleApplication1 = new RoleApplication();
        roleApplication1.setStuId(user.getId());
        roleApplication1.setAdminId(roleApplication.getAdminId());
        roleApplication1.setRole(roleApplication.getRole());
        roleApplication1.setInformation(roleApplication.getInformation());
        roleApplication1.setImage(roleApplication.getImage());
        roleApplication1.setStatus(0);
        this.save(roleApplication1);
        return roleApplication.getStatus();
    }
}




