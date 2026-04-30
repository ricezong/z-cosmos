package cn.kong.cosmos.biz.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务抽象
 * 默认实现为本地磁盘存储（LocalFileStorageService）
 * 后续可替换为 OSS/COS/七牛等云存储实现
 */
public interface FileStorageService {

    /**
     * 上传图片文件
     * @param file       前端上传的文件
     * @param category   分类目录（如 avatar / post / comment）
     * @param ownerId    归属用户业务 ID，用于路径隔离和日志
     * @return 可通过 HTTP 直接访问的 URL（相对或绝对）
     */
    String uploadImage(MultipartFile file, String category, String ownerId);
}
