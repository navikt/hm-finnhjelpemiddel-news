package no.nav.hm.finnhjelpemiddelnews.news.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import org.slf4j.LoggerFactory
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.finnhjelpemiddelnews.news.CreateTagDto
import no.nav.hm.finnhjelpemiddelnews.news.TagDto
import no.nav.hm.finnhjelpemiddelnews.news.Tags
import no.nav.hm.finnhjelpemiddelnews.news.TagsRepository
import java.util.UUID

@Controller("/admin/tags")
class TagsAdminController(
    private val tagsRepository: TagsRepository,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TagsAdminController::class.java)
    }

    @Post("/")
    fun createTags(
        @Body createTagDto: CreateTagDto
    ): HttpResponse<UUID> {
        return try {
            if (createTagDto.tag.isBlank()) return HttpResponse.badRequest()
            val tag = runBlocking {
                val saved = tagsRepository.save(Tags(
                    tag = createTagDto.tag,
                ))
                saved
            }
            HttpResponse.ok(tag.id)
        } catch (exception: Exception) {
            LOG.error("Failed to create new tag \"$createTagDto\"", exception)
            HttpResponse.serverError()
        }
    }

    @Put("/{id}")
    fun updateTag(
        @Body tagDto: CreateTagDto,
        id: UUID
    ): HttpResponse<String> {
        try {
            runBlocking {
                val tag = tagsRepository.findById(id)
                if(tag != null) {
                    val updatedTag = tag.copy(
                        tag = tagDto.tag,
                    )
                    tagsRepository.update(updatedTag)
                } else throw Exception("Failed to find tag by id $id")
            }
            return HttpResponse.ok("updated $id")
        } catch (exception: Exception) {
            LOG.error("Failed to update tag \"$id\"", exception)
            return HttpResponse.serverError()
        }

    }

    @Delete("/{id}")
    fun deleteTag(
        id: UUID
    ): HttpResponse<String> {
        try {
            runBlocking {
                if (!tagsRepository.existsById(id)) return@runBlocking HttpResponse.badRequest<String>()
                tagsRepository.deleteById(id)
            }
            return HttpResponse.ok("deleted $id")
        } catch (exception: Exception) {
            LOG.error("Failed to delete tag \"$id\"", exception)
            return HttpResponse.serverError()
        }
    }

    @Get("/{id}")
    suspend fun getTagsById(id: UUID): HttpResponse<List<String>> = try {
        val tags = tagsRepository.findTag(id)
        HttpResponse.ok(tags)
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av tags", exception)
        HttpResponse.notFound()
    }

    @Get("/")
    suspend fun getTagsList(): HttpResponse<List<TagDto>> = try {
        tagsRepository.findAll().map { it.toDto() }.toList().let { HttpResponse.ok(it) }
    } catch (exception: Exception) {
        LOG.error("Feil ved henting av tags", exception)
        HttpResponse.notFound()
    }

}
