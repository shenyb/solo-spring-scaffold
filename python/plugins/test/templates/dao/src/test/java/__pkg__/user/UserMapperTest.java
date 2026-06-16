package {{basePackage}}.dao;

import {{basePackage}}.user.User;
import {{basePackage}}.user.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserMapper 集成测试（使用 H2 内存库）
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@DisplayName("UserMapper 集成测试")
class {{ProjectName}}UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("插入并查询用户")
    void shouldInsertAndSelect() {
        User user = new User();
        user.setUsername("integ_test");
        user.setEmail("integ@test.com");
        user.setStatus(1);

        userMapper.insert(user);
        assertThat(user.getId()).isNotNull();

        User found = userMapper.selectById(user.getId());
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("integ_test");
    }

    @Test
    @DisplayName("按用户名查询")
    void shouldSelectByUsername() {
        User user = new User();
        user.setUsername("find_me");
        user.setEmail("find@test.com");
        user.setStatus(1);
        userMapper.insert(user);

        User found = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "find_me"));

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("find@test.com");
    }
}
