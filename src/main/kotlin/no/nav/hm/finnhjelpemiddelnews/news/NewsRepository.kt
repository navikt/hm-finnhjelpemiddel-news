package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface NewsRepository : CoroutineCrudRepository<News, UUID> {
    suspend fun findOne(id: UUID): News
    suspend fun findByTitle(title: String): News
}
