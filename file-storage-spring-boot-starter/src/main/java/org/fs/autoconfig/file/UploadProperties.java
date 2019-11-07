package org.fs.autoconfig.file;

import org.fs.file.FileUploadConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件上传参数
 *
 *
 */
@ConfigurationProperties("fs.upload")
public class UploadProperties {

    /**
     * 上传配置映射
     */
    private Map<String, FileUploadConfig> types = new LinkedHashMap<>();

    public Map<String, FileUploadConfig> getTypes() {
        return types;
    }

    public void setTypes(Map<String, FileUploadConfig> types) {
        this.types = types;
    }
}
