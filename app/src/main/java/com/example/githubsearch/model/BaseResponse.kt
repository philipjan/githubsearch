package com.example.githubsearch.model

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("total_count")
    var totalCount: Int,
    @SerializedName("incomplete_results")
    var incompleteResult: Boolean,
    @SerializedName("items")
    var items: MutableList<Items>
)

data class Items(
    @SerializedName("html_url")
    var url: String?,
    @SerializedName("description")
    var description: String?,
    @SerializedName("name")
    var repoName: String?,
    @SerializedName("full_name")
    var fullName: String?
)