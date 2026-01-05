package fun.xiabiqing.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.common.ResultUtils;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.model.domain.Admin;
import fun.xiabiqing.model.domain.RoleApplication;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.model.request.LoginUser;
import fun.xiabiqing.service.AdminService;
import fun.xiabiqing.service.RoleApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private RoleApplicationService roleApplicationService;
    @PostMapping("/login")
    BaseResponse<Admin> login(@Valid @RequestBody LoginUser loginUser, HttpServletRequest request) {
        Admin newAdmin = adminService.login(loginUser, request);
        return ResultUtils.success(newAdmin);
    }

    @GetMapping("/listAdmin")
    BaseResponse<List<Admin>> listAdmin() {
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        List<Admin> admins = adminService.list(adminQueryWrapper);
        List<Admin> safeAdmins = admins.stream().map(admin -> {
            return new Admin(admin.getId(), admin.getUsername(), admin.getImage(), admin.getRole());
        }).collect(Collectors.toList());
        return ResultUtils.success(safeAdmins);
    }
    @PostMapping("/dealApplication")
    BaseResponse<Boolean> dealApplication(HttpServletRequest request,@RequestParam Boolean choose,@RequestBody RoleApplication roleApplication) {
        Boolean b = adminService.dealApplication(request, choose, roleApplication);
        return ResultUtils.success(b);
    }
    @GetMapping("/application")
    BaseResponse<List<RoleApplication>> getApplication(HttpServletRequest request) {
        Admin attribute = (Admin) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if(attribute == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if(!UserConstant.MANAGER.equals(attribute.getRole())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<RoleApplication> roleApplicationQueryWrapper = new QueryWrapper<>();
        roleApplicationQueryWrapper.eq("admin_id", attribute.getId());
        List<RoleApplication> list = roleApplicationService.list(roleApplicationQueryWrapper);
        return ResultUtils.success(list);
    }


}
