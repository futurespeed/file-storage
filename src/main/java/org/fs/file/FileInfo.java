package org.fs.file;


import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息
 */
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private String id;
    /**
     * 文件编码
     */
    private String fileCode;
    /**
     * 存储种类：1-永久 2-临时
     */
    private String storageKind;
    /**
     * 存储类型：local-本地 swift-swift
     */
    private String storageType;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 文件后缀
     */
    private String fileExtension;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 创建用户
     */
    private String createName;
    /**
     * 业务编号
     */
    private String busiNo;
    /**
     * 业务类型
     */
    private String busiType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getStorageKind() {
        return storageKind;
    }

    public void setStorageKind(String storageKind) {
        this.storageKind = storageKind;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getBusiNo() {
        return busiNo;
    }

    public void setBusiNo(String busiNo) {
        this.busiNo = busiNo;
    }

    public String getBusiType() {
        return busiType;
    }

    public void setBusiType(String busiType) {
        this.busiType = busiType;
    }
}