package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.auth.AuthResponse
import no.nav.hm.finnhjelpemiddelnews.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelnews.news.CreateTagDto
import no.nav.hm.finnhjelpemiddelnews.news.News
import no.nav.hm.finnhjelpemiddelnews.news.NewsRepository
import no.nav.hm.finnhjelpemiddelnews.news.Status
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class TagsAdminControllerTest(
    private val tagsAdminController: TagsAdminController,
    private val tagsRepository: TagsRepository,
    private val newsRepository: NewsRepository,
) {

    @MockBean(AzureAdUserClient::class)
    fun mockAzureAdUserClient(): AzureAdUserClient = mockk<AzureAdUserClient>().apply {
        coEvery {
            validateToken(any())
        } answers {
            AuthResponse(active = true)
        }
    }

    val news = News(
        title = "Testnyheten", description = "Test", body = "Innhold",
        created = LocalDateTime.now(), publishedFrom = LocalDateTime.now(),
        publishedTo = LocalDateTime.now(), imageUrl = null, imageDescription = "",
        status = Status.PUBLISHED
    )

    @BeforeEach
    fun init() = runBlocking {
        newsRepository.save(news)
        Unit
    }

    @Test
    fun createTagTest() {
        runBlocking {
            val response = tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "Fra hjelpemiddelområdet")
            )

            response.status shouldBe HttpStatus.OK
            response.body() shouldNotBe null

            val saved = tagsRepository.findById(response.body()!!)
            saved shouldNotBe null
            saved!!.tag shouldBe "Fra hjelpemiddelområdet"
        }
    }

    @Test
    fun badBlankTagTest() {
        runBlocking {
            val response = tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "")
            )

            response.status shouldBe HttpStatus.BAD_REQUEST
        }
    }

    @Test
    fun updateTag() {
        runBlocking {
            val tagId = tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "gammel")
            ).body()!!

            val response = tagsAdminController.updateTag(
                authorization = "auth",
                CreateTagDto(tag = "ny"), tagId
            )

            response.status shouldBe HttpStatus.OK
            tagsRepository.findById(tagId)!!.tag shouldBe "ny"
        }
    }

    @Test
    fun deleteTag() {
        runBlocking {
            val tagId = tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "slettemeg")
            ).body()!!
            tagsRepository.existsById(tagId) shouldBe true

            tagsAdminController.deleteTag(
                authorization = "auth",
                tagId
            )

            tagsRepository.existsById(tagId) shouldBe false
        }
    }


    @Test
    fun listAllTagsTest() {
        runBlocking {
            tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "listetest1")
            )
            tagsAdminController.createTags(
                authorization = "auth",
                CreateTagDto(tag = "listetest2")
            )

            val response = tagsAdminController.getTagsList()

            response.status shouldBe HttpStatus.OK
            response.body()?.map { it.tag }?.shouldContain("listetest1")
            response.body()?.map { it.tag }?.shouldContain("listetest2")
        }
    }
}
