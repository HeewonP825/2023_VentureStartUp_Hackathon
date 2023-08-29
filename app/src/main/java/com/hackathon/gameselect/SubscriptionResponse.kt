package com.hackathon.gameselect

data class SubscriptionResponse(
    val kind: String,
    val etag: String,
    val pageInfo: PageInfo,
    val items: List<SubscriptionItem>
)

data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class SubscriptionItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: Snippet
)

data class Snippet(
    val title: String,
    val channelId: String,
    // 추가 필요한 구독 정보 속성들
)
