package fun.xiabiqing.controller;

import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ResultUtils;
import fun.xiabiqing.model.domain.JobApplication;
import fun.xiabiqing.model.domain.Recruitment;
import fun.xiabiqing.model.domain.UserBusiness;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.service.UserStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/student")
public class UserStudentController {
    @Autowired
    private UserStudentService userStudentService;
    @GetMapping("/displayRecruitment")
    BaseResponse<List<Recruitment>> displayRecruitment(HttpServletRequest request) {
        return ResultUtils.success(userStudentService.displayRecruitment(request));
    }
    @PostMapping("/sendJobApplication")
    BaseResponse<Boolean> sendJobApplication(Recruitment recruitment, HttpServletRequest request, @RequestBody JobApplication jobApplication) {
        return ResultUtils.success(userStudentService.sendJobApplication(recruitment,request,jobApplication));
    }
    @GetMapping("/listJobApp")
    BaseResponse<List<JobApplication>> listJobApp(HttpServletRequest request) {
        return ResultUtils.success(userStudentService.listJobApplication(request));
    }
    @GetMapping("/chat/contacts")
    BaseResponse<Set<UserWithBusiness>> getContacts(HttpServletRequest request) {
        Set<UserWithBusiness> contacts = userStudentService.getContacts(request);
        return ResultUtils.success(contacts);
    }
}
