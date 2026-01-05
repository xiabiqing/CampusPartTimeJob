package fun.xiabiqing;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

@SpringBootTest
class CampusPartTimeJobBackedApplicationTests {

    @Autowired
    private UserService userService;
    @Test
    void mybatisPlusTest() {
        /*User user = new User();
        user.setUsername("李玉");
        user.setUserAccount("2222222");
        user.setUserPassword("2632493933.");
        user.setRole(0);
        user.setCreat_time(new Date());
        user.setUpdate_time(new Date());
        user.setIs_delete(0);
        userService.save(user);*/
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> list = userService.list(userQueryWrapper);
        System.out.println(list.toString());

    }


}
