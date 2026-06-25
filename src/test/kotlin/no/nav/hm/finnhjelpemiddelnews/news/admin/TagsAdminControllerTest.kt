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
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsRepository
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class TagsAdminControllerTest(
    private val tagsAdminController: TagsAdminController,
    private val newsAdminController: NewsAdminController,
    private val tagsRepository: TagsRepository,
    private val newsTagsRepository: NewsTagsRepository,
    private val newsRepository: NewsRepository,
) {
    val news = News(
        title = "Testnyheten", description = "Test", body = "Innhold",
        created = LocalDateTime.now(), publishedFrom = LocalDateTime.now(),
        publishedTo = LocalDateTime.now(), image_url = null
    )

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
    fun linkTagTest() {
        runBlocking {
            val tagId = tagsAdminController.createTags(CreateTagDto(tag = "hjelpemiddel")).body()

            val linkResponse = tagsAdminController.linkTagToNews(tagId, news.id)
            linkResponse.status shouldBe HttpStatus.OK

            val tagsForNews = tagsAdminController.getTagsByNewsId(news.id)
            tagsForNews.status shouldBe HttpStatus.OK
            tagsForNews.body() shouldContain "hjelpemiddel"
        }
    }

    @Test
    fun unlinkTagTest() {
        runBlocking {
            val tagId = tagsAdminController.createTags(CreateTagDto(tag = "fjernmeg")).body()
            tagsAdminController.linkTagToNews(tagId, news.id)

            tagsAdminController.unlinkTagFromNews(tagId, news.id)

            val tagsForNews = tagsAdminController.getTagsByNewsId(news.id)
            tagsForNews.status shouldBe HttpStatus.OK
            (tagsForNews.body() ?: emptyList()) shouldNotContain "fjernmeg"
        }
    }

    @Test
    fun returnTagsOnNewsTest() {
        runBlocking {
            val newsId = newsAdminController.createNews(
                CreateNewsDto(title = "Nyhet med tags", description = "", body = "Innhold",
                    publishedFrom = LocalDateTime.now(), publishedTo = LocalDateTime.now(), image_url = null)
            ).body()
            val tagId = tagsAdminController.createTags(CreateTagDto(tag = "rullestol")).body()
            tagsAdminController.linkTagToNews(tagId, newsId)

            val tagsForNews = tagsAdminController.getTagsByNewsId(newsId)

            tagsForNews.status shouldBe HttpStatus.OK
            tagsForNews.body() shouldContain "rullestol"
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
