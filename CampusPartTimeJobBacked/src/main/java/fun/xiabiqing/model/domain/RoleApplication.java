package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色申请表
 * @TableName role_application
 */
@TableName(value ="role_application")
@Data
public class RoleApplication implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 默认用户id
     */
    private Long stuId;

    /**
     * 管理员id
     */
    @Min(value = 1)
    private Long adminId;
    /**
     * 申请成为的角色
     */
    private Integer role;
    /**
     * 申请信息
     */
    @Size(max = 1000)
    private String information;

    /**
     * 申请资政
     */
    @Size(max = 100)
    private String image;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}