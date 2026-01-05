package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.JobApplication;
import fun.xiabiqing.model.domain.Recruitment;
import fun.xiabiqing.model.domain.UserBusiness;
import fun.xiabiqing.model.domain.UserStudent;
import fun.xiabiqing.model.vo.UserWithBusiness;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

/**
* @author 26324
* @description 针对表【user_student(学生表)】的数据库操作Service
* @createDate 2025-12-30 17:48:04
*/
public interface UserStudentService extends IService<UserStudent> {
    /**
     * 展示所有兼职订单信息
     * @param request
     * @return
     */
    List<Recruitment> displayRecruitment(HttpServletRequest request);

    /**
     * 发送兼职(工作)申请
     * @param recruitment
     * @param request
     * @return
     */
    Boolean sendJobApplication(Recruitment recruitment, HttpServletRequest request, JobApplication jobApplication);
    /**
     * 列出所有工作申请单
     * @param request 请求
     * @return 所有工作申请单
     */
    List<JobApplication> listJobApplication(HttpServletRequest request);
    Set<UserWithBusiness> getContacts(HttpServletRequest request);
}
