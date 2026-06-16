package {{basePackage}}.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 自定义数据库健康检查指示器
 * <p>
 * 在 /actuator/health 端点中显示数据库连接状态
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return Health.up()
                        .withDetail("database", conn.getMetaData().getDatabaseProductName())
                        .withDetail("url", conn.getMetaData().getURL())
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.down().withDetail("error", "连接验证失败").build();
    }
}
