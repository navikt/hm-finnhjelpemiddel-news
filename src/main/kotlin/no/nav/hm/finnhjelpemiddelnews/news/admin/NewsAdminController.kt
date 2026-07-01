package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.MediaType.MULTIPART_FORM_DATA
import io.micronaut.http.annotation.Body
import org.slf4j.LoggerFactory
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.media.MediaDTO
import no.nav.hm.finnhjelpemiddelnews.media.MediaUploadService
import no.nav.hm.finnhjelpemiddelnews.media.ObjectType
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import no.nav.hm.finnhjelpemiddelnews.news.NewsTags
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsId
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsRepository
import java.time.LocalDateTime
import java.util.UUID

@Controller("/admin/news")
class NewsAdminController(
    private val newsRepository: NewsRepository,
    private val newsTagsRepository: NewsTagsRepository,
    private val mediaUploadService: MediaUploadService,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsAdminController::class.java)
    }

    @Post("/")
     fun createNews(
        @Body createNewsDto: CreateNewsDto): HttpResponse<UUID> {
        return try {
            if (createNewsDto.title.isBlank()) return HttpResponse.badRequest()
            val news = runBlocking {
                val saved = newsRepository.save(News(
                    title = createNewsDto.title,
                    description = createNewsDto.description,
                    body = createNewsDto.body,
                    created = LocalDateTime.now(),
                    publishedFrom = createNewsDto.publishedFrom,
                    publishedTo = createNewsDto.publishedTo,
                    image_url = createNewsDto.image_url,
                    imageDescription = createNewsDto.imageDescription,
                ))
                val tagLinks = createNewsDto.tags.map { tagId ->
                    NewsTags(NewsTagsId(tagId = UUID.fromString(tagId), newsId = saved.id))
                }
                newsTagsRepository.saveAll(tagLinks).toList()
                saved

            }
            HttpResponse.ok(news.id)
        } catch (exception: Exception) {
            LOG.error("Failed to create new news \"$createNewsDto\"", exception)
            HttpResponse.serverError()
        }
    }

    @Put("/{id}")
     fun updateNews(
        @Body newsDto: CreateNewsDto,
        id: UUID
        ): HttpResponse<String> {
        try {
            runBlocking {
                val news = newsRepository.findById(id)
                if(news != null) {
                  val updatedNews = news.copy(
                      title = newsDto.title,
                      description = newsDto.description,
                      body = newsDto.body,
                      updated = LocalDateTime.now(),
                      publishedFrom = newsDto.publishedFrom,
                      publishedTo = newsDto.publishedTo,
                      image_url = newsDto.image_url,
                      imageDescription = newsDto.imageDescription,
                  )
                    newsTagsRepository.deleteByIdNewsId(updatedNews.id)
                    val tagLinks = newsDto.tags.map { tagId ->
                        NewsTags(NewsTagsId(tagId = UUID.fromString(tagId), newsId = updatedNews.id))
                    }
                    if (tagLinks.isNotEmpty()) newsTagsRepository.saveAll(tagLinks).toList()
                  newsRepository.update(updatedNews)
                } else throw Exception("Failed to find news by id $id")
            }
            return HttpResponse.ok("updated $id")
        } catch (exception: Exception) {
            LOG.error("Failed to update news \"$id\"", exception)
            return HttpResponse.serverError()
        }

    }

    @Delete("/{id}")
    fun deleteNews(
        id: UUID
    ): HttpResponse<String> {
        try {
            runBlocking {
                if (!newsRepository.existsById(id)) return@runBlocking HttpResponse.badRequest<String>()
                newsRepository.deleteById(id)
            }
            return HttpResponse.ok("deleted $id")
        } catch (exception: Exception) {
            LOG.error("Failed to delete news \"$id\"", exception)
            return HttpResponse.serverError()
        }
    }
    @Post(
        value = "/{newsId}/media",
        consumes = [MULTIPART_FORM_DATA],
        produces = [APPLICATION_JSON]
    )
    suspend fun uploadNewsImage(newsId: UUID, @Body file: CompletedFileUpload): HttpResponse<MediaDTO> {
        val news = newsRepository.findById(newsId) ?: return HttpResponse.notFound()
        val media = mediaUploadService.uploadMedia(file, newsId, ObjectType.UNKNOWN)
        newsRepository.update(news.copy(image_url = media.uri))
        return HttpResponse.created(media)
    }

    @Get("/media/{newsId}")
    suspend fun getMediaList(newsId: UUID): HttpResponse<List<MediaDTO>> =
        HttpResponse.ok(mediaUploadService.getMediaList(newsId))

    @Delete("/media/{newsId}/{uri}")
    suspend fun deleteMedia(newsId: UUID, uri: String): HttpResponse<MediaDTO> {
        LOG.info("Deleting media for news $newsId, uri: $uri")
        return HttpResponse.ok(mediaUploadService.deleteByOidAndUri(newsId, uri))
    }
}
