package fs.web;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;

/**
 * 媒体类型工具
 */
public class MediaTypeUtils {

    /**
     * 默认媒体类型
     */
    public static final String MEDIA_TYPE_DEFAULT = "application/octet-stream";

    private static final FileTypeMap fileTypeMap;

    static {
        fileTypeMap = loadFileTypeMapFromContextSupportModule();
    }

    private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
        Resource mappingLocation = new ClassPathResource("mime.types");
        if (!mappingLocation.exists()) {
            mappingLocation = new ClassPathResource("fs/web/mime.types");
        }
        if (mappingLocation.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = mappingLocation.getInputStream();
                return new MimetypesFileTypeMap(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return FileTypeMap.getDefaultFileTypeMap();
    }

    /**
     * 获取媒体类型
     *
     * @param fileName 文件名
     * @return 媒体类型
     */
    public static String getMediaType(String fileName) {
        String mediaType = fileTypeMap.getContentType(fileName.toLowerCase());
        if (StringUtils.isNotBlank(mediaType)) {
            return mediaType;
        }
        return MEDIA_TYPE_DEFAULT;
    }

    /**
     * 获取媒体类型
     *
     * @param fileExtension 文件后缀
     * @return 媒体类型
     */
    public static String getMediaTypeByExtension(String fileExtension) {
        return getMediaType("a." + fileExtension);
    }
}