package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.UserBusinessMapper;
import fun.xiabiqing.model.domain.*;
import fun.xiabiqing.model.vo.JobApplicationResponse;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import fun.xiabiqing.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 26324
* @description 针对表【user_business(商家)】的数据库操作Service实现
* @createDate 2026-01-02 14:27:58
*/
@Service
public class UserBusinessServiceImpl extends ServiceImpl<UserBusinessMapper, UserBusiness>
    implements UserBusinessService {
    @Autowired
    private JobApplicationService jobApplicationService;
    @Autowired
    private RecruitmentService recruitmentService;
    @Autowired
    private UserStudentService userStudentService;
    @Autowired
    private UserService userService;

    public User checkBus(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (!UserConstant.BUSINESS.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return user;
    }

    @Override
    public List<JobApplication> listJobApplication(HttpServletRequest request) {
        User user = this.checkBus(request);
        Long userId = user.getId();
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        recruitmentQueryWrapper.eq("bus_id_recru", userId);
        List<Recruitment> recruList = recruitmentService.list(recruitmentQueryWrapper);
        List<Long> recruIdList = recruList.stream().map(Recruitment::getId).toList();
        if (recruIdList == null || recruIdList.isEmpty()) {
            return List.of();
        }
        QueryWrapper<JobApplication> jobApplicationQueryWrapper = new QueryWrapper<>();
        jobApplicationQueryWrapper.in("recru_id", recruIdList);
        List<JobApplication> jobApplicationList = jobApplicationService.list(jobApplicationQueryWrapper);
        return jobApplicationList;
    }

    @Override
    public Boolean dealJobApp(HttpServletRequest request, Boolean choose, JobApplication jobApplication) {
        /**
         * 基本校验
         */
        User bus = this.checkBus(request);
        /**
         * 每个商家只能处理对应自己的工作申请
         */

        Long recruId = jobApplication.getRecruId();
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        recruitmentQueryWrapper.eq("id", recruId);
        Recruitment Recru = recruitmentService.getOne(recruitmentQueryWrapper);
        Long busIdRecru = Recru.getBusIdRecru();
        if (!busIdRecru.equals(bus.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        /**
         * 申请表查申请者id，定位到用户表的id，进行role的修改，并同时修改申请表的status
         */
        JobApplication newJobApplication = new JobApplication();
        newJobApplication.setId(jobApplication.getId());
        if (choose) {
            // 同意：状态设置为1（申请成功）
            newJobApplication.setStatus(UserConstant.SUCCESS);
            boolean result = jobApplicationService.updateById(newJobApplication);
            return result;
        }
        // 拒绝：状态设置为2（申请失败），根据JobApplication实体注释：0正在申请中 1申请成功 2申请失败
        newJobApplication.setStatus(2);
        boolean result = jobApplicationService.updateById(newJobApplication);
        return result;
    }

    @Override
    public Set<UserWithStudent> getContacts(HttpServletRequest request) {
        User user = checkBus(request);
        Long userId = user.getId();
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        /*
        通过外键关联查找对应商家的的所有兼职单
         */
        recruitmentQueryWrapper.eq("bus_id_recru", userId);
        List<Recruitment> recruList = recruitmentService.list(recruitmentQueryWrapper);
        List<Long> recruIdList = recruList.stream().map(Recruitment::getId).toList();
        if (recruIdList.isEmpty()) {
            return Set.of();
        }
        QueryWrapper<JobApplication> jobApplicationQueryWrapper = new QueryWrapper<>();
         /*
         获得这个商家所有兼职单的所有申请单
          */
        jobApplicationQueryWrapper.in("recru_id", recruIdList);
        List<JobApplication> jobApplicationList = jobApplicationService.list(jobApplicationQueryWrapper);
        /*
        获取所有关联学生id的外键stu_id(一个学生可能申请多个，所以用set去重一下)
         */
        Set<Long> stuIdList = jobApplicationList.stream().map(JobApplication::getStuId).collect(Collectors.toSet());
        QueryWrapper<UserStudent> userStudentQueryWrapper = new QueryWrapper<>();
        if(stuIdList.isEmpty()) {
            return Set.of();
        }
        /*
        查询到所有的学生记录了
         */
        userStudentQueryWrapper.in("user_id_stu", stuIdList);
        List<UserStudent> stuList = userStudentService.list(userStudentQueryWrapper);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        HashSet<UserWithStudent> userWithStudents = new HashSet<>();
        for (UserStudent userStudent : stuList) {
            //每次清空查找，负责会拼接and条件
            userQueryWrapper.clear();
            userQueryWrapper.eq("id", userStudent.getUserIdStu());
            User newUser = userService.getOne(userQueryWrapper);
            if (newUser == null) {
                continue; // 如果用户不存在，跳过
            }
            UserWithStudent userWithStudent = new UserWithStudent();
            userWithStudent.setId(newUser.getId());
            userWithStudent.setUsername(newUser.getUsername());
            userWithStudent.setImage(newUser.getImage());
            userWithStudent.setAge(userStudent.getAge());
            userWithStudent.setRole(newUser.getRole());
            userWithStudent.setGender(userStudent.getGender());
            userWithStudent.setMajor(userStudent.getMajor());
            userWithStudent.setEmail(userStudent.getEmail());
            userWithStudent.setSelfIntroduction(userStudent.getSelfIntroduction());
            userWithStudents.add(userWithStudent);
        }
        return userWithStudents;

    }
}



