package cn.kong.cosmos.biz.storage;

import cn.kong.cosmos.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现
 * 存储目录结构：{storage.local.root}/{category}/{yyyyMMdd}/{uuid}.{ext}
 * 访问路径：{storage.local.url-prefix}/{category}/{yyyyMMdd}/{uuid}.{ext}
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    /** 允许的图片类型白名单 */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /** 允许的后缀白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    /** 单张图片最大体积（字节），默认 5MB */
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${storage.local.root:./uploads}")
    private String rootDir;

    @Value("${storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public String uploadImage(MultipartFile file, String category, String ownerId) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("上传文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw BusinessException.badRequest("图片大小不能超过 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.badRequest("仅支持 jpg/png/gif/webp 格式图片");
        }

        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw BusinessException.badRequest("非法的文件后缀");
        }

        String safeCategory = sanitizeSegment(category, "misc");
        String dateDir = LocalDate.now().format(DATE_FMT);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            Path baseDir = Paths.get(rootDir).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(safeCategory).resolve(dateDir).normalize();

            // 防止路径穿越
            if (!targetDir.startsWith(baseDir)) {
                throw BusinessException.badRequest("非法的存储路径");
            }
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile.toFile());
            // 兜底：有些环境 transferTo 行为差异较大，再做一次断言
            if (!Files.exists(targetFile)) {
                try (var in = file.getInputStream()) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            String url = urlPrefix + "/" + safeCategory + "/" + dateDir + "/" + fileName;
            log.info("文件上传成功 category={} owner={} size={} url={}",
                    safeCategory, ownerId, file.getSize(), url);
            return url;
        } catch (IOException e) {
            log.error("文件存储失败 category={} owner={}", safeCategory, ownerId, e);
            throw new BusinessException(500, "文件存储失败");
        }
    }

    private String extractExtension(String originalFilename) {
        String name = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return "";
        }
        return name.substring(idx + 1).toLowerCase();
    }

    /**
     * 仅允许 [a-zA-Z0-9_-] 作为目录段，否则使用兜底值
     */
    private String sanitizeSegment(String seg, String fallback) {
        if (seg == null || seg.isBlank()) {
            return fallback;
        }
        String cleaned = seg.replaceAll("[^a-zA-Z0-9_-]", "");
        return cleaned.isEmpty() ? fallback : cleaned;
    }
}
