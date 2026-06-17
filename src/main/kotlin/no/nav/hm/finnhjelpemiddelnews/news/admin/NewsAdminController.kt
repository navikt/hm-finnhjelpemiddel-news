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
import no.nav.hm.finnhjelpemiddelnews.news.News
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
     fun createNews(
        @Body createNewsDto: CreateNewsDto): HttpResponse<UUID> {
        return try {
            if (createNewsDto.title.isBlank()) return HttpResponse.badRequest()
            val news = runBlocking {
                newsRepository.save(News(title = createNewsDto.title, body = createNewsDto.body))
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

                news?.body = newsDto.body
                news?.title = newsDto.title

                newsRepository.update(news as News)
            }
            return HttpResponse.ok("updated $newsDto.id.toString()")
        } catch (exception: Exception) {
            LOG.error("Failed to update news \"$newsDto.id\"", exception)
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
}
