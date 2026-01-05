package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 申请表
 * @TableName job_application
 */
@TableName(value ="job_application")
@Data
public class JobApplication implements Serializable {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long stuId;

    /**
     * 
     */
    private Long recruId;

    /**
     * 申请信息
     */
    @NotBlank(message = "申请信息不能为空")
    private String information;

    /**
     * 申请状态 0正在申请中 1申请成功 2申请失败
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