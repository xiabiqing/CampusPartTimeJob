package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息记录表
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@Data
public class ChatMessage implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送方用户ID
     */
    private Long sendUserId;

    /**
     * 发送方角色：0-学生，1-商家
     */
    private Integer sendRole;

    /**
     * 接收方用户ID
     */
    private Long receiveUserId;

    /**
     * 接收方角色：0-学生，1-商家
     */
    private Integer receiveRole;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 消息创建时间，自动填充
     */
    private Date createTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}