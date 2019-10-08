package org.fs.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件存储参数
 *
 */
@Getter
@Setter
@ConfigurationProperties("file-storage")
public class FileStorageProperties {

    /**
     * 默认存储类型
     */
    private String defaultType = "swift";

    /**
     * 存储类型映射
     */
    private Map<String, FileStorageType> types = new LinkedHashMap<>();

    /**
     * 存储类型
     *
     */
    @Getter
    @Setter
    public static class FileStorageType {
        /**
         * 类型
         */
        private String type = "swift";
        /**
         * 路径
         */
        private String path;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
    }
}
