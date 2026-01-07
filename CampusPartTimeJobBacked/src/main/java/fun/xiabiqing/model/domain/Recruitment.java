package fun.xiabiqing.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 招聘表
 * @TableName recruitment
 */
@TableName(value ="recruitment", autoResultMap = true)
@Data
public class Recruitment implements Serializable {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外键
     */
    private Long busIdRecru;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 30,message = "标题字数在1~30字之间")
    private String title;

    /**
     * 薪水
     */
    @Min(value = 0,message = "最低薪资为0")
    private Long salary;

    /**
     * 公司
     */
    @NotBlank(message = "公司名称不能为空")
    @Size(min=1,max = 30,message = "公司名称在1~30之间")
    private String company;

    /**
     * 招聘人数
     */
    @Min(value = 1,message = "招聘人数最低为一人")
    private Integer count;

    /**
     * 标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 岗位描述
     */
    @NotBlank
    @Size(min = 10,message = "岗位详细描述信息不能为空")
    private String jobDetails;

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
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}