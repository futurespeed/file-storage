package org.fs.autoconfigure;

import org.fs.autoconfigure.web.SwiftAutoConfiguration;
import org.fs.common.Callback;
import org.fs.file.FileInfo;
import org.fs.file.FileStorageHandler;
import org.fs.file.FileStorageTemplate;
import org.fs.web.SwiftTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * K2-Swift存储自动配置
 *
 */
@Configuration
@ConditionalOnClass(SwiftTemplate.class)
@ConditionalOnMissingClass({"com.youcash.ucff.filestorage.swift.template.SwiftTemplate"})
@AutoConfigureAfter({FileStorageAutoConfiguration.class, SwiftAutoConfiguration.class})
@AutoConfigureBefore({GroupFileStorageAutoConfiguration.class})
public class K2SwiftFileStorageAutoConfiguration {

    @Bean
    @ConditionalOnBean({FileStorageTemplate.class, SwiftTemplate.class})
    @ConditionalOnMissingBean(name = "swiftFileStorageHandlerMap")
    public Map<String, FileStorageHandler> swiftFileStorageHandlerMap(FileStorageProperties fileStorageProperties,
                                                                      FileStorageTemplate fileStorageTemplate,
                                                                      SwiftTemplate swiftTemplate) {
        Map<String, FileStorageHandler> map = new LinkedHashMap<>();
        for (Map.Entry<String, FileStorageProperties.FileStorageType> entry :
                fileStorageProperties.getTypes().entrySet()) {
            if ("swift".equals(entry.getValue().getType())) {
                SwiftFileStorageHandler handler = new SwiftFileStorageHandler(entry.getValue().getPath(),
                        swiftTemplate);
                map.put(entry.getKey(), handler);
            }
        }
        fileStorageTemplate.getFileStorageHandlerMap().putAll(map);
        return map;
    }

    /**
     * Swift文件存储控制器
     *
     */
    public static class SwiftFileStorageHandler implements FileStorageHandler {

        private String parentPath;

        private SwiftTemplate swiftTemplate;

        public SwiftFileStorageHandler(String parentPath, SwiftTemplate swiftTemplate) {
            this.parentPath = parentPath;
            this.swiftTemplate = swiftTemplate;
        }

        @Override
        public void storageFile(FileInfo fileInfo, InputStream in) {
            swiftTemplate.put(parentPath + "/" + fileInfo.getFilePath(), fileInfo.getFileSize(), in);
        }

        @Override
        public <T> T storageFile(FileInfo fileInfo, Callback<OutputStream, T> callback) {
            return swiftTemplate.put(parentPath + "/" + fileInfo.getFilePath(), fileInfo.getFileSize(), callback);
        }

        @Override
        public void getFile(FileInfo fileInfo, OutputStream out) {
            swiftTemplate.get(parentPath + "/" + fileInfo.getFilePath(), out);
        }

        @Override
        public <T> T getFile(FileInfo fileInfo, Callback<InputStream, T> callback) {
            return swiftTemplate.get(parentPath + "/" + fileInfo.getFilePath(), callback);
        }

        @Override
        public void deleteFile(FileInfo fileInfo) {
            swiftTemplate.delete(parentPath + "/" + fileInfo.getFilePath());
        }
    }
}
