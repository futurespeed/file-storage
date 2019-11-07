package org.fs.autoconfig.file;

import org.fs.file.FileStorageTemplate;
import org.fs.file.FileUploadConfig;
import org.fs.file.FileUploadTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;

/**
 * 文件上传自动配置
 *
 *
 */
@Configuration
@ConditionalOnClass({MultipartResolver.class})
@AutoConfigureAfter({FileStorageAutoConfiguration.class, MultipartAutoConfiguration.class})
@EnableConfigurationProperties({UploadProperties.class})
public class FileUploadAutoConfiguration {
    private UploadProperties uploadProperties;

    public FileUploadAutoConfiguration(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;

        final String defaultType = "default";
        if (null == uploadProperties.getTypes().get(defaultType)) {
            FileUploadConfig defaultConfig = new FileUploadConfig();
            defaultConfig.setFileCode(defaultType);
            defaultConfig.setStorageKind(FileUploadConfig.STORAGE_KIND_TEMP);
            defaultConfig.setMaxSize("50m");
            uploadProperties.getTypes().put(defaultType, defaultConfig);
        }
    }

    @Bean
    @ConditionalOnBean({FileStorageTemplate.class, MultipartResolver.class})
    @ConditionalOnMissingBean
    public FileUploadTemplate fileUploadTemplate(FileStorageTemplate fileStorageTemplate) {
        FileUploadTemplate template = new FileUploadTemplate();
        template.setFileStorageTemplate(fileStorageTemplate);
        template.setUploadConfigs(uploadProperties.getTypes());
        return template;
    }
}
