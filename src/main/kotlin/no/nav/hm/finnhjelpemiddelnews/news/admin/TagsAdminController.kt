package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import no.nav.hm.finnhjelpemiddelnews.auth.AuthBody
import no.nav.hm.finnhjelpemiddelnews.auth.AzureAdUserClient
import no.nav.hm.finnhjelpemiddelnews.news.CreateTagDto
import no.nav.hm.finnhjelpemiddelnews.news.NewsTagsRepository
import no.nav.hm.finnhjelpemiddelnews.news.TagDto
import no.nav.hm.finnhjelpemiddelnews.news.Tags
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import org.slf4j.LoggerFactory

@Controller("/admin/tags")
class TagsAdminController(
    private val tagsRepository: TagsRepository,
    private val newsTagsRepository: NewsTagsRepository,
    private val azureAdUserClient: AzureAdUserClient
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TagsAdminController::class.java)
    }

    @Post("/")
    suspend fun createTags(
        @Header("Authorization") authorization: String,
        @Body createTagDto: CreateTagDto
    ): HttpResponse<UUID> {
        if (notAuthenticated(authorization)) return HttpResponse.unauthorized()

        if (createTagDto.tag.isBlank()) return HttpResponse.badRequest()
        return try {
            val tag = tagsRepository.save(Tags(tag = createTagDto.tag))
            HttpResponse.ok(tag.id)
        } catch (exception: Exception) {
            LOG.error("Failed to create new tag \"$createTagDto\"", exception)
            HttpResponse.serverError()
        }
    }

    @Put("/{id}")
    suspend fun updateTag(
        @Header("Authorization") authorization: String,
        @Body tagDto: CreateTagDto, id: UUID
    ): HttpResponse<String> {
        if (notAuthenticated(authorization)) return HttpResponse.unauthorized()

        val tag = tagsRepository.findById(id) ?: return HttpResponse.notFound()
        return try {
            tagsRepository.update(tag.copy(tag = tagDto.tag))
            HttpResponse.ok("updated $id")
        } catch (exception: Exception) {
            LOG.error("Failed to update tag \"$id\"", exception)
            HttpResponse.serverError()
        }
    }

    @Delete("/{id}")
    suspend fun deleteTag(
        @Header("Authorization") authorization: String,
        id: UUID
    ): HttpResponse<String> {
        if (notAuthenticated(authorization)) return HttpResponse.unauthorized()

        if (!tagsRepository.existsById(id)) return HttpResponse.notFound()
        return try {
            tagsRepository.deleteById(id)
            HttpResponse.ok("deleted $id")
        } catch (exception: Exception) {
            LOG.error("Failed to delete tag \"$id\"", exception)
            HttpResponse.serverError()
        }
    }

    @Get("/news/{newsId}")
    suspend fun getTagsByNewsId(newsId: UUID): HttpResponse<List<String>> = try {
        val tagIds = newsTagsRepository.findByIdNewsId(newsId).map { it.id.tagId }
        val tags = if (tagIds.isEmpty()) emptyList() else tagsRepository.findByIdIn(tagIds).map { it.tag }
        HttpResponse.ok(tags)
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av tags for news $newsId", exception)
        HttpResponse.notFound()
    }

    @Get("/")
    suspend fun getTagsList(): HttpResponse<List<TagDto>> = try {
        tagsRepository.findAll().map { it.toDto() }.toList().let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av tags", exception)
        HttpResponse.notFound()
    }

    suspend fun notAuthenticated(authorization: String) =
        !azureAdUserClient.validateToken(AuthBody(token = authorization.removePrefix("Bearer "))).active
}
