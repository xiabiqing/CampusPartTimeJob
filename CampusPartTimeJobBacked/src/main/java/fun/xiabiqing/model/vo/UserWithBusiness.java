package fun.xiabiqing.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserWithBusiness {
    private Long id;

    /**
     * 用户名
     */
    @NotBlank
    @Size(min = 1, max = 10,message = "用户名长度不符合要求")
    private String username;

    /**
     * 头像
     */
    private String image;
    /**
     * 角色
     */
    private Integer role;
    /**
     * 职位
     */
    @NotBlank
    @Size(min = 1,max = 10,message = "职位描述长度为1~10")
    private String position;

    /**
     * 位置
     */
    @NotBlank
    @Size(min = 1,max = 30,message = "职位描述长度为1~30")
    private String location;

    /**
     * 公司
     */
    @NotBlank
    @Size(min = 1,max = 30,message = "职位描述长度为1~30")
    private String company;
}
