package fun.xiabiqing.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.common.ResultUtils;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.model.domain.Recruitment;
import fun.xiabiqing.model.domain.RoleApplication;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.model.domain.UserBusiness;
import fun.xiabiqing.model.request.LoginUser;
import fun.xiabiqing.model.request.RegisterUser;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import fun.xiabiqing.service.RecruitmentService;
import fun.xiabiqing.service.RoleApplicationService;
import fun.xiabiqing.service.UserService;
import fun.xiabiqing.service.UserStudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserStudentService userStudentService;
    @Autowired
    private RoleApplicationService roleApplicationService;
    @Autowired
    private RecruitmentService recruitmentService;

    /**
     * 注册接口
     *
     * @param registerUser 注册类
     * @return 用户id
     */
    @PostMapping("/register")
    BaseResponse<Long> register(@Valid @RequestBody RegisterUser registerUser) {
        Long userId = userService.register(registerUser);
        return ResultUtils.success(userId);
    }

    /**
     * 登录接口
     *
     * @param loginUser 登录类
     * @param request   请求
     * @return user
     */
    @PostMapping("/login")
    BaseResponse<User> login(@Valid @RequestBody LoginUser loginUser, HttpServletRequest request) {
        User user = userService.login(loginUser, request);
        return ResultUtils.success(user);
    }

    /**
     * 修改默认信息接口(初始角色是学生)
     *
     * @param userWithStudent
     * @param request
     * @return
     */

    @PostMapping("/updateDefaultInfo")
    BaseResponse<Boolean> updateDefaultInfo(@Valid @RequestBody UserWithStudent userWithStudent, HttpServletRequest request) {
        Boolean b = userService.updateDefaultInfo(userWithStudent, request);
        return ResultUtils.success(b);
    }

    /**
     * 修改商家信息接口
     *
     * @param userWithBusiness 商家信息
     * @param request          请求
     * @return 成功或失败
     */
    @PostMapping("/updateBusInfo")
    BaseResponse<Boolean> updateBusInfo(@Valid @RequestBody UserWithBusiness userWithBusiness, HttpServletRequest request) {
        Boolean b = userService.updateBusInfo(userWithBusiness, request);
        return ResultUtils.success(b);
    }

    /**
     * 展示所有角色申请状态
     *
     * @param request 请求
     * @return 返回角色申请单
     */
    @GetMapping("/application")
    BaseResponse<List<RoleApplication>> getApplication(HttpServletRequest request) {
        User attribute = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }

        QueryWrapper<RoleApplication> roleApplicationQueryWrapper = new QueryWrapper<>();
        roleApplicationQueryWrapper.eq("stu_id", attribute.getId());
        List<RoleApplication> list = roleApplicationService.list(roleApplicationQueryWrapper);
        return ResultUtils.success(list);
    }

    /**
     * 前端展示信息
     *
     * @return
     */
    @GetMapping("/current")
    BaseResponse<Object> getCurrentUser(HttpServletRequest request) {
        Object o = userService.currentInfo(request);
        if (o instanceof UserWithStudent) {
            return ResultUtils.success((UserWithStudent) o);
        }
        return ResultUtils.success((UserWithBusiness) o);
    }

    /**
     * 根据工作内容描述搜索兼职
     *
     * @param search 搜索的内容
     * @return 返回所有符合的兼职
     */
    @GetMapping("/searchRecruitment")
    BaseResponse<List<Recruitment>> getRecruitmentsBySearch(@RequestParam String search) {
        if (search == null || search.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "搜索内容不能为空");
        }
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        recruitmentQueryWrapper.and(wrapper -> wrapper.like("title", search).or().like("company", search).or().like("job_details", search));
        List<Recruitment> list = recruitmentService.list(recruitmentQueryWrapper);
        return ResultUtils.success(list);
    }

}
