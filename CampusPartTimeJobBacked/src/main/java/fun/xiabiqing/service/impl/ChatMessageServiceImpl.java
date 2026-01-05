package fun.xiabiqing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.xiabiqing.mapper.ChatMessageMapper;
import fun.xiabiqing.model.domain.ChatMessage;
import fun.xiabiqing.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
* @author 26324
* @description 针对表【chat_message(聊天消息记录表)】的数据库操作Service实现
* @createDate 2026-01-03 20:01:42
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {

}




