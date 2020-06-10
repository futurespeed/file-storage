package fs.web;

import org.apache.commons.io.IOUtils;
import fs.common.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * swift存储模版
 */
public class SwiftTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(SwiftTemplate.class);

    /**
     * 连接超时
     */
    private int connectTimeout = 3000;

    /**
     * 读超时
     */
    private int readTimeout = 600000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 上传文件
     *
     * @param path     路径
     * @param fileSize 文件大小
     * @param in       输入流
     */
    public void put(String path, long fileSize, InputStream in) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);//连接超时
            conn.setReadTimeout(readTimeout);//读超时
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            if (fileSize > 0) {
                conn.setRequestProperty("Content-Length", String.valueOf(fileSize));
            }
            try {
                conn.connect();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 上传文件失败，连接swift异常", e);
                throw e;
            }
            OutputStream out = null;
            try {
                out = conn.getOutputStream();
                IOUtils.copy(in, out);
                out.flush();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 上传文件失败", e);
                throw e;
            } finally {
                IOUtils.closeQuietly(out);
            }
            if (conn.getResponseCode() / 100 != 2) {
                LOG.error("[SwiftTemplate] 上传文件失败，swift响应码：{}", conn.getResponseCode());
                throw new RuntimeException("上传文件失败");
            }
            try {
                conn.disconnect();
            } catch (Exception e) {
                LOG.warn("[SwiftTemplate] 关闭http连接异常", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件
     *
     * @param path 路径
     * @param out  输出流
     */
    public void get(String path, OutputStream out) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);//连接超时
            conn.setReadTimeout(readTimeout);//读超时
            conn.setRequestMethod("GET");
            try {
                conn.connect();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 输出文件失败，连接swift异常", e);
                throw e;
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOG.error("[SwiftTemplate] 输出文件失败，swift响应码：{}", conn.getResponseCode());
                throw new RuntimeException("请求失败");
            }
            InputStream in = null;
            try {
                in = conn.getInputStream();
                IOUtils.copy(in, out);
                out.flush();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 输出文件失败", e);
                throw e;
            } finally {
                IOUtils.closeQuietly(in);
            }
            try {
                conn.disconnect();
            } catch (Exception e) {
                LOG.warn("[SwiftTemplate] 关闭http连接异常", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     *
     * @param path 路径
     */
    public void delete(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);//连接超时
            conn.setReadTimeout(readTimeout);//读超时
            conn.setRequestMethod("DELETE");
            try {
                conn.connect();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 删除文件失败，连接swift异常", e);
                throw e;
            }
            if (conn.getResponseCode() / 100 != 2 && conn.getResponseCode() != 404) {
                LOG.error("[SwiftTemplate] 删除文件失败，swift响应码：{}", conn.getResponseCode());
                throw new RuntimeException("删除文件失败");
            }
            try {
                conn.disconnect();
            } catch (Exception e) {
                LOG.warn("[SwiftTemplate] 关闭http连接异常", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param path     路径
     * @param fileSize 文件大小
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    public <T> T put(String path, long fileSize, Callback<OutputStream, T> callback) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);//连接超时
            conn.setReadTimeout(readTimeout);//读超时
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            if (fileSize > 0) {
                conn.setRequestProperty("Content-Length", String.valueOf(fileSize));
            }
            try {
                conn.connect();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 上传文件失败，连接swift异常", e);
                throw e;
            }
            OutputStream out = null;
            T result = null;
            try {
                out = conn.getOutputStream();
                result = callback.call(out);
                out.flush();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 上传文件失败", e);
                throw e;
            } finally {
                IOUtils.closeQuietly(out);
            }
            if (conn.getResponseCode() / 100 != 2) {
                LOG.error("[SwiftTemplate] 上传文件失败，swift响应码：{}", conn.getResponseCode());
                throw new RuntimeException("上传文件失败");
            }
            try {
                conn.disconnect();
            } catch (Exception e) {
                LOG.warn("[SwiftTemplate] 关闭http连接异常", e);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件
     *
     * @param path     路径
     * @param callback 回调
     * @return 处理结果（callback返回的结果）
     */
    public <T> T get(String path, Callback<InputStream, T> callback) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);//连接超时
            conn.setReadTimeout(readTimeout);//读超时
            conn.setRequestMethod("GET");
            try {
                conn.connect();
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 输出文件失败，连接swift异常", e);
                throw e;
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOG.error("[SwiftTemplate] 输出文件失败，swift响应码：{}", conn.getResponseCode());
                throw new RuntimeException("请求失败");
            }
            InputStream in = null;
            T result = null;
            try {
                in = conn.getInputStream();
                result = callback.call(in);
            } catch (Exception e) {
                LOG.error("[SwiftTemplate] 输出文件失败", e);
                throw e;
            } finally {
                IOUtils.closeQuietly(in);
            }
            try {
                conn.disconnect();
            } catch (Exception e) {
                LOG.warn("[SwiftTemplate] 关闭http连接异常", e);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}