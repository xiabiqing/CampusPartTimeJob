package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.RoleApplication;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 26324
* @description 针对表【role_application(角色申请表)】的数据库操作Service
* @createDate 2025-12-31 11:41:52
*/
public interface RoleApplicationService extends IService<RoleApplication> {
    /**
     * 角色迁移
     * @param roleApplication 申请表
     * @param request 请求
     * @return 状态
     */
    Integer identityPorting(RoleApplication roleApplication, HttpServletRequest request);
}
