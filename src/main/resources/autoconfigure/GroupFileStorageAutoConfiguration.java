package org.fs.autoconfigure;

import org.fs.common.Callback;
import org.fs.file.FileInfo;
import org.fs.file.FileStorageHandler;
import org.fs.file.FileStorageTemplate;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 组存储自动配置
 *
 */
@Configuration
@AutoConfigureAfter({FileStorageAutoConfiguration.class})
public class GroupFileStorageAutoConfiguration {

    @Bean
    @ConditionalOnBean({FileStorageTemplate.class})
    @ConditionalOnMissingBean(name = "groupFileStorageHandlerMap")
    public Map<String, FileStorageHandler> groupFileStorageHandlerMap(FileStorageProperties fileStorageProperties,
                                                                      FileStorageTemplate fileStorageTemplate) {
        Map<String, FileStorageHandler> map = new LinkedHashMap<>();
        for (Map.Entry<String, FileStorageProperties.FileStorageType> entry :
                fileStorageProperties.getTypes().entrySet()) {
            if ("group".equals(entry.getValue().getType())) {
                GroupFileStorageHandler handler = new GroupFileStorageHandler();
                String[] handlerTypes = entry.getValue().getPath().split(",");
                for (String handleType : handlerTypes) {
                    FileStorageHandler subHandler = fileStorageTemplate.getFileStorageHandlerMap().get(handleType);
                    if (null == subHandler) {
                        throw new IllegalArgumentException("文件存储处理器[" + handleType + "]不存在");
                    }
                    handler.addHandler(subHandler);
                }
                map.put(entry.getKey(), handler);
            }
        }
        fileStorageTemplate.getFileStorageHandlerMap().putAll(map);
        return map;
    }

    /**
     * 组文件存储控制器
     *
     */
    public static class GroupFileStorageHandler implements FileStorageHandler {

        private List<FileStorageHandler> handlerList = new ArrayList<>();

        public void addHandler(FileStorageHandler handler) {
            handlerList.add(handler);
        }

        @Override
        public void storageFile(FileInfo fileInfo, InputStream in) {
            // 先保存到临时文件，再分发
            File tmpFile;
            try {
                tmpFile = File.createTempFile("fsg_", "_file");
            } catch (IOException e) {
                throw new RuntimeException("创建临时文件失败", e);
            }
            if (null == tmpFile) {
                throw new RuntimeException("创建临时文件失败");
            }
            try (OutputStream out = new FileOutputStream(tmpFile)) {
                IOUtils.copy(in, out);
                out.flush();
            } catch (IOException e) {
                tmpFile.delete();
                throw new RuntimeException("保存临时文件失败", e);
            }
            try {
                for (FileStorageHandler handler : handlerList) {
                    try (InputStream input = new FileInputStream(tmpFile)) {
                        handler.storageFile(fileInfo, input);
                    } catch (Exception e) {
                        throw new RuntimeException("读取临时文件失败", e);
                    }
                }
            } finally {
                tmpFile.delete();
            }

        }

        @Override
        public <T> T storageFile(FileInfo fileInfo, Callback<OutputStream, T> callback) {
            // 先保存到临时文件
            File tmpFile;
            try {
                tmpFile = File.createTempFile("fsg_", "_file");
            } catch (IOException e) {
                throw new RuntimeException("创建临时文件失败", e);
            }
            if (null == tmpFile) {
                throw new RuntimeException("创建临时文件失败");
            }
            T result;
            try (OutputStream out = new FileOutputStream(tmpFile)) {
                result = callback.call(out);
                out.flush();
            } catch (IOException e) {
                tmpFile.delete();
                throw new RuntimeException("保存临时文件失败", e);
            }
            try (InputStream in = new FileInputStream(tmpFile)) {
                storageFile(fileInfo, in);
            } catch (IOException e) {
                throw new RuntimeException("上传文件失败", e);
            } finally {
                tmpFile.delete();
            }
            return result;
        }

        @Override
        public void getFile(FileInfo fileInfo, OutputStream out) {
            handlerList.get(0).getFile(fileInfo, out);
        }

        @Override
        public <T> T getFile(FileInfo fileInfo, Callback<InputStream, T> callback) {
            // 先保存到临时文件
            File tmpFile;
            try {
                tmpFile = File.createTempFile("fsg_", "_file");
            } catch (IOException e) {
                throw new RuntimeException("创建临时文件失败", e);
            }
            if (null == tmpFile) {
                throw new RuntimeException("创建临时文件失败");
            }
            try (OutputStream out = new FileOutputStream(tmpFile)) {
                getFile(fileInfo, out);
                out.flush();
            } catch (IOException e) {
                tmpFile.delete();
                throw new RuntimeException("保存临时文件失败", e);
            }
            try (InputStream in = new FileInputStream(tmpFile)) {
                return callback.call(in);
            } catch (IOException e) {
                throw new RuntimeException("上传文件失败", e);
            } finally {
                tmpFile.delete();
            }
        }

        @Override
        public void deleteFile(FileInfo fileInfo) {
            for (FileStorageHandler handler : handlerList) {
                handler.deleteFile(fileInfo);
            }
        }
    }
}
