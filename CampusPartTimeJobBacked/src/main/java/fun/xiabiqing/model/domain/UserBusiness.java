package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商家
 * @TableName user_business
 */
@TableName(value ="user_business")
@Data
public class UserBusiness implements Serializable {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外键
     */
    private Long userIdBus;

    /**
     * 职位
     */
    private String position;

    /**
     * 位置
     */
    private String location;

    /**
     * 公司
     */
    private String company;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}