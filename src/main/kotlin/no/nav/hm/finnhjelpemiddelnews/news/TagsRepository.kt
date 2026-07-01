package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface TagsRepository : CoroutineCrudRepository<Tags, UUID> {
    suspend fun findByIdIn(ids: Iterable<UUID>): List<Tags>
    suspend fun findByTag(tag: String): Tags?
}