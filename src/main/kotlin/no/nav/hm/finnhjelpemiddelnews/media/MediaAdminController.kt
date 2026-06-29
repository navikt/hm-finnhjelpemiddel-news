package no.nav.hm.finnhjelpemiddelnews.media

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.util.*

@Controller("/admin/media")
@Tag(name = "Admin Media")
class MediaAdminController(private val mediaUploadService: MediaUploadService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaAdminController::class.java)
    }

    @Post(
        value = "/news/{newsId}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadNewsMedia(
        newsId: UUID,
        files: Publisher<CompletedFileUpload>
    ): HttpResponse<List<MediaDTO>> {
        LOG.info("Uploading media for news $newsId")
        return HttpResponse.created(
            files.asFlow().map { mediaUploadService.uploadMedia(it, newsId, ObjectType.UNKNOWN) }.toList()
        )
    }

    @Get("/news/{newsId}")
    suspend fun getMediaList(newsId: UUID): HttpResponse<List<MediaDTO>> =
        HttpResponse.ok(mediaUploadService.getMediaList(newsId))

    @Delete("/news/{newsId}/{uri}")
    suspend fun deleteMedia(newsId: UUID, uri: String): HttpResponse<MediaDTO> {
        LOG.info("Deleting media for news $newsId, uri: $uri")
        return HttpResponse.ok(mediaUploadService.deleteByOidAndUri(newsId, uri))
    }
}
