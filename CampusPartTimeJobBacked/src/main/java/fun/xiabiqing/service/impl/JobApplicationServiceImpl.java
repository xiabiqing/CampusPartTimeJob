package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.mapper.JobApplicationMapper;
import fun.xiabiqing.model.domain.JobApplication;
import fun.xiabiqing.service.JobApplicationService;
import org.springframework.stereotype.Service;

/**
* @author 26324
* @description 针对表【job_application(申请表)】的数据库操作Service实现
* @createDate 2026-01-02 20:45:38
*/
@Service
public class JobApplicationServiceImpl extends ServiceImpl<JobApplicationMapper, JobApplication>
    implements JobApplicationService {

}




