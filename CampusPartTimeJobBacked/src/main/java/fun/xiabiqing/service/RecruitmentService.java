package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.Recruitment;
import jakarta.servlet.http.HttpServletRequest;


/**
* @author 26324
* @description 针对表【recruitment(招聘表)】的数据库操作Service
* @createDate 2026-01-02 15:58:51
*/
public interface RecruitmentService extends IService<Recruitment> {
    /**
     * 创建兼职订单
     * @param menu 订单信息
     * @param request 请求
     * @return 成功或失败
     */
    Boolean createMenu(Recruitment menu,HttpServletRequest request);

}
