package fun.xiabiqing.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.RecruitmentMapper;
import fun.xiabiqing.model.domain.Recruitment;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.RecruitmentService;
import fun.xiabiqing.service.UserBusinessService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author 26324
* @description 针对表【recruitment(招聘表)】的数据库操作Service实现
* @createDate 2026-01-02 15:58:51
*/
@Service
public class RecruitmentServiceImpl extends ServiceImpl<RecruitmentMapper, Recruitment>
    implements RecruitmentService {
    @Autowired
    private UserBusinessService userBusinessService;
    @Override
    public Boolean createMenu(Recruitment menu,HttpServletRequest request) {
        User user = userBusinessService.checkBus(request);
        Recruitment recruitment = new Recruitment();
        recruitment.setBusIdRecru(user.getId());
        recruitment.setTitle(menu.getTitle());
        recruitment.setSalary(menu.getSalary());
        recruitment.setCompany(menu.getCompany());
        recruitment.setCount(menu.getCount());
        recruitment.setTags(menu.getTags());
        recruitment.setJobDetails(menu.getJobDetails());
        return this.save(recruitment);
    }
}




