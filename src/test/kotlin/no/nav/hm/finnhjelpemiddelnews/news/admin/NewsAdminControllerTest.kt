package no.nav.hm.finnhjelpemiddelnews.news.admin

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsController
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@MicronautTest
class NewsAdminControllerTest (
    private val newsController: NewsController,
    private val newsRepository: NewsRepository,
    private val newsAdminController: NewsAdminController)
{
    val newsDto = News(title = "Nyhet 1", body = "Dette er en nyhet")

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

            newsRepository.deleteById(newsDto.id)
            val res = newsController.getNewsById(newsDto.id)
            res.status shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Test
    fun postTest() {
        runBlocking {
            val dto = CreateNewsDto(title = "Nyhet 2", body = "Dette er ny nyhet")
            newsAdminController.createNews(dto)

            val created = newsRepository.findByTitle("Nyhet 2")
            created.body shouldBe dto.body
        }
    }

    @Test
    fun `Bad path`() {
        runBlocking {
            newsController.getNewsById(UUID.randomUUID()).status shouldBe HttpStatus.NOT_FOUND
        }
    }
}