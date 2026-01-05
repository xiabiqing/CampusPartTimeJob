package fun.xiabiqing.controller;

import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ResultUtils;
import fun.xiabiqing.model.domain.RoleApplication;
import fun.xiabiqing.service.RoleApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/portRole")
@RestController
public class RoleApplicationController {
    @Autowired
    private RoleApplicationService roleApplicationService;
    @PostMapping("/role")
    BaseResponse<Integer> identityPorting(@Valid @RequestBody RoleApplication roleApplication, HttpServletRequest request) {
        Integer status = roleApplicationService.identityPorting(roleApplication, request);
        return ResultUtils.success(status);
    }

}
