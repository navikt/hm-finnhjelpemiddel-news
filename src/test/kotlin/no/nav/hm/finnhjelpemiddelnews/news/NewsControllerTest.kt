package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

@MicronautTest
class NewsControllerTest (
    private val newsController: NewsController,
    private val newsRepository: NewsRepository,
) {
    val newsDto = News(title = "Nyhet 1", description = "", body = "Dette er en nyhet", created = LocalDateTime.now(),
        publishedFrom = LocalDateTime.now().minusDays(1), publishedTo = LocalDateTime.now().plusYears(1), image_url = null, imageDescription = "")
    val newsDto2 = News(title = "Nyhet 2", description = "", body = "Dette er en nyhet", created = LocalDateTime.now(),
        publishedFrom = LocalDateTime.now().minusDays(1), publishedTo = LocalDateTime.now().plusYears(1), image_url = null, imageDescription = "")
    val newsDto3 = News(title = "Nyhet 3", description = "", body = "Dette er en nyhet", created = LocalDateTime.now(),
        publishedFrom = LocalDateTime.now().minusDays(1), publishedTo = LocalDateTime.now().plusYears(1), image_url = null, imageDescription = "")


    @Test
    fun getNewsByIdTest() {
        runBlocking {
            newsRepository.save(newsDto)
            val responseNewsDto = newsController.getNewsById(newsDto.id)
            responseNewsDto.status shouldBe HttpStatus.OK
            responseNewsDto.body().body shouldBe newsDto.body
        }
    }

    @Test
    fun getAllNews()  {
        runBlocking {
            newsRepository.save(newsDto2)
            newsRepository.save(newsDto3)
            val res = newsController.getNewsList(page = 0, size = 10)
            res.status shouldBe HttpStatus.OK
            res.body().content.size shouldBe 2

        }
    }

    @Test
    fun badGetNewsById() {
        runBlocking {
            newsController.getNewsById(UUID.randomUUID()).status shouldBe HttpStatus.NOT_FOUND
        }
    } }