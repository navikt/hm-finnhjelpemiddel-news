package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.micronaut.data.model.Page
import io.micronaut.data.model.Sort
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
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import no.nav.hm.finnhjelpemiddelnews.media.MediaDTO
import no.nav.hm.finnhjelpemiddelnews.media.MediaUploadService
import no.nav.hm.finnhjelpemiddelnews.media.ObjectType
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsDto
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import no.nav.hm.finnhjelpemiddelnews.news.NewsService
import no.nav.hm.finnhjelpemiddelnews.news.NewsTags
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsId
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsRepository
import no.nav.hm.finnhjelpemiddelnews.news.PublishingState
import no.nav.hm.finnhjelpemiddelnews.news.Status
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import org.reactivestreams.Publisher
import java.time.LocalDateTime
import java.util.UUID

@Controller("/admin/news")
class NewsAdminController(
    private val newsService: NewsService,
    private val newsRepository: NewsRepository,
    private val newsTagsRepository: NewsTagsRepository,
    private val tagsRepository: TagsRepository,
    private val mediaUploadService: MediaUploadService,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsAdminController::class.java)
    }

    @Get("/")
    suspend fun getAllNews(@QueryValue(defaultValue = "0") page: Int,
                            @QueryValue(defaultValue = "6") size: Int,
                            @QueryValue tag: List<String>? = null,
                            @QueryValue search: String? = null,
                           @QueryValue status: Status? = null,
                           @QueryValue publishingState: PublishingState? = null): HttpResponse<Page<NewsDto>> = try {
        HttpResponse.ok(newsService.getNews(page, size, tag, search, sort = Sort.of(Sort.Order.desc("updated"), Sort.Order.desc("created")), status = status, publishingState = publishingState))
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    @Get("/{id}")
    suspend fun getNewsById(id: UUID): HttpResponse<NewsDto> = try {
        val news = newsRepository.findOne(id)
        val tags = fetchTagsForNews(id)
        HttpResponse.ok(news.toDto(tags))
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    @Get("/list")
    suspend fun getNewsBySize(@QueryValue(defaultValue = "5") size: Int): HttpResponse<List<NewsDto>> = try {
        newsService.getNews(0, size, null, null, status = Status.PUBLISHED, publishingState = PublishingState.ACTIVE).content
            .let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    @Post("/")
    suspend fun createNews(
        @Body createNewsDto: CreateNewsDto): HttpResponse<UUID> {
            if (createNewsDto.title.isBlank()) return HttpResponse.badRequest()
            return try {
                val now = LocalDateTime.now()
                val saved = newsRepository.save(News(
                    title = createNewsDto.title,
                    description = createNewsDto.description,
                    body = createNewsDto.body,
                    created = now,
                    updated = now,
                    publishedFrom = createNewsDto.publishedFrom,
                    publishedTo = createNewsDto.publishedTo,
                    imageUrl = createNewsDto.imageUrl,
                    imageDescription = createNewsDto.imageDescription,
                    status = createNewsDto.status,
                    comment = createNewsDto.comment,
                ))
                val tagLinks = createNewsDto.tags.map { tagId ->
                    NewsTags(NewsTagsId(tagId = UUID.fromString(tagId), newsId = saved.id))
                }
                newsTagsRepository.saveAll(tagLinks).toList()
                HttpResponse.ok(saved.id)
        } catch (exception: Exception) {
            LOG.error("Failed to create new news \"$createNewsDto\"", exception)
            HttpResponse.serverError()
        }
    }

    @Put("/{id}")
    suspend fun updateNews(
        @Body newsDto: CreateNewsDto,
        id: UUID,
    ): HttpResponse<String> {
        val news = newsRepository.findById(id) ?: return HttpResponse.notFound()
        return try {
            val updatedNews = news.copy(
                title = newsDto.title,
                description = newsDto.description,
                body = newsDto.body,
                updated = LocalDateTime.now(),
                publishedFrom = newsDto.publishedFrom,
                publishedTo = newsDto.publishedTo,
                imageUrl = newsDto.imageUrl,
                imageDescription = newsDto.imageDescription,
                status = newsDto.status,
                comment = newsDto.comment,
            )
            newsTagsRepository.deleteByIdNewsId(updatedNews.id)
            val tagLinks = newsDto.tags.map { tagId ->
                NewsTags(NewsTagsId(tagId = UUID.fromString(tagId), newsId = updatedNews.id))
            }
            if (tagLinks.isNotEmpty()) newsTagsRepository.saveAll(tagLinks).toList()
            newsRepository.update(updatedNews)
            HttpResponse.ok("updated $id")
        } catch (exception: Exception) {
            LOG.error("Failed to update news \"$id\"", exception)
            HttpResponse.serverError()
        }
    }

    @Delete("/{id}")
    suspend fun deleteNews(id: UUID): HttpResponse<String> {
        if (!newsRepository.existsById(id)) return HttpResponse.notFound()
        return try {
            newsRepository.deleteById(id)
            HttpResponse.ok("deleted $id")
        } catch (exception: Exception) {
            LOG.error("Failed to delete news \"$id\"", exception)
            HttpResponse.serverError()
        }
    }

    @Post(
        value = "/{newsId}/media",
        consumes = [MULTIPART_FORM_DATA],
        produces = [APPLICATION_JSON]
    )
    suspend fun uploadNewsImage(newsId: UUID, files: Publisher<CompletedFileUpload>): HttpResponse<MediaDTO> {
        val news = newsRepository.findById(newsId) ?: return HttpResponse.notFound()
        val file = files.asFlow().firstOrNull() ?: return HttpResponse.badRequest()
        val media = mediaUploadService.uploadMedia(file, newsId, ObjectType.UNKNOWN)
        newsRepository.update(news.copy(imageUrl = media.uri))
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

    private suspend fun fetchTagsForNews(newsId: UUID): List<String> {
        val tagIds = newsTagsRepository.findByIdNewsId(newsId).map { it.id.tagId }
        return if (tagIds.isEmpty()) emptyList() else tagsRepository.findByIdIn(tagIds).map { it.tag }
    }
}
