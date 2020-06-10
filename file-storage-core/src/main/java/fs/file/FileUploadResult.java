package fs.file;


/**
 * 文件上传结果
 */
public class FileUploadResult {
    /**
     * 结果-成功
     */
    public static final String RESULT_SUCCESS = "success";
    /**
     * 结果-失败
     */
    public static final String RESULT_FAIL = "fail";
    /**
     * 结果代码-成功
     */
    public static final String RESULT_CODE_SUCCESS = "0000";

    /**
     * 结果
     */
    private String result;
    /**
     * 结果代码
     */
    private String resultCode;
    /**
     * 结果信息
     */
    private String resultMsg;
    /**
     * 文件信息
     */
    private FileInfo fileInfo;

    public FileUploadResult() {
        super();
    }

    public FileUploadResult(String result, String resultCode, String resultMsg) {
        super();
        this.result = result;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public FileUploadResult(String result, String resultCode, String resultMsg, FileInfo fileInfo) {
        super();
        this.result = result;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.fileInfo = fileInfo;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}