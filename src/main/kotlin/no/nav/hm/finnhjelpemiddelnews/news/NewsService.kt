package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton

@Singleton
class NewsService(private val newsRepository: NewsRepository,
                  private val tagsRepository: TagsRepository,
                  private val newsTagsRepository: NewsTagsRepository,) {
    suspend fun getNews(
        page: Int,
        size: Int,
        tag: List<String>?,
        search: String?,
        active: Boolean,
    ): Page<NewsDto> {
        val sort = Sort.of(Sort.Order.desc("created"))
        val pageable = Pageable.from(page,size,sort)

        val newsPage = when {
            tag != null && search != null -> {
                val tagsIds = tagsRepository.findByTagIn(tag).map { it.id }
                if (tagsIds.isEmpty()) return Page.empty()
                val newsIds = newsTagsRepository.findByIdTagIdIn(tagsIds).map { it.id.newsId }.distinct()
                if (newsIds.isEmpty()) return Page.empty()
                newsRepository.searchAllByIds(newsIds, "%$search%", pageable, active)

            }
            tag != null -> {
                val tagIds = tagsRepository.findByTagIn(tag).map { it.id }
                if (tagIds.isEmpty()) return Page.empty()
                val newsIds = newsTagsRepository.findByIdTagIdIn(tagIds).map { it.id.newsId }.distinct()
                if (newsIds.isEmpty()) return Page.empty()
                newsRepository.findAllByIds(newsIds, pageable, active)
            }
            search != null -> newsRepository.searchAll("%$search%", pageable, active)
            else -> newsRepository.findAllPaged(pageable, active)
        }

        val newsIds = newsPage.content.map { it.id }
        val tagsByNewsId = newsTagsRepository.findByIdNewsIdIn(newsIds).groupBy { it.id.newsId }
        val allTagNames = tagsRepository.findByIdIn(tagsByNewsId.values.flatten().map { it.id.tagId }).associateBy { it.id }
        val newsDtos = newsPage.content.map { news ->
            val tags = tagsByNewsId[news.id]?.map { allTagNames[it.id.tagId]?.tag ?: "" } ?: emptyList()
            news.toDto(tags)
        }
        return Page.of(newsDtos, pageable, newsPage.totalSize)

    }
}