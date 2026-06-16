package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface NewsRepository : CoroutineCrudRepository<News, UUID> {
    fun findOne(id: UUID): NewsDto
    fun findByTitle(title: String): NewsDto
}
