package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.mapper.TagMapper;
import fun.xiabiqing.model.domain.Tag;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.TagService;
import fun.xiabiqing.service.UserBusinessService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 26324
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2026-01-02 16:33:28
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {
    @Autowired
    private UserBusinessService userBusinessService;
    public List<Tag> getTagsList(HttpServletRequest request) {
        userBusinessService.checkBus(request);
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        List<Tag> list = this.list(tagQueryWrapper);
        return list;
    }
}




