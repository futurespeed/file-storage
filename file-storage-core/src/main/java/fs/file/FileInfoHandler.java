package fs.file;


/**
 * 文件信息处理器
 */
public interface FileInfoHandler {
    /**
     * 获取文件信息
     *
     * @param id 文件标识
     * @return 文件信息
     */
    FileInfo getFileInfo(String id);

    /**
     * 保存文件信息
     *
     * @param fileInfo 文件信息
     */
    void saveFileInfo(FileInfo fileInfo);

    /**
     * 删除文件信息
     *
     * @param id 文件标识
     */
    void deleteFileInfo(String id);
}
