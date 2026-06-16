package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import java.util.UUID
import org.slf4j.LoggerFactory

@Controller("/news")
@Tag(name = "News")
class NewsController(
    private val newsRepository: NewsRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NewsController::class.java)
    }

    @Get("/{id}")
    suspend fun getNewsById(id: UUID): HttpResponse<NewsDto> =
        newsRepository.findOne(id).let { HttpResponse.ok(it) }
            ?: HttpResponse.notFound()

    @Post("/ids")
    fun getNewsList( @Body news: List<UUID>): HttpResponse<*> = try {
        HttpResponse.ok(news.mapNotNull { runBlocking { newsRepository.findById(it)} })
    } catch (exception: Exception) {
        LOG.error("Error when getting news $news", exception)
        HttpResponse.serverError(exception.message)
    }
}