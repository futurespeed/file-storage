package org.fs.autoconfig.file;

import org.fs.file.FileInfo;
import org.fs.file.FileInfoHandler;
import org.fs.file.FileStorageTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 *
 *
 */
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfiguration {

    private FileStorageProperties fileStorageProperties;

    FileStorageAutoConfiguration(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public FileInfoHandler defaultFileInfoHandler() {
        final String errorMsg = "请配置文件信息处理器（FileInfoHandler）";
        return new FileInfoHandler() {
            public FileInfo getFileInfo(String id) {
                throw new IllegalArgumentException(errorMsg);
            }

            public void saveFileInfo(FileInfo fileInfo) {
                throw new IllegalArgumentException(errorMsg);
            }

            public void deleteFileInfo(String id) {
                throw new IllegalArgumentException(errorMsg);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStorageTemplate fileStorageTemplate(FileInfoHandler fileInfoHandler) {
        FileStorageTemplate template = new FileStorageTemplate();
        template.setDefaultStorageType(fileStorageProperties.getDefaultType());
        template.setFileInfoHandler(fileInfoHandler);
        return template;
    }
}
