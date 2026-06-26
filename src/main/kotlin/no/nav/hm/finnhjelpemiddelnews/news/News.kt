package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@MappedEntity("news")
data class News(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String?,
    val body: String,
    val created: LocalDateTime,
    val updated: LocalDateTime = LocalDateTime.now(),
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val image_url: String?,
    val imageDescription: String,
) {fun toDto(tags: List<String> = emptyList()): NewsDto {return NewsDto(id=id, title=title, description=description, body=body, created=created,
    updated=updated, publishedFrom=publishedFrom, publishedTo=publishedTo, image_url=image_url, imageDescription=imageDescription, tags=tags)}}

@Serdeable
data class CreateNewsDto(
    val title: String,
    val description: String?,
    val body: String,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val image_url: String?,
    val imageDescription: String,
    val tags: List<String> = emptyList()
)

@Serdeable
@Introspected
data class NewsDto(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val description: String?,
    val body: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val image_url: String?,
    val imageDescription: String,
    val tags: List<String> = emptyList()
)
