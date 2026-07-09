package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.model.Page
import io.micronaut.data.model.Sort
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.slf4j.LoggerFactory

@Controller("/news")
@Tag(name = "News")
class NewsController(
    private val newsService: NewsService,
    private val newsRepository: NewsRepository,
    private val tagsRepository: TagsRepository,
    private val newsTagsRepository: NewsTagsRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NewsController::class.java)
    }

    @Get("/{id}")
    suspend fun getNewsById(id: UUID): HttpResponse<PublicNewsDto> = try {
        val news = newsRepository.findOne(id)
        val tags = fetchTagsForNews(id)
        HttpResponse.ok(news.toPublicDto(tags))
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    @Get("/")
    suspend fun getNewsList(@QueryValue(defaultValue = "0") page: Int,
                            @QueryValue(defaultValue = "6") size: Int,
                            @QueryValue tag: List<String>? = null,
                            @QueryValue search: String? = null, ): HttpResponse<Page<PublicNewsDto>> = try {
        val newsPage = newsService.getNews(page, size, tag, search, Sort.of(Sort.Order.desc("publishedFrom")), Status.PUBLISHED, PublishingState.ACTIVE)
        HttpResponse.ok(newsPage.map { it.toPublicDto() })
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news: ${exception.message}", exception)
        HttpResponse.notFound()
    }


    private suspend fun fetchTagsForNews(newsId: UUID): List<String> {
        val tagIds = newsTagsRepository.findByIdNewsId(newsId).map { it.id.tagId }
        return if (tagIds.isEmpty()) emptyList() else tagsRepository.findByIdIn(tagIds).map { it.tag }
    }
}