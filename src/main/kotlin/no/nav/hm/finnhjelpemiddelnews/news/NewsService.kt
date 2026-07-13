package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID

@Singleton
class NewsService(private val newsRepository: NewsRepository,
                  private val tagsRepository: TagsRepository,
                  private val newsTagsRepository: NewsTagsRepository,) {
    suspend fun getNews(
        page: Int,
        size: Int,
        tag: List<String>?,
        search: String?,
        sort: Sort = Sort.of(Sort.Order.desc("created")),
        status: Status? = null,
        publishingStates: List<PublishingState>? = null,
    ): Page<NewsDto> {
        val pageable = Pageable.from(page, size, sort)
        val newsIds = tag?.let { getNewsIdsByTag(it) ?: return Page.empty() }
        val newsPage = newsRepository.findAll(
            filter(newsIds, search?.let { "%$it%" }, status, publishingStates), pageable
        )

        val tagsByNewsId = newsTagsRepository.findByIdNewsIdIn(newsPage.content.map { it.id }).groupBy { it.id.newsId }
        val allTagNames = tagsRepository.findByIdIn(tagsByNewsId.values.flatten().map { it.id.tagId }).associateBy { it.id }
        val dtos = newsPage.content.map { news ->
            news.toDto(tagsByNewsId[news.id]?.map { allTagNames[it.id.tagId]?.tag ?: "" } ?: emptyList())
        }
        return Page.of(dtos, pageable, newsPage.totalSize)
    }

    private suspend fun getNewsIdsByTag(tags: List<String>): List<UUID>? {
        val tagIds = tagsRepository.findByTagIn(tags).map { it.id }
        if (tagIds.isEmpty()) return null
        val ids = newsTagsRepository.findByIdTagIdIn(tagIds).map { it.id.newsId }.distinct()
        return ids.ifEmpty { null }
    }


    fun withIds(ids: List<UUID>): PredicateSpecification<News> =
        PredicateSpecification { root, _ -> root.get<UUID>("id").`in`(ids) }

    fun withSearch(search: String): PredicateSpecification<News> =
        PredicateSpecification { root, cb -> cb.or(
            cb.like(cb.lower(root.get("title")), search.lowercase()),
            cb.like(cb.lower(root.get("description")), search.lowercase()),
        )}

    fun withStatus(status: Status): PredicateSpecification<News> =
        PredicateSpecification { root, cb -> cb.equal(root.get<Status>("status"), status) }

    fun withPublishingState(states: List<PublishingState>): PredicateSpecification<News> {
        val now = LocalDateTime.now()
        return states.map { state ->
            when (state) {
                PublishingState.UPCOMING -> PredicateSpecification<News> { root, cb ->
                    cb.greaterThan(root.get("publishedFrom"), now)
                }
                PublishingState.ACTIVE -> PredicateSpecification<News> { root, cb -> cb.and(
                    cb.lessThanOrEqualTo(root.get("publishedFrom"), now),
                    cb.greaterThanOrEqualTo(root.get("publishedTo"), now),
                )}
                PublishingState.EXPIRED -> PredicateSpecification<News> { root, cb ->
                    cb.lessThan(root.get("publishedTo"), now)
                }
            }
        }.reduce { acc, spec -> acc.or(spec) }
    }

    fun filter(
        ids: List<UUID>? = null,
        search: String? = null,
        status: Status? = null,
        publishingStates: List<PublishingState>? = null,
    ): PredicateSpecification<News>? =
        listOfNotNull(
            ids?.let { withIds(it) },
            search?.let { withSearch(it) },
            status?.let { withStatus(it) },
            publishingStates?.let { withPublishingState(it) },
        ).reduceOrNull { acc, spec -> acc.and(spec) }

}