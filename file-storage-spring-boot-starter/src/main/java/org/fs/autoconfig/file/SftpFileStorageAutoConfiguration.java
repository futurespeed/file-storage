package org.fs.autoconfig.file;

import org.fs.autoconfig.ftp.SftpAutoConfiguration;
import org.fs.common.Callback;
import org.fs.file.FileInfo;
import org.fs.file.FileStorageHandler;
import org.fs.file.FileStorageTemplate;
import org.fs.file.FileUtils;
import org.fs.ftp.SftpSetting;
import org.fs.ftp.SftpTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SFTP存储自动配置
 *
 *
 */
@Configuration
@ConditionalOnClass(SftpTemplate.class)
@AutoConfigureAfter({FileStorageAutoConfiguration.class, SftpAutoConfiguration.class})
@AutoConfigureBefore({GroupFileStorageAutoConfiguration.class})
public class SftpFileStorageAutoConfiguration {

    @Bean
    @ConditionalOnBean({FileStorageTemplate.class, SftpTemplate.class})
    @ConditionalOnMissingBean(name = "sftpFileStorageHandlerMap")
    public Map<String, FileStorageHandler> sftpFileStorageHandlerMap(FileStorageProperties fileStorageProperties,
                                                                     FileStorageTemplate fileStorageTemplate,
                                                                     SftpTemplate sftpTemplate) {
        Map<String, FileStorageHandler> map = new LinkedHashMap<>();
        for (Map.Entry<String, FileStorageProperties.FileStorageType> entry :
                fileStorageProperties.getTypes().entrySet()) {
            if ("sftp".equals(entry.getValue().getType())) {
                SftpFileStorageHandler handler = new SftpFileStorageHandler(sftpTemplate, entry.getValue());
                map.put(entry.getKey(), handler);
            }
        }
        fileStorageTemplate.getFileStorageHandlerMap().putAll(map);
        return map;
    }

    /**
     * SFTP文件存储控制器
     *
     *
     */
    public static class SftpFileStorageHandler implements FileStorageHandler {

        private SftpTemplate sftpTemplate;

        private SftpSetting sftpSetting;

        private FileStorageProperties.FileStorageType fileStorageType;

        private String remotePath;

        public SftpFileStorageHandler(SftpTemplate sftpTemplate, FileStorageProperties.FileStorageType fileStorageType) {
            this.sftpTemplate = sftpTemplate;
            this.fileStorageType = fileStorageType;
            init();
        }

        private void init() {
            this.remotePath = fileStorageType.getPath().substring(fileStorageType.getPath().indexOf('/'));
            String hostPath = fileStorageType.getPath().substring(0, fileStorageType.getPath().indexOf('/'));
            String[] hostArr = hostPath.split(":");
            sftpSetting = new SftpSetting();
            sftpSetting.setHost(hostArr[0]);
            if (hostArr.length > 1) {
                sftpSetting.setPort(Integer.parseInt(hostArr[1]));
            }
            sftpSetting.setUsername(fileStorageType.getUsername());
            sftpSetting.setPassword(fileStorageType.getPassword());
        }

        @Override
        public void storageFile(FileInfo fileInfo, InputStream in) {
            String relativePath = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf('/'));
            String fileName = fileInfo.getFilePath().substring(fileInfo.getFilePath().lastIndexOf('/') + 1);
            sftpTemplate.put(sftpSetting, remotePath, relativePath, fileName, in, true);
        }

        @Override
        public <T> T storageFile(FileInfo fileInfo, Callback<OutputStream, T> callback) {
            // 先保存到临时文件
            Path tmpFilePath;
            try {
                tmpFilePath = Files.createTempFile("fsf_", "_file");
            } catch (IOException e) {
                throw new RuntimeException("创建临时文件失败", e);
            }
            if (null == tmpFilePath) {
                throw new RuntimeException("创建临时文件失败");
            }
            T result;
            try (OutputStream out = Files.newOutputStream(tmpFilePath)) {
                result = callback.call(out);
                out.flush();
            } catch (IOException e) {
                FileUtils.delete(tmpFilePath);
                throw new RuntimeException("保存临时文件失败", e);
            }
            try (InputStream in = Files.newInputStream(tmpFilePath)) {
                storageFile(fileInfo, in);
            } catch (IOException e) {
                throw new RuntimeException("上传文件失败", e);
            } finally {
                FileUtils.delete(tmpFilePath);
            }
            return result;
        }

        @Override
        public void getFile(FileInfo fileInfo, OutputStream out) {
            String relativePath = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf('/'));
            String fileName = fileInfo.getFilePath().substring(fileInfo.getFilePath().lastIndexOf('/') + 1);
            sftpTemplate.get(sftpSetting, remotePath, relativePath, fileName, out);
        }

        @Override
        public <T> T getFile(FileInfo fileInfo, Callback<InputStream, T> callback) {
            // 先保存到临时文件
            Path tmpFilePath;
            try {
                tmpFilePath = Files.createTempFile("fsf_", "_file");
            } catch (IOException e) {
                throw new RuntimeException("创建临时文件失败", e);
            }
            if (null == tmpFilePath) {
                throw new RuntimeException("创建临时文件失败");
            }
            try (OutputStream out = Files.newOutputStream(tmpFilePath)) {
                getFile(fileInfo, out);
                out.flush();
            } catch (IOException e) {
                FileUtils.delete(tmpFilePath);
                throw new RuntimeException("保存临时文件失败", e);
            }
            try (InputStream in = Files.newInputStream(tmpFilePath)) {
                return callback.call(in);
            } catch (IOException e) {
                throw new RuntimeException("上传文件失败", e);
            } finally {
                FileUtils.delete(tmpFilePath);
            }
        }

        @Override
        public void deleteFile(FileInfo fileInfo) {
            String relativePath = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf('/'));
            String fileName = fileInfo.getFilePath().substring(fileInfo.getFilePath().lastIndexOf('/') + 1);
            sftpTemplate.rm(sftpSetting, remotePath, relativePath, fileName);
        }
    }
}
