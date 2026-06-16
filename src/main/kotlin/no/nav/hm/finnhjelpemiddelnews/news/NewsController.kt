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
     fun getNewsById(id: UUID): HttpResponse<NewsDto> = try {
        newsRepository.findOne(id).let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
         HttpResponse.notFound()
    }



    //todo fiks dis shit
    @Post("/ids")
    fun getNewsList( @Body news: List<UUID>): HttpResponse<*> = try {
        HttpResponse.ok(news.mapNotNull { runBlocking { newsRepository.findById(it)} })
    } catch (exception: Exception) {
        LOG.error("Error when getting news $news", exception)
        HttpResponse.serverError(exception.message)
    }
}