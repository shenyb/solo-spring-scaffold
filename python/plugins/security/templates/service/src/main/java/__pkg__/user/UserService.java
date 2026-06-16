package {{basePackage}}.user;

import {{basePackage}}.common.api.PageResult;
import {{basePackage}}.user.dto.UserCreateRequest;
import {{basePackage}}.user.dto.UserResponse;
import {{basePackage}}.user.dto.UserUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;

public interface UserService {

    @PreAuthorize("hasRole('USER')")
    UserResponse getById(Long id);

    @PreAuthorize("hasRole('USER')")
    PageResult<UserResponse> page(int page, int size);

    @PreAuthorize("hasRole('ADMIN')")
    Long create(UserCreateRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void update(UserUpdateRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void delete(Long id);
}
