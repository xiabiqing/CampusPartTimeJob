package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.Admin;
import fun.xiabiqing.model.domain.RoleApplication;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.model.request.LoginUser;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 26324
* @description 针对表【admin(用户表)】的数据库操作Service
* @createDate 2025-12-31 12:31:06
*/
public interface AdminService extends IService<Admin> {
    /**
     *  管理员处理申请表
     * @param request 请求
     * @param choose 同意/拒绝
     * @param roleApplication 申请表单
     * @return 状态(choose)
     */
    Boolean dealApplication(HttpServletRequest request, Boolean choose, RoleApplication roleApplication);

    /**
     *  登陆
     * @param loginUser 登录封装类
     * @param request 请求
     * @return 管理员信息(安全)
     */
    Admin login(LoginUser loginUser, HttpServletRequest request);

    /**
     *  敏感信息过滤
     * @param admin 原始信息(管理员)
     * @return 管理员安全信息(安全)
     */
    Admin safeAdmin(Admin admin);

}
