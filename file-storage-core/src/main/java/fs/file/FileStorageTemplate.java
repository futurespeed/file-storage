package fs.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import fs.common.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件存储模版
 */
public class FileStorageTemplate {

    private String defaultStorageType = "swift";

    private FileInfoHandler fileInfoHandler;

    private Map<String, FileStorageHandler> fileStorageHandlerMap = new ConcurrentHashMap<String, FileStorageHandler>();

    public String getDefaultStorageType() {
        return defaultStorageType;
    }

    public void setDefaultStorageType(String defaultStorageType) {
        this.defaultStorageType = defaultStorageType;
    }

    public FileInfoHandler getFileInfoHandler() {
        return fileInfoHandler;
    }

    public void setFileInfoHandler(FileInfoHandler fileInfoHandler) {
        this.fileInfoHandler = fileInfoHandler;
    }

    public Map<String, FileStorageHandler> getFileStorageHandlerMap() {
        return fileStorageHandlerMap;
    }

    public void setFileStorageHandlerMap(Map<String, FileStorageHandler> fileStorageHandlerMap) {
        this.fileStorageHandlerMap = fileStorageHandlerMap;
    }

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    public FileInfo getFileInfo(String id) {
        return fileInfoHandler.getFileInfo(id);
    }

    /**
     * 存储文件
     *
     * @param file     文件
     * @param fileInfo 文件信息
     * @return 文件信息
     */
    public FileInfo storageFile(File file, FileInfo fileInfo) {
        FileInputStream in = null;
        try {
            if (null == fileInfo) {
                fileInfo = new FileInfo();
            }
            fileInfo.setFileSize(file.length());
            if (StringUtils.isBlank(fileInfo.getFileName())) {
                fileInfo.setFileName(file.getName());
            }
            in = new FileInputStream(file);
            return storageFile(in, fileInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * 存储文件
     *
     * @param fileInfo 文件信息
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    public <T> T storageFile(FileInfo fileInfo, Callback<OutputStream, T> callback) {
        fileInfo = setStorageFileInfo(fileInfo);
        //保存文件信息
        fileInfoHandler.saveFileInfo(fileInfo);
        FileStorageHandler fileStorageHandler = fileStorageHandlerMap.get(fileInfo.getStorageType());
        if (null == fileStorageHandler) {
            throw new RuntimeException("无法获取文件存储处理器（FileStorageHandler），存储类型：" + fileInfo.getStorageType());
        }
        return fileStorageHandler.storageFile(fileInfo, callback);
    }

    /**
     * 存储文件
     *
     * @param in       输入流
     * @param fileInfo 文件信息
     * @return 文件信息
     */
    public FileInfo storageFile(InputStream in, FileInfo fileInfo) {
        fileInfo = setStorageFileInfo(fileInfo);
        //保存文件信息
        fileInfoHandler.saveFileInfo(fileInfo);
        FileStorageHandler fileStorageHandler = fileStorageHandlerMap.get(fileInfo.getStorageType());
        if (null == fileStorageHandler) {
            throw new RuntimeException("无法获取文件存储处理器（FileStorageHandler），存储类型：" + fileInfo.getStorageType());
        }
        fileStorageHandler.storageFile(fileInfo, in);
        return fileInfo;
    }

    /**
     * 设置文件信息-添加默认属性
     *
     * @param fileInfo 文件信息
     * @return 文件信息
     */
    protected FileInfo setStorageFileInfo(FileInfo fileInfo) {
        Date currTime = new Date();
        String month = (new SimpleDateFormat("yyyyMM")).format(currTime);
        if (null == fileInfo) {
            fileInfo = new FileInfo();
        }
        if (StringUtils.isBlank(fileInfo.getStorageType())) {
            fileInfo.setStorageType(defaultStorageType);
        }
        if (StringUtils.isBlank(fileInfo.getId())) {
            fileInfo.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (StringUtils.isBlank(fileInfo.getFileExtension())) {
            String suffix = "dat";
            if (StringUtils.isNotBlank(fileInfo.getFileName())) {
                int idx = fileInfo.getFileName().lastIndexOf(".");
                if (idx >= 0 && fileInfo.getFileName().length() > idx + 1) {
                    suffix = fileInfo.getFileName().substring(idx + 1);
                }
            }
            fileInfo.setFileExtension(suffix.toLowerCase());
        }
        if (StringUtils.isBlank(fileInfo.getFileName())) {
            fileInfo.setFileName(fileInfo.getId() + "." + fileInfo.getFileExtension());
        }
        if (StringUtils.isBlank(fileInfo.getFileCode())) {
            fileInfo.setFileCode("default");
        }
        if (StringUtils.isBlank(fileInfo.getFilePath())) {
            //文件路径：[文件编码]/[yyyyMM]/[资源ID].[文件后缀]
            fileInfo.setFilePath(fileInfo.getFileCode() + "/" + month + "/" + fileInfo.getId() + "." + fileInfo.getFileExtension());
        }
        if (StringUtils.isBlank(fileInfo.getStorageKind())) {
            fileInfo.setStorageKind("2");//1-永久 2-临时
        }
        fileInfo.setCreateTime(currTime);
        return fileInfo;
    }

    /**
     * 获取文件
     *
     * @param id  文件ID
     * @param out 输出流
     */
    public void getFile(String id, OutputStream out) {
        FileInfo fileInfo = getFileInfo(id);
        if (null == fileInfo) {
            throw new RuntimeException("文件不存在");
        }
        getFile(fileInfo, out);
    }

    /**
     * 获取文件
     *
     * @param fileInfo 文件信息
     * @param out      输出流
     */
    public void getFile(FileInfo fileInfo, OutputStream out) {
        if (null == fileInfo) {
            throw new RuntimeException("fileInfo不能为空");
        }
        if (StringUtils.isBlank(fileInfo.getStorageType())) {
            throw new RuntimeException("fileInfo.storageType不能为空");
        }
        FileStorageHandler fileStorageHandler = fileStorageHandlerMap.get(fileInfo.getStorageType());
        if (null == fileStorageHandler) {
            throw new RuntimeException("无法获取文件存储处理器（FileStorageHandler），存储类型：" + fileInfo.getStorageType());
        }
        fileStorageHandler.getFile(fileInfo, out);
    }

    /**
     * 获取文件
     *
     * @param fileInfo 文件信息
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    public <T> T getFile(FileInfo fileInfo, Callback<InputStream, T> callback) {
        if (null == fileInfo) {
            throw new RuntimeException("fileInfo不能为空");
        }
        if (StringUtils.isBlank(fileInfo.getStorageType())) {
            throw new RuntimeException("fileInfo.storageType不能为空");
        }
        FileStorageHandler fileStorageHandler = fileStorageHandlerMap.get(fileInfo.getStorageType());
        if (null == fileStorageHandler) {
            throw new RuntimeException("无法获取文件存储处理器（FileStorageHandler），存储类型：" + fileInfo.getStorageType());
        }
        return fileStorageHandler.getFile(fileInfo, callback);
    }

    /**
     * 获取文件
     *
     * @param id       文件ID
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    public <T> T getFile(String id, Callback<InputStream, T> callback) {
        FileInfo fileInfo = getFileInfo(id);
        if (null == fileInfo) {
            throw new RuntimeException("文件不存在");
        }
        return getFile(fileInfo, callback);
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     */
    public void deleteFile(String id) {
        FileInfo fileInfo = getFileInfo(id);
        if (null == fileInfo) {
            return;
        }

        FileStorageHandler fileStorageHandler = fileStorageHandlerMap.get(fileInfo.getStorageType());
        if (null == fileStorageHandler) {
            throw new RuntimeException("无法获取文件存储处理器（FileStorageHandler），存储类型：" + fileInfo.getStorageType());
        }
        fileStorageHandler.deleteFile(fileInfo);

        fileInfoHandler.deleteFileInfo(id);
    }
}