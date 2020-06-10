package fs.ftp;

import lombok.Getter;
import lombok.Setter;

/**
 * SFTP 参数
 *
 *
 */
@Getter
@Setter
public class SftpSetting {
    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port = 22;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 密钥
     */
    private String privateKey;
    /**
     * 连接超时时间（单位：毫秒）
     */
    private String connectTimeout;
}
