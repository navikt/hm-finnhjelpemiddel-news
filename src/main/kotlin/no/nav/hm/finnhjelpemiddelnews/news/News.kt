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
    val updated: LocalDateTime? = null,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val imageUrl: String?,
    val imageDescription: String?,
    val status: Status,
    val comment: String? = null,
) {
    fun toDto(tags: List<String> = emptyList()): NewsDto = NewsDto(
        id=id, title=title, description=description, body=body, created=created,
        updated=updated, publishedFrom=publishedFrom, publishedTo=publishedTo,
        imageUrl=imageUrl, imageDescription=imageDescription, tags=tags, status=status, comment=comment)

    fun toPublicDto(tags: List<String> = emptyList()): PublicNewsDto = PublicNewsDto(
        id=id, title=title, description=description, body=body, created=created,
        updated=updated, publishedFrom=publishedFrom, publishedTo=publishedTo,
        imageUrl=imageUrl, imageDescription=imageDescription, tags=tags, status=status)
}

@Serdeable
data class CreateNewsDto(
    val title: String,
    val description: String?,
    val body: String,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val imageUrl: String?,
    val imageDescription: String? = null,
    val status: Status,
    val tags: List<String> = emptyList(),
    val comment: String? = null,
)

@Serdeable
@Introspected
data class NewsDto(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val description: String?,
    val body: String,
    val created: LocalDateTime,
    val updated: LocalDateTime?,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val imageUrl: String?,
    val imageDescription: String?,
    val status: Status,
    val tags: List<String> = emptyList(),
    val comment: String? = null,
) {
    fun toPublicDto(): PublicNewsDto = PublicNewsDto(
        id=id, title=title, description=description, body=body, created=created,
        updated=updated, publishedFrom=publishedFrom, publishedTo=publishedTo,
        imageUrl=imageUrl, imageDescription=imageDescription, tags=tags, status=status)
}

@Serdeable
@Introspected
data class PublicNewsDto(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val description: String?,
    val body: String,
    val created: LocalDateTime,
    val updated: LocalDateTime?,
    val publishedFrom: LocalDateTime,
    val publishedTo: LocalDateTime,
    val imageUrl: String?,
    val imageDescription: String?,
    val status: Status,
    val tags: List<String> = emptyList(),
)

