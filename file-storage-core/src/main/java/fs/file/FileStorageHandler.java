package fs.file;

import fs.common.Callback;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件存储处理器
 */
public interface FileStorageHandler {
    /**
     * 存储文件
     *
     * @param fileInfo 文件信息
     * @param in       输入流
     */
    void storageFile(FileInfo fileInfo, InputStream in);

    /**
     * 存储文件
     *
     * @param fileInfo 文件信息
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    <T> T storageFile(FileInfo fileInfo, Callback<OutputStream, T> callback);

    /**
     * 获取文件
     *
     * @param fileInfo 文件信息
     * @param out      输出流
     */
    void getFile(FileInfo fileInfo, OutputStream out);

    /**
     * 获取文件
     *
     * @param fileInfo 文件信息
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    <T> T getFile(FileInfo fileInfo, Callback<InputStream, T> callback);

    /**
     * 删除文件
     *
     * @param fileInfo 文件信息
     */
    void deleteFile(FileInfo fileInfo);
}
