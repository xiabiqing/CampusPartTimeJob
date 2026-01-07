package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.UserStudentMapper;
import fun.xiabiqing.model.domain.*;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import fun.xiabiqing.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 26324
* @description 针对表【user_student(学生表)】的数据库操作Service实现
* @createDate 2025-12-30 17:48:04
*/
@Service
public class UserStudentServiceImpl extends ServiceImpl<UserStudentMapper, UserStudent>
    implements UserStudentService {
    @Autowired
    private RecruitmentService recruitmentService;
    @Autowired
    private JobApplicationService jobApplicationService;
    @Autowired
    private UserBusinessService userBusinessService;
    @Autowired
    private UserService userService;
    @Override
    public List<Recruitment> displayRecruitment(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        return recruitmentService.list(recruitmentQueryWrapper);
    }

    @Override
    public Boolean sendJobApplication(Recruitment recruitment, HttpServletRequest request, JobApplication jobApplication) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = recruitment.getId();
        Long userId = user.getId();
        JobApplication todoJobApplication = new JobApplication();
        todoJobApplication.setStuId(userId);
        todoJobApplication.setRecruId(id);
        todoJobApplication.setInformation(jobApplication.getInformation());
        todoJobApplication.setStatus(UserConstant.RUNNING);
        return jobApplicationService.save(todoJobApplication);

    }
    @Override
    public List<JobApplication> listJobApplication(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long userId = user.getId();
        QueryWrapper<JobApplication> jobApplicationQueryWrapper = new QueryWrapper<>();
        jobApplicationQueryWrapper.eq("stu_id", userId);
        return jobApplicationService.list(jobApplicationQueryWrapper);
    }
    @Override
    public Set<UserWithBusiness> getContacts(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        /*
        查询到所有的工作申请单
         */
        QueryWrapper<JobApplication> jobApplicationQueryWrapper = new QueryWrapper<>();
        jobApplicationQueryWrapper.eq("stu_id", user.getId());
        List<JobApplication> jobList = jobApplicationService.list(jobApplicationQueryWrapper);
        /*
        通过工作申请单获取所有的兼职单外键recru_id
         */
        List<Long> recruIdList = jobList.stream().map(JobApplication::getRecruId).toList();
        QueryWrapper<Recruitment> recruitmentQueryWrapper = new QueryWrapper<>();
        if(recruIdList.isEmpty()){
            return new HashSet<>();
        }
        recruitmentQueryWrapper.in("id", recruIdList);
        /*
        查询到所有的兼职单了（申请了工作的）
         */
        List<Recruitment> list = recruitmentService.list(recruitmentQueryWrapper);
        /*
        也是过滤了一下商家id
         */
        Set<Long> busUserIdList = list.stream().map(Recruitment::getBusIdRecru).collect(Collectors.toSet());
        QueryWrapper<UserBusiness> userBusinessQueryWrapper = new QueryWrapper<>();
        if(busUserIdList.isEmpty()){
            return new HashSet<>();
        }
        // 修复：busIdRecru 是 User 表的ID，应该用 user_id_bus 字段查询 UserBusiness
        userBusinessQueryWrapper.in("user_id_bus", busUserIdList);
        /*
        查询到所有的商家了
         */
        List<UserBusiness> busList = userBusinessService.list(userBusinessQueryWrapper);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        HashSet<UserWithBusiness> userWithBusinesses = new HashSet<>();
        for (UserBusiness userBusiness : busList) {
            // 清空条件
            userQueryWrapper.clear();
            userQueryWrapper.eq("id", userBusiness.getUserIdBus());
            User newUser = userService.getOne(userQueryWrapper);
            if (newUser == null) {
                continue; // 如果用户不存在，跳过
            }
            UserWithBusiness userWithBusiness = new UserWithBusiness();
            userWithBusiness.setId(newUser.getId());
            userWithBusiness.setUsername(newUser.getUsername());
            userWithBusiness.setImage(newUser.getImage());
            userWithBusiness.setRole(newUser.getRole());
            userWithBusiness.setPosition(userBusiness.getPosition());
            userWithBusiness.setLocation(userBusiness.getLocation());
            userWithBusiness.setCompany(userBusiness.getCompany());
            userWithBusinesses.add(userWithBusiness);
        }
        return userWithBusinesses;
    }
}




