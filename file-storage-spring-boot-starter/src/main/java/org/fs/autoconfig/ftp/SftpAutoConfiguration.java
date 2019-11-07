package org.fs.autoconfig.ftp;

import com.jcraft.jsch.JSch;
import org.fs.ftp.SftpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SFTP自动配置
 *
 *
 */
@Configuration
@EnableConfigurationProperties(SftpProperties.class)
@ConditionalOnClass(JSch.class)
public class SftpAutoConfiguration {

    private SftpProperties sftpProperties;

    SftpAutoConfiguration(SftpProperties sftpProperties) {
        this.sftpProperties = sftpProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SftpTemplate sftpTemplate() {
        SftpTemplate template = new SftpTemplate();
        template.setConnectTimeout(sftpProperties.getConnectTimeout());
        return template;
    }
}
