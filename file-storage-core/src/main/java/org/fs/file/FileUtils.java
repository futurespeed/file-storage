package org.fs.file;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件工具
 *
 *
 */
@Slf4j
public class FileUtils {

    /**
     * 删除文件
     * @param path 文件路径
     */
    public static void delete(Path path){
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error("删除文件失败，路径：" + path, e);
        }
    }

    private FileUtils(){}
}
