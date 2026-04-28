package kz.idc.rescue.plan;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.HttpResponse;
import javax.inject.Inject;

@Controller("/api/plan")
public class PlanController {

    @Inject
    PlanService planService;

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> get() throws Exception {
        var meta = planService.current();
        if (meta == null) return HttpResponse.noContent();
        return HttpResponse.ok(meta);
    }

    @Post(consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> upload(@Part("file") CompletedFileUpload file) throws Exception {
        if (file == null || file.getFilename() == null || file.getSize() == 0) {
            return HttpResponse.badRequest("{\"error\":\"empty file\"}");
        }
    
        // MediaType -> String ("image/jpeg", "video/mp4", ...)
        String ctype = file.getContentType()
                .map(MediaType::toString)
                .orElse(null);
    
        byte[] bytes = file.getBytes();
    
        var meta = planService.save(ctype, bytes);
        return HttpResponse.ok(meta);
    }
    
}
