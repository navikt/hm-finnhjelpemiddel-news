package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.annotation.EmbeddedId
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.serde.annotation.Serdeable
import java.io.Serializable
import java.util.UUID

@Serdeable
@MappedEntity("news_tags")
data class NewsTags(
    @field:EmbeddedId
    val id: NewsTagsId
)

@Serdeable
data class NewsTagsId(
    @field:MappedProperty("tag_id")
    val tagId: UUID,
    @field:MappedProperty("news_id")
    val newsId: UUID
) : Serializable
