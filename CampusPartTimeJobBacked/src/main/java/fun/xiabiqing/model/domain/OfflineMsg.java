package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 离线消息暂存表
 * @TableName offline_msg
 */
@TableName(value ="offline_msg")
@Data
public class OfflineMsg implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送方ID
     */
    private Long sendUserId;

    /**
     * 发送方角色(student/business)
     */
    private Integer sendRole;

    /**
     * 接收方ID（离线用户）
     */
    private Long receiveUserId;

    /**
     * 离线消息内容
     */
    private String msgContent;

    /**
     * 消息发送时间
     */
    private Date createTime;

    /**
     * 是否已推送（0=未推送，1=已推送）
     */
    private Integer isPush;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}