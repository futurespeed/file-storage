package org.fs.file;

/**
 * 文件上传配置
 */
public class FileUploadConfig {
    /**
     * 存储种类-永久
     */
    public static final String STORAGE_KIND_PERMANENT = "1";
    /**
     * 存储种类-临时
     */
    public static final String STORAGE_KIND_TEMP = "2";
    /**
     * 文件编码
     */
    private String fileCode;
    /**
     * 文件后缀
     */
    private String suffix;
    /**
     * 存储种类
     */
    private String storageKind = STORAGE_KIND_TEMP;
    /**
     * 存储类型
     */
    private String storageType;
    /**
     * 文件大小
     */
    private long maxSize;

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
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

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String sizeStr) {
        long scale = 1;
        String numStr = "0";

        if (sizeStr.matches("^[\\d]+k$")) {
            scale = 1024;
            numStr = sizeStr.split("k")[0];
        } else if (sizeStr.matches("^[\\d]+m$")) {
            scale = 1024 * 1024;
            numStr = sizeStr.split("m")[0];
        } else if (sizeStr.matches("^[\\d]+g$")) {
            scale = 1024 * 1024 * 1024;
            numStr = sizeStr.split("g")[0];
        }
        try {
            this.maxSize = Long.parseLong(numStr) * scale;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}