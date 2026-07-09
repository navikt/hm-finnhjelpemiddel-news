package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.news.CreateNewsDto
import no.nav.hm.finnhjelpemiddelnews.news.CreateTagDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import no.nav.hm.finnhjelpemiddelnews.news.Status
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class TagsAdminControllerTest(
    private val tagsAdminController: TagsAdminController,
    private val tagsRepository: TagsRepository,
    private val newsRepository: NewsRepository,
) {
    val news = News(
        title = "Testnyheten", description = "Test", body = "Innhold",
        created = LocalDateTime.now(), publishedFrom = LocalDateTime.now(),
        publishedTo = LocalDateTime.now(), image_url = null, imageDescription = "",
        status = Status.PUBLISHED)

    @BeforeEach
    fun init() = runBlocking {
        newsRepository.save(news)
        Unit
    }

    @Test
    fun createTagTest() {
        runBlocking {
            val response = tagsAdminController.createTags(CreateTagDto(tag = "tilskudd"))

            response.status shouldBe HttpStatus.OK
            response.body() shouldNotBe null

            val saved = tagsRepository.findById(response.body())
            saved shouldNotBe null
            saved!!.tag shouldBe "tilskudd"
        }
    }

    @Test
    fun badBlankTagTest() {
        runBlocking {
            val response = tagsAdminController.createTags(CreateTagDto(tag = ""))

            response.status shouldBe HttpStatus.BAD_REQUEST
        }
    }

    @Test
    fun updateTag() {
        runBlocking {
            val tagId = tagsAdminController.createTags(CreateTagDto(tag = "gammel")).body()

            val response = tagsAdminController.updateTag(CreateTagDto(tag = "ny"), tagId)

            response.status shouldBe HttpStatus.OK
            tagsRepository.findById(tagId)!!.tag shouldBe "ny"
        }
    }

    @Test
    fun deleteTag() {
        runBlocking {
            val tagId = tagsAdminController.createTags(CreateTagDto(tag = "slettemeg")).body()
            tagsRepository.existsById(tagId) shouldBe true

            tagsAdminController.deleteTag(tagId)

            tagsRepository.existsById(tagId) shouldBe false
        }
    }


    @Test
    fun listAllTagsTest() {
        runBlocking {
            tagsAdminController.createTags(CreateTagDto(tag = "listetest1"))
            tagsAdminController.createTags(CreateTagDto(tag = "listetest2"))

            val response = tagsAdminController.getTagsList()

            response.status shouldBe HttpStatus.OK
            response.body().map { it.tag } shouldContain "listetest1"
            response.body().map { it.tag } shouldContain "listetest2"
        }
    }
}
