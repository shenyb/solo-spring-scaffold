package {{basePackage}}.web.user;

import {{basePackage}}.common.api.PageResult;
import {{basePackage}}.common.api.Result;
import {{basePackage}}.user.UserService;
import {{basePackage}}.user.dto.UserCreateRequest;
import {{basePackage}}.user.dto.UserResponse;
import {{basePackage}}.user.dto.UserUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 切片测试
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController 测试")
class {{ProjectName}}UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse buildResponse() {
        UserResponse r = new UserResponse();
        r.setId(1L);
        r.setUsername("testuser");
        r.setEmail("test@example.com");
        r.setStatus(1);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("查询用户详情 - 200")
        void shouldReturnUser() throws Exception {
            when(userService.getById(1L)).thenReturn(buildResponse());

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateTests {

        @Test
        @DisplayName("创建用户 - 200")
        void shouldCreateUser() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");

            when(userService.create(any(UserCreateRequest.class))).thenReturn(2L);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(2));
        }

        @Test
        @DisplayName("参数校验失败 - 400")
        void shouldReturn400_whenInvalidInput() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            // username 为空，应触发校验

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
