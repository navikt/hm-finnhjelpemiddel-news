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

    @Get("/{news}")
    fun getNewsByTitle(news: String): HttpResponse<*> = try {
        newsRepository.findByTitle(news)?.let { HttpResponse.ok(it.toOut()) }
            ?: HttpResponse.badRequest("No news with id $news")
    } catch (exception: Exception) {
        LOG.error("Error when getting news $news", exception)
        HttpResponse.serverError(exception.message)
    }

    @Get("/{news}")
    suspend fun getNewsById(news: UUID): HttpResponse<*> = try {
        newsRepository.findById(news)?.let { HttpResponse.ok(it.toOut()) }
            ?: HttpResponse.badRequest("No news with id $news")
    } catch (exception: Exception) {
        LOG.error("Error when getting news $news", exception)
        HttpResponse.serverError(exception.message)
    }

    @Post("/ids")
    fun getNewsList( @Body news: List<UUID>): HttpResponse<*> = try {
        HttpResponse.ok(news.mapNotNull { runBlocking { newsRepository.findById(it)?.toOut() } })
    } catch (exception: Exception) {
        LOG.error("Error when getting news $news", exception)
        HttpResponse.serverError(exception.message)
    }

    private fun NewsDto.toOut(): NewsOut = NewsOut(
        id = id,
        title = title,
        data = data
    )
}