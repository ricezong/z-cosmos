package cn.kong.cosmos.biz.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地上传目录 → HTTP 访问路径映射
 * 例：/uploads/avatar/20260430/xxx.jpg → {rootDir}/avatar/20260430/xxx.jpg
 */
@Configuration
public class LocalStorageWebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.local.root:./uploads}")
    private String rootDir;

    @Value("${storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absPath = Paths.get(rootDir).toAbsolutePath().normalize();
        String location = absPath.toUri().toString();
        String pattern = (urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/") + "**";
        registry.addResourceHandler(pattern)
                .addResourceLocations(location);
    }
}
