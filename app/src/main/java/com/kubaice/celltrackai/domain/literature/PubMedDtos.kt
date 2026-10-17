package com.kubaice.celltrackai.domain.literature

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PubMedSearchResponse(
    @SerialName("esearchresult") val esearchResult: ESearchResult? = null
)

@Serializable
data class ESearchResult(
    val count: String? = null,
    val retmax: String? = null,
    val retstart: String? = null,
    val idlist: List<String> = emptyList()
)

@Serializable
data class PubMedSummaryResponse(
    @SerialName("result") val result: Map<String, PubMedArticleDto>? = null
)

@Serializable
data class PubMedArticleDto(
    val uid: String? = null,
    val title: String? = null,
    val pubdate: String? = null,
    val source: String? = null,
    val authors: List<PubMedAuthorDto> = emptyList(),
    val articleids: List<PubMedArticleIdDto> = emptyList()
)

@Serializable
data class PubMedAuthorDto(
    val name: String? = null,
    val authtype: String? = null
)

@Serializable
data class PubMedArticleIdDto(
    val idtype: String? = null,
    val idvalue: String? = null
)
