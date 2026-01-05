package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生表
 * @TableName user_student
 */
@TableName(value ="user_student")
@Data
public class UserStudent implements Serializable {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外键
     */
    private Long userIdStu;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别 0男 1女
     */
    private Integer gender;

    /**
     * 专业
     */
    private String major;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 自我介绍
     */
    private String selfIntroduction;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}