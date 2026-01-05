package fun.xiabiqing.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class UserWithStudent {
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
    @NotNull
    @Range(min = 0, max = 150, message = "年龄必须在0-150岁之间")
    private Integer age;
    /**
     * 角色
     */
    private Integer role;
    /**
     * 性别 0男 1女
     */
    @NotNull
    private Integer gender;

    /**
     * 专业
     */
    @NotBlank
    @Size(max = 20,message = "长度不合规范")
    private String major;

    /**
     * 邮箱
     */
    @NotBlank
    @Size(max = 20,message = "长度不合规范")
    private String email;

    /**
     * 自我介绍
     */
    @Size(max = 200,message = "长度不合规范")
    private String selfIntroduction;
}
