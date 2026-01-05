package fun.xiabiqing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.xiabiqing.model.domain.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 26324
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2026-01-02 16:33:28
*/
public interface TagService extends IService<Tag> {
    /**
     * 展示所有标签
     * @param request 请求
     * @return 所有标签
     */
    List<Tag> getTagsList(HttpServletRequest request);
}
