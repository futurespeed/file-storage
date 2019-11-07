package org.fs.autoconfig.web;

import org.fs.web.SwiftTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * swift自动配置
 */
@Configuration
@EnableConfigurationProperties(SwiftProperties.class)
public class SwiftAutoConfiguration {

    private SwiftProperties swiftProperties;

    SwiftAutoConfiguration(SwiftProperties swiftProperties){
        this.swiftProperties = swiftProperties;
    }

    @Bean("k2SwiftTemplate")
    @ConditionalOnMissingBean
    public SwiftTemplate k2SwiftTemplate(){
        SwiftTemplate template = new SwiftTemplate();
        template.setConnectTimeout(swiftProperties.getConnectTimeout());
        template.setReadTimeout(swiftProperties.getReadTimeout());
        return template;
    }
}
