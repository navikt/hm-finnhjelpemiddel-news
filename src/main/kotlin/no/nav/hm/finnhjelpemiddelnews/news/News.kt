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
    val updated: LocalDateTime = LocalDateTime.now()
) {fun toDto(): NewsDto {return NewsDto(id=id, title=title, description=description, body=body, created=created, updated=updated)}}

@Serdeable
data class CreateNewsDto(
    val title: String,
    val description: String?,
    val body: String,
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
)
