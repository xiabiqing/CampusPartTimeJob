package fun.xiabiqing.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.common.ResultUtils;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.model.domain.*;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import fun.xiabiqing.service.JobApplicationService;
import fun.xiabiqing.service.RecruitmentService;
import fun.xiabiqing.service.TagService;
import fun.xiabiqing.service.UserBusinessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/business")
public class UserBusinessController {
    @Autowired
    private TagService tagService;
    @Autowired
    private RecruitmentService recruitmentService;
    @Autowired
    private UserBusinessService userBusinessService;
    @Autowired
    private JobApplicationService jobApplicationService;
    @PostMapping("/createMenu")
    BaseResponse<Boolean> createMenu(@Valid @RequestBody Recruitment menu, HttpServletRequest request) {
        return ResultUtils.success(recruitmentService.createMenu(menu, request));
    }
    @GetMapping("/getTags")
    BaseResponse<List<Tag>> getTagsList(HttpServletRequest request) {
        return ResultUtils.success(tagService.getTagsList(request));
    }
    @GetMapping("/getRecruList")
    BaseResponse<List<Recruitment>> getRecruitmentList(HttpServletRequest request) {
        User user = userBusinessService.checkBus(request);
        Long id = user.getId();
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        recruitmentQueryWrapper.eq("bus_id_recru", id);
        return ResultUtils.success(recruitmentService.list(recruitmentQueryWrapper));
    }
    @DeleteMapping("/deleteMenu")
    BaseResponse<Boolean> deleteTag(@RequestBody Recruitment menu,HttpServletRequest request) {
        User user = userBusinessService.checkBus(request);
        if(!user.getId().equals(menu.getBusIdRecru())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = recruitmentService.removeById(menu.getId());
        /*
        顺便删除所有的申请单
         */
        Long id = menu.getId();
        QueryWrapper<JobApplication> jobApplicationQueryWrapper = new QueryWrapper<>();
        jobApplicationQueryWrapper.eq("recru_id", id);
        jobApplicationService.remove(jobApplicationQueryWrapper);
        return ResultUtils.success(result);
    }
    @PutMapping("/updateMenu")
    BaseResponse<Boolean> updateMenu(@Valid @RequestBody Recruitment menu, HttpServletRequest request) {
        User user = userBusinessService.checkBus(request);
        if(!user.getId().equals(menu.getBusIdRecru())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long id = menu.getId();
        Recruitment recruitment = new Recruitment();
        recruitment.setId(id);
        recruitment.setTitle(menu.getTitle());
        recruitment.setSalary(menu.getSalary());
        recruitment.setCompany(menu.getCompany());
        recruitment.setCount(menu.getCount());
        recruitment.setTags(menu.getTags());
        recruitment.setJobDetails(menu.getJobDetails());
        return ResultUtils.success(recruitmentService.updateById(recruitment));

    }
    @GetMapping("/listJobApp")
    BaseResponse<List<JobApplication>> listJobApp(HttpServletRequest request) {
        return ResultUtils.success(userBusinessService.listJobApplication(request));
    }
    @PostMapping("/dealJobApp")
    BaseResponse<Boolean> dealApplication(HttpServletRequest request,@RequestParam Boolean choose,@RequestBody JobApplication jobApplication) {
        return ResultUtils.success(userBusinessService.dealJobApp(request, choose, jobApplication));
    }
    @GetMapping("/chat/contacts")
    BaseResponse<Set<UserWithStudent>> getContacts(HttpServletRequest request) {
        Set<UserWithStudent> contacts = userBusinessService.getContacts(request);
        return ResultUtils.success(contacts);
    }
}
