package fun.xiabiqing.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUser {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max =20,message = "用户名长度在6~20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "用户名仅支持字母、数字、下划线(_)和(.)，禁止输入其他字符")
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8,max = 20,message = "密码长度在8~20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "密码仅支持字母、数字、下划线(_)和(.)，禁止输入其他字符")
    private String userPassword;
    /**
     * 校验码
     */
    @NotBlank(message = "检验码不能为空")
    private  String checkPassword;
}
