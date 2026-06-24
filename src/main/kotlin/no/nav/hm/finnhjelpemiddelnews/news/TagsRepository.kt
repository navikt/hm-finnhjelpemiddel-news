package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface TagsRepository : CoroutineCrudRepository<Tags, UUID> {
    fun findTag(tagId: UUID): List<String>
    fun deleteTag(tagId: UUID)
}