package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import java.util.UUID

@MicronautTest
class NewsControllerTest (
    private val newsController: NewsController,
    private val newsRepository: NewsRepository,
) {
    @Test
    fun `Happy path`() {
        val newsDto = News(title = "Nyhet 1", body = "Dette er en nyhet")

        runBlocking {
            newsRepository.save(newsDto)
            val responseNewsDto = newsController.getNewsById(newsDto.id)
            responseNewsDto.status shouldBe HttpStatus.OK
            responseNewsDto.body().body shouldBe newsDto.body
        }
    }

    @Test
    fun `Bad path`() {
        runBlocking {
            newsController.getNewsById(UUID.randomUUID()).status shouldBe HttpStatus.NOT_FOUND
        }
    }
}