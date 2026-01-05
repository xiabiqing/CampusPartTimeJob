package fun.xiabiqing.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobApplicationResponse {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long stuId;
    /**
     * 学生姓名
     */
    private String stuName;

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

}
