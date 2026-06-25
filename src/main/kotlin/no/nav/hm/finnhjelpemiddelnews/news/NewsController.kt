package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.UUID
import org.slf4j.LoggerFactory

@Controller("/news")
@Tag(name = "News")
class NewsController(
    private val newsRepository: NewsRepository,
    private val tagsRepository: TagsRepository,
    private val newsTagsRepository: NewsTagsRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NewsController::class.java)
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

    @Get("/")
    suspend fun getNewsList(): HttpResponse<List<NewsDto>> = try {
        newsRepository.findAll().map { news ->
            news.toDto(fetchTagsForNews(news.id))
        }.toList().let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    @Get("/list")
    suspend fun getNewsBySize(@QueryValue(defaultValue = "5") size: Int): HttpResponse<List<NewsDto>> = try {
        newsRepository.findAll().map { news ->
            news.toDto(fetchTagsForNews(news.id))
        }.toList().take(size).let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }

    private suspend fun fetchTagsForNews(newsId: UUID): List<String> {
        val tagIds = newsTagsRepository.findByIdNewsId(newsId).map { it.id.tagId }
        return if (tagIds.isEmpty()) emptyList() else tagsRepository.findByIdIn(tagIds).map { it.tag }
    }
}