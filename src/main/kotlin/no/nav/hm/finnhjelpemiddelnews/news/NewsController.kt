package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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

    @Get("/")
    suspend fun getNewsList(): HttpResponse<List<NewsDto>> = try {
        newsRepository.findAll().map{it.toDto()}.toList().let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av news", exception)
        HttpResponse.notFound()
    }
}