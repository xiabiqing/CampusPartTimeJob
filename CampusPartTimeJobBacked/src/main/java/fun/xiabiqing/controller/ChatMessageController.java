package fun.xiabiqing.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.xiabiqing.common.BaseResponse;
import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.constant.UserConstant;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.model.domain.ChatMessage;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.ChatMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatMessageController {
    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/chat/history/{targetUserId}")
    public BaseResponse<List<ChatMessage>> history(@PathVariable String targetUserId, HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        
        Long currentUserId = user.getId();
        Long targetId = Long.valueOf(targetUserId);
        
        QueryWrapper<ChatMessage> chatMessageQueryWrapper = new QueryWrapper<>();
        // 查询条件：当前用户发送给目标用户 或 目标用户发送给当前用户
        chatMessageQueryWrapper.and(wrapper -> wrapper
                        .eq("send_user_id", currentUserId)
                        .eq("receive_user_id", targetId)
                )
                .or(wrapper -> wrapper
                        .eq("send_user_id", targetId)
                        .eq("receive_user_id", currentUserId)
                )
                .orderByAsc("create_time");
        List<ChatMessage> list = chatMessageService.list(chatMessageQueryWrapper);
        return new BaseResponse<>(list);
    }
}
