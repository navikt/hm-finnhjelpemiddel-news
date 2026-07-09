package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface NewsRepository : CoroutineCrudRepository<News, UUID>,
    CoroutinePageableCrudRepository<News, UUID>,
    CoroutineJpaSpecificationExecutor<News> {
    suspend fun findOne(id: UUID): News
}
