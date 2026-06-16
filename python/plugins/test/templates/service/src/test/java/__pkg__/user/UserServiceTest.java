package {{basePackage}}.user;

import {{basePackage}}.common.api.PageResult;
import {{basePackage}}.common.exception.BizException;
import {{basePackage}}.user.dto.UserCreateRequest;
import {{basePackage}}.user.dto.UserResponse;
import {{basePackage}}.user.dto.UserUpdateRequest;
import {{basePackage}}.user.impl.UserServiceImpl;
import {{basePackage}}.user.UserMapper;
import {{basePackage}}.user.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 测试")
class {{ProjectName}}UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getById 测试")
    class GetByIdTests {

        @Test
        @DisplayName("正常查询用户")
        void shouldReturnUser_whenExists() {
            when(userMapper.selectById(1L)).thenReturn(testUser);

            UserResponse response = userService.getById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrow_whenNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> userService.getById(999L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Nested
    @DisplayName("page 测试")
    class PageTests {

        @Test
        @DisplayName("分页查询返回正确结果")
        void shouldReturnPagedResult() {
            Page<User> page = new Page<>(1, 10);
            page.setRecords(List.of(testUser));
            page.setTotal(1);

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            PageResult<UserResponse> result = userService.page(1, 10);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("create 测试")
    class CreateTests {

        @Test
        @DisplayName("创建用户成功")
        void shouldCreateUser() {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");

            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return 1;
            });

            Long id = userService.create(request);

            assertThat(id).isEqualTo(2L);
            verify(userMapper).insert(argThat(user ->
                    user.getUsername().equals("newuser") &&
                    user.getEmail().equals("new@example.com")
            ));
        }
    }

    @Nested
    @DisplayName("delete 测试")
    class DeleteTests {

        @Test
        @DisplayName("删除用户成功")
        void shouldDeleteUser() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.deleteById(1L)).thenReturn(1);

            assertThatCode(() -> userService.delete(1L)).doesNotThrowAnyException();
            verify(userMapper).deleteById(1L);
        }

        @Test
        @DisplayName("删除不存在的用户抛出异常")
        void shouldThrow_whenDeletingNonExistent() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> userService.delete(999L))
                    .isInstanceOf(BizException.class);
            verify(userMapper, never()).deleteById(anyLong());
        }
    }
}
