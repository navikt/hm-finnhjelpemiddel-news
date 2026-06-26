package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsController
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class NewsAdminControllerTest (
    private val newsController: NewsController,
    private val newsRepository: NewsRepository,
    private val newsAdminController: NewsAdminController)
{
    val newsDto = News(title = "Nyhet 1", description = "Deez nuts", body = "Dette er en nyhet", created = LocalDateTime.now(),
        publishedFrom = LocalDateTime.now(), publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "")

    @BeforeEach
    fun init() = runBlocking {
        newsRepository.save(newsDto)
        Unit
    }

    @Test
    fun deleteTest() {
        runBlocking {
            val responseNewsDto = newsController.getNewsById(newsDto.id)
            responseNewsDto.status shouldBe HttpStatus.OK
            responseNewsDto.body().body shouldBe newsDto.body

            newsAdminController.deleteNews(newsDto.id)
            val res = newsController.getNewsById(newsDto.id)
            res.status shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Test
    fun postTest() {
        runBlocking {
            val dto = CreateNewsDto(title = "Nyhet 2", description = "ohio", body = "Dette er ny nyhet", publishedFrom = LocalDateTime.now(),
                publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "", tags = emptyList())
            newsAdminController.createNews(dto)

            val created = newsRepository.findByTitle("Nyhet 2")
            created.body shouldBe dto.body
        }
    }

    @Test
    fun putTest() {
        runBlocking {
            val updatedNews = CreateNewsDto(title = "Nyhet oppdatering", description = "oniichan", body = "Dette er en oppdatering",
                publishedFrom = LocalDateTime.now(), publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "", tags = emptyList())
            val response = newsAdminController.updateNews(updatedNews, newsDto.id)
            response.status shouldBe HttpStatus.OK

            val fetched = newsController.getNewsById(newsDto.id)
            fetched.status shouldBe HttpStatus.OK
            fetched.body().body shouldBe "Dette er en oppdatering"
            fetched.body().description shouldBe "oniichan"
            fetched.body().title shouldBe "Nyhet oppdatering"
        }
    }

    @Test
    fun badDeleteTest() {
        runBlocking {
            val dto = CreateNewsDto(title = "Nyhet 3",description = "daddy", body = "Dette er ny nyhet", publishedFrom = LocalDateTime.now(),
                publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "", tags = emptyList())
            val createdNewsId = newsAdminController.createNews(dto).body()

            newsAdminController.deleteNews(createdNewsId)


            val res = newsController.getNewsById(createdNewsId)
            res.status shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Test
    fun badPostTest() {
        runBlocking {
            val dto = CreateNewsDto(title = "", description = "", body = "Dette er ny nyhet", publishedFrom = LocalDateTime.now(),
                publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "", tags = emptyList())
            val createdNewsId = newsAdminController.createNews(dto)

            createdNewsId.status shouldBe HttpStatus.BAD_REQUEST
        }
    }

    @Test
    fun badPutTest() {
        runBlocking {

        }
    }
}