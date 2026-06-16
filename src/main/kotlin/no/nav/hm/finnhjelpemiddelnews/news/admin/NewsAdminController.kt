package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import org.slf4j.LoggerFactory
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.NewsDto
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import java.util.UUID

@Controller("/admin/news")
class NewsAdminController(
    private val newsRepository: NewsRepository
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsAdminController::class.java)
    }

    @Post("/")
    suspend fun createNews(
        @Body createNewsDto: CreateNewsDto) {
        try {
            val news = runBlocking {
                newsRepository.save(NewsDto(title = createNewsDto.title, data = createNewsDto.data))
            }
            HttpResponse.ok(news.id.toString())
        } catch (exception: Exception) {
            LOG.error("Failed to create new category \"$createNewsDto\"", exception)
            HttpResponse.serverError()
        }
    }

    @Put("/")
    suspend fun updateNews(
        @Body newsDto: NewsDto): HttpResponse<String> {
        try {
            runBlocking {
                newsRepository.update(newsDto)
            }
            return HttpResponse.ok(newsDto.id.toString())
        } catch (exception: Exception) {
            LOG.error("Failed to update news \"$newsDto\"", exception)
            return HttpResponse.serverError<String>()
        }

    }

    @Delete("/id/{id}")
    suspend fun deleteNews(
        id: String
    ): HttpResponse<String> {
        try {
            runBlocking {
                newsRepository.deleteById(UUID.fromString(id))
            }
            return HttpResponse.ok(id)
        } catch (exception: Exception) {
            LOG.error("Failed to delete news \"$id\"", exception)
            return HttpResponse.serverError<String>()
        }
    }
}
