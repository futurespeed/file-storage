package fs.ftp;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SFTP 存储模板
 *
 *
 */
@Slf4j
public class SftpTemplate {

    /**
     * 连接超时时间（单位：毫秒）
     */
    private int connectTimeout = 3000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 文件上传
     *
     * @param setting      SFTP配置
     * @param remotePath   远程主目录（只允许创建单层目录）
     * @param relativePath 相对目录（可空，允许创建多层目录）
     * @param fileName     文件名称
     * @param in           输入流
     * @param isOverwrite  是否覆盖
     */
    public void put(SftpSetting setting, String remotePath, String relativePath,
                    String fileName, InputStream in, boolean isOverwrite) {
        log.info("sftp put file [remotePath: {}, relativePath: {}, fileName: {}]", remotePath, relativePath, fileName);
        Session sshSession = null;
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            sshSession = getSession(setting);
            channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            //创建主目录
            mkdir(sftp, remotePath);

            //创建相对目录
            String finalPath = mkdirs(sftp, remotePath, relativePath);

            //上传文件
            putFile(sftp, finalPath, fileName, in, null, null, isOverwrite);

        } catch (Exception e) {
            log.error("sftp can not put", e);
            throw new RuntimeException(e);
        } finally {
            release(sshSession, channel, sftp);
        }
    }

    /**
     * 文件下载
     *
     * @param setting      SFTP配置
     * @param remotePath   远程主目录（只允许创建单层目录）
     * @param relativePath 相对目录（可空，允许创建多层目录）
     * @param fileName     文件名称
     * @param out          输出流
     */
    public void get(SftpSetting setting, String remotePath, String relativePath,
                    String fileName, OutputStream out) {
        log.info("sftp get file [remotePath: {}, relativePath: {}, fileName: {}]", remotePath, relativePath, fileName);
        Session sshSession = null;
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            sshSession = getSession(setting);
            channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            String finalPath = remotePath + getDirDelimiter() + relativePath;
            log.trace("sftp cd {}", finalPath);
            sftp.cd(finalPath);
            sftp.get(fileName, out);
            out.flush();
        } catch (Exception e) {
            log.error("sftp can not get", e);
            throw new RuntimeException(e);
        } finally {
            release(sshSession, channel, sftp);
        }
    }

    /**
     * 文件删除
     *
     * @param setting      SFTP配置
     * @param remotePath   远程主目录（只允许创建单层目录）
     * @param relativePath 相对目录（可空，允许创建多层目录）
     * @param fileName     文件名称
     */
    public void rm(SftpSetting setting, String remotePath, String relativePath, String fileName) {
        log.info("sftp rm file [remotePath: {}, relativePath: {}, fileName: {}]", remotePath, relativePath, fileName);
        Session sshSession = null;
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            sshSession = getSession(setting);
            channel = sshSession.openChannel("sftp");
            channel.connect(connectTimeout);
            sftp = (ChannelSftp) channel;

            String finalPath = remotePath + getDirDelimiter() + relativePath;
            sftp.rm(finalPath + getDirDelimiter() + fileName);
        } catch (Exception e) {
            log.error("sftp can not rm", e);
            throw new RuntimeException(e);
        } finally {
            release(sshSession, channel, sftp);
        }
    }

    /**
     * 创建单层目录
     *
     * @param sftp       sftp对象
     * @param remotePath 远程路径
     * @return 最终目录路径
     * @throws Exception
     */
    protected String mkdir(ChannelSftp sftp, String remotePath) throws Exception {
        try {
            log.trace("sftp cd {}", remotePath);
            sftp.cd(remotePath);
        } catch (SftpException e) {
            try {
                log.info("sftp mkdir {}", remotePath);
                sftp.mkdir(remotePath);
            } catch (SftpException e1) {
                log.error("sftp can not mkdir {}", remotePath, e1);
                throw e1;
            }
        }
        return remotePath;
    }

    /**
     * 创建多层目录
     *
     * @param sftp         sftp对象
     * @param remotePath   远程主路径
     * @param relativePath 远程相对路径
     * @return 最终目录路径
     * @throws Exception
     */
    protected String mkdirs(ChannelSftp sftp, final String remotePath, final String relativePath)
            throws Exception {
        StringBuilder path = new StringBuilder();
        path.append(remotePath).append(getDirDelimiter());
        // relativePath存放的是文件在web服务器下的相对路径，例如:2012/2
        if (relativePath != null && relativePath.length() > 0) {
            StringTokenizer strObj = new StringTokenizer(relativePath, getDirDelimiter());
            while (strObj.hasMoreTokens()) {
                String filePathName = (String) strObj.nextElement();
                if (filePathName != null && filePathName.length() > 0) {
                    path.append(filePathName).append(getDirDelimiter());
                }
                mkdir(sftp, path.toString());
            }
        }
        return path.toString();
    }

    /**
     * 上传文件
     *
     * @param sftp          sftp对象
     * @param finalPath     远程文件目录路径
     * @param fileName      远程文件名称
     * @param in            输入流
     * @param localDir      本地目录
     * @param localFileName 本地文件名
     * @param isOverwrite   是否覆盖
     * @throws Exception
     */
    protected void putFile(ChannelSftp sftp, String finalPath, String fileName,
                           InputStream in, String localDir, String localFileName,
                           boolean isOverwrite) throws Exception {
        log.trace("sftp cd " + finalPath);
        sftp.cd(finalPath);
        boolean isFileExist = false;
        log.trace("sftp ls " + fileName);
        try {
            sftp.ls(fileName);
            isFileExist = true;
            log.trace("sftp file [{}] already exist", fileName);
        } catch (Exception e) {
            log.trace("sftp file [{}] not exist", fileName);
        }
        if (isFileExist && !isOverwrite) {
            throw new RuntimeException("sftp file [" + fileName + "] already exist");
        }

        //tempFileName为临时文件
        String tempFileName = getTempFileName(fileName);

        if (StringUtils.isNotBlank(localDir) && StringUtils.isNotBlank(localFileName)) {
            log.trace("sftp lcd {}", localDir);
            sftp.lcd(localDir);
            log.debug("sftp create temp file [{}]", tempFileName);
            sftp.put(localFileName, tempFileName);
        } else if (in != null) {
            log.debug("sftp create temp file [{}]", tempFileName);
            sftp.put(in, tempFileName);
        } else {
            throw new RuntimeException("sftp localFileName or inputStream can not be null");
        }

        if (isFileExist) {
            log.debug("sftp remove old file [{}]", fileName);
            try {
                sftp.rm(fileName);//删除原文件
            } catch (SftpException e) {
                log.error("sftp can not remove file [" + fileName + "]", e);
                throw e;
            }
        }
        log.debug("sftp rename temp file to [{}]", fileName);
        sftp.rename(tempFileName, fileName);//将新文件更名为正确文件名
        log.info("sftp put file [{}] success", fileName);
    }

    /**
     * 获取文件夹分隔符
     *
     * @return 获取文件夹分隔符
     */
    protected String getDirDelimiter() {
        return "/";
    }

    /**
     * 返回临时文件名称
     * <p>规则：yyyyMMddHHmmssSSS_【6位随机数】_【文件名后8位，默认为file】_【后缀后5位，默认为data】.tmp</p>
     *
     * @param fileName 原文件名称
     * @return 文件名称
     */
    protected String getTempFileName(String fileName) {
        String name = "file";
        int namePos = fileName.indexOf('.');
        if (namePos != -1) {
            name = fileName.substring(0, namePos);
            int nameLen = name.length();
            if (nameLen > 8) {
                name = name.substring(nameLen - 8);
            }
        }
        String suffix = "data";
        int suffixPos = fileName.lastIndexOf('.');
        if (suffixPos != -1) {
            suffix = fileName.substring(suffixPos + 1);
            int suffixLen = suffix.length();
            if (suffixLen > 5) {
                suffix = suffix.substring(suffixLen - 5);
            }
        }
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "_" +
                ThreadLocalRandom.current().nextInt(1000000) + "_" + name + "_" + suffix + ".tmp";
    }


    /**
     * 获取SFTP session
     *
     * @param setting SFTP配置
     * @return SFTP session
     * @throws Exception
     */
    protected Session getSession(SftpSetting setting) throws Exception {
        try {
            JSch jsch = new JSch();
            if (StringUtils.isNotBlank(setting.getPrivateKey())) {
                jsch.addIdentity(setting.getPrivateKey());
            }
            Session sshSession = jsch.getSession(setting.getUsername(), setting.getHost(), setting.getPort());
            sshSession.setPassword(setting.getPassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            log.info("sftp connected to {}:{}", setting.getHost(), setting.getPort());
            sshSession.connect();
            return sshSession;
        } catch (Exception e) {
            log.error("sftp can not connected to " + setting.getHost() + ":" + setting.getPort(), e);
            throw e;
        }
    }

    /**
     * 释放资源
     *
     * @param sshSession session对象
     * @param channel    channel对象
     * @param sftp       sftp对象
     */
    protected void release(Session sshSession, Channel channel, ChannelSftp sftp) {
        log.debug("sftp release resource");
        if (sftp != null && sftp.isConnected()) {
            try {
                sftp.disconnect();
            } catch (Exception e) {
                log.warn("sftp close channelSftp error", e);
            }
        }
        if (channel != null && channel.isConnected()) {
            try {
                channel.disconnect();
            } catch (Exception e) {
                log.warn("sftp close channel error", e);
            }
        }
        if (sshSession != null && sshSession.isConnected()) {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                log.warn("sftp close session error", e);
            }
        }
    }
}
