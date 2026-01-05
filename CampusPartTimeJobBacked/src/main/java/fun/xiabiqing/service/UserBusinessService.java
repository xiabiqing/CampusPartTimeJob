package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.model.domain.*;
import fun.xiabiqing.model.vo.UserWithBusiness;
import fun.xiabiqing.model.vo.UserWithStudent;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

/**
* @author 26324
* @description 针对表【user_business(商家)】的数据库操作Service
* @createDate 2026-01-02 14:16:08
*/
public interface UserBusinessService extends IService<UserBusiness> {
    /**
     * 确定是商家
     * @param request 请求
     */
    User checkBus(HttpServletRequest request);

    /**
     * 列出所有工作申请单
     * @param request 请求
     * @return 所有工作申请单
     */
    List<JobApplication> listJobApplication(HttpServletRequest request);
    /**
     *  管理员处理申请表
     * @param request 请求
     * @param choose 同意/拒绝
     * @param jobApplication 申请表单
     * @return 状态(choose)
     */
    Boolean dealJobApp(HttpServletRequest request, Boolean choose, JobApplication jobApplication);

    Set<UserWithStudent> getContacts(HttpServletRequest request);
}
