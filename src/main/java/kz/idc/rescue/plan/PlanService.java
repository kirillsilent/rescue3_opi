package kz.idc.rescue.plan;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.MediaType;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Singleton
public class PlanService {

    private static final List<String> ALLOWED = List.of(
            MediaType.IMAGE_PNG,
            MediaType.IMAGE_JPEG,
            "image/webp",
            "image/svg+xml",
            "video/mp4",
            "video/webm",
            "video/ogg"
    );

    private final Path dir;
    private final String baseName;

    public PlanService(
            @Value("${uploads.dir:${user.dir}/.rescue/storage/plan}") String dir,
            @Value("${uploads.basename:plan}") String baseName
    ) throws IOException {
        this.dir = Paths.get(dir);
        this.baseName = baseName;
        Files.createDirectories(this.dir);
    }

    @Introspected
    public static class PlanMeta {
        private String url;
        private long version;
        private String type;

        public PlanMeta() {}

        public PlanMeta(String url, long version, String type) {
            this.url = url;
            this.version = version;
            this.type = type;
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public PlanMeta current() throws IOException {
        try (var s = Files.list(dir)) {
            var p = s.filter(f -> f.getFileName().toString().startsWith(baseName + "."))
                    .findFirst().orElse(null);

            if (p == null) return null;

            String url = "/uploads/" + p.getFileName();
            long ver = Files.getLastModifiedTime(p).toMillis();
            String type = Files.probeContentType(p);

            return new PlanMeta(url, ver, type);
        }
    }

    public PlanMeta save(String contentType, byte[] bytes) throws IOException {

        if (contentType == null || ALLOWED.stream().noneMatch(contentType::equalsIgnoreCase)) {
            throw new IOException("Unsupported or missing content type: " + contentType);
        }

        String ext;
        switch (contentType) {
            case MediaType.IMAGE_PNG:
                ext = "png";
                break;
            case MediaType.IMAGE_JPEG:
                ext = "jpg";
                break;
            case "image/webp":
                ext = "webp";
                break;
            case "image/svg+xml":
                ext = "svg";
                break;
            case "video/mp4":
                ext = "mp4";
                break;
            case "video/webm":
                ext = "webm";
                break;
            case "video/ogg":
                ext = "ogv";
                break;
            default:
                throw new IOException("Unsupported content type: " + contentType);
        }

        try (var s = Files.list(dir)) {
            s.filter(f -> f.getFileName().toString().startsWith(baseName + "."))
                    .forEach(f -> { try { Files.deleteIfExists(f); } catch (IOException ignored) {} });
        }

        Path target = dir.resolve(baseName + "." + ext);
        Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String url = "/uploads/" + target.getFileName();
        long ver = Files.getLastModifiedTime(target).toMillis();

        return new PlanMeta(url, ver, contentType);
    }
}
