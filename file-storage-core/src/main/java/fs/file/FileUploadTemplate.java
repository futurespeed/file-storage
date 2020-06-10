package fs.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件上传模版
 */
public class FileUploadTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadTemplate.class);

    private Map<String, FileUploadConfig> uploadConfigs;

    private FileStorageTemplate fileStorageTemplate;

    public Map<String, FileUploadConfig> getUploadConfigs() {
        return uploadConfigs;
    }

    public void setUploadConfigs(Map<String, FileUploadConfig> uploadConfigs) {
        this.uploadConfigs = uploadConfigs;
    }

    public FileStorageTemplate getFileStorageTemplate() {
        return fileStorageTemplate;
    }

    public void setFileStorageTemplate(FileStorageTemplate fileStorageTemplate) {
        this.fileStorageTemplate = fileStorageTemplate;
    }

    /**
     * 获取文件上传配置
     *
     * @param fileCode 文件编码
     * @return 文件上传配置
     */
    public FileUploadConfig getUploadConfig(String fileCode) {
        return uploadConfigs.get(fileCode);
    }

    /**
     * 文件上传
     *
     * @param userCode 用户编码
     * @param fileCode 文件编码
     * @param mFile    文件
     * @return 文件上传结果
     */
    public FileUploadResult uploadFile(String userCode, String fileCode, MultipartFile mFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileCode(fileCode);
        fileInfo.setCreateName(userCode);
        return uploadFile(fileInfo, mFile);
    }

    /**
     * 文件上传
     *
     * @param fileInfo 文件信息
     * @param mFile    文件
     * @return 文件上传结果
     */
    public FileUploadResult uploadFile(FileInfo fileInfo, MultipartFile mFile) {
        try {
            //文件检查
            FileUploadResult checkResult = checkFile(fileInfo.getFileCode(), mFile);
            if (checkResult != null) {
                return checkResult;
            }

            //设置文件信息
            FileUploadConfig uploadConfig = uploadConfigs.get(fileInfo.getFileCode());
            if (StringUtils.isBlank(fileInfo.getStorageType())) {
                fileInfo.setStorageType(uploadConfig.getStorageType());
            }
            if (StringUtils.isBlank(fileInfo.getStorageKind())) {
                fileInfo.setStorageKind(uploadConfig.getStorageKind());
            }
            if (StringUtils.isBlank(fileInfo.getFileName())) {
                fileInfo.setFileName(mFile.getOriginalFilename());
            }
            if (0 == fileInfo.getFileSize()) {
                fileInfo.setFileSize(mFile.getSize());
            }

            //保存文件
            InputStream in = mFile.getInputStream();
            try {
                fileInfo = fileStorageTemplate.storageFile(in, fileInfo);
            } finally {
                IOUtils.closeQuietly(in);
            }
            return new FileUploadResult(FileUploadResult.RESULT_SUCCESS, FileUploadResult.RESULT_CODE_SUCCESS, "上传成功", fileInfo);
        } catch (Exception e) {
            LOG.error("上传异常", e);
            return new FileUploadResult(FileUploadResult.RESULT_FAIL, "FILE_UPLOAD_999", "上传失败");
        }
    }

    /**
     * 删除上传的文件
     *
     * @param resourceId 资源ID
     */
    public void deleteUploadFile(String resourceId) {
        fileStorageTemplate.deleteFile(resourceId);
    }

    /**
     * 文件检查
     *
     * @param fileCode 文件编码
     * @param mFile    文件
     * @return 上传结果
     */
    protected FileUploadResult checkFile(String fileCode, MultipartFile mFile) {
        FileUploadConfig config = uploadConfigs.get(fileCode);
        if (null == config) {
            return new FileUploadResult(FileUploadResult.RESULT_FAIL, "FILE_UPLOAD_001", "读取文件上传配置异常");
        }
        if (config.getMaxSize() > 0 && mFile.getSize() > config.getMaxSize()) {
            return new FileUploadResult(FileUploadResult.RESULT_FAIL, "FILE_UPLOAD_002", "上传文件过大");
        }
        if (StringUtils.isNotBlank(config.getSuffix())) {
            String fileName = mFile.getOriginalFilename();
            String suffix = "";
            int idx = fileName.lastIndexOf(".");
            if (idx >= 0) {
                suffix = fileName.substring(idx + 1);
            }
            boolean isMatch = false;
            for (String configSuffix : config.getSuffix().split(",")) {
                if (StringUtils.equalsIgnoreCase(suffix, configSuffix)) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return new FileUploadResult(FileUploadResult.RESULT_FAIL, "FILE_UPLOAD_003", "只支持" + config.getSuffix() + "格式");
            }
        }
        return null;
    }
}