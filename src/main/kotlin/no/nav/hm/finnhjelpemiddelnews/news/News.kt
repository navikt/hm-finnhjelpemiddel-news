package no.nav.hm.finnhjelpemiddelnews.news

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
@MappedEntity("news")
data class NewsDto(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: JsonNode,
    val created: LocalDateTime = LocalDateTime.now()
)

@Serdeable
data class CreateNewsDto(
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val data: JsonNode,
)

@Serdeable
data class NewsOut(
    val id: UUID? = UUID.randomUUID(),
    val title: String,
    val data: JsonNode
)
