package fun.xiabiqing.constant;

public interface UserConstant {
    String USER_LOGIN_STATUS = "user_login_status";
    /**
     * 学生 0
     * 商家 1
     * 管理员 2
     */
    Integer STUDENT=0;
    Integer BUSINESS=1;
    Integer MANAGER=2;
    /**
     * 正在处理状态 0
     * 同意成功 1
     * 拒绝失败 -1
     */
    Integer RUNNING=0;
    Integer SUCCESS=1;
    Integer FLAIL=-1;
}
