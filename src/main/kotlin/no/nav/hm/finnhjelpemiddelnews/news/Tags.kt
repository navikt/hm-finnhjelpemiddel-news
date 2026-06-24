package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
@MappedEntity("tags")
data class Tags(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val tag: String
) {fun toDto(): TagDto {return TagDto(id=id, tag=tag)}}

@Serdeable
data class CreateTagDto(
    val tag: String
)

@Serdeable
@Introspected
data class TagDto(
    val id: UUID? = UUID.randomUUID(),
    val tag: String
)
