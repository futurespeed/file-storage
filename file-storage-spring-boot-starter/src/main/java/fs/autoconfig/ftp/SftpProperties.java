package fs.autoconfig.ftp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SFTP自动配置参数
 *
 *
 */
@Getter
@Setter
@ConfigurationProperties("fs.sftp")
public class SftpProperties {
    /**
     * 连接超时时间（单位：毫秒）
     */
    private int connectTimeout = 3000;
}
