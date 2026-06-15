package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.finnhjelpemiddelnews.news.NewsController
import no.nav.hm.finnhjelpemiddelnews.news.NewsDto
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import no.nav.hm.finnhjelpemiddelnews.news.NewsOut

@MicronautTest
class NewsControllerTest (
    private val newsController: NewsController,
    private val newsRepository: NewsRepository,
    private val objectMapper: ObjectMapper
) {
    @Test
    fun `Happy path`() {
        @Language("JSON") val data = """
            {
            "description": "Dette er en nyhet"
            }
        """.trimIndent()
        val newsDto = NewsDto(title = "Nyhet 1", data = objectMapper.readTree(data))

        runBlocking {
            newsRepository.save(newsDto)

            val responseNewsDto = newsController.getNewsByTitle(newsDto.title)
            responseNewsDto.status shouldBe HttpStatus.OK
            (responseNewsDto.body() as NewsOut).let {
                it.id shouldBe newsDto.id
                it.title shouldBe newsDto.title
            }
        }
    }

    @Test
    fun `Bad path`() {
        runBlocking {
            newsController.getNewsByTitle("unknown").status shouldBe HttpStatus.BAD_REQUEST
        }
    }
}