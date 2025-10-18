package com.fatron.network_module.models.response.aiChat

data class AIQuestionSummaryResponse(
    val attachment_url: String
)

data class Summary(
    val specialities: List<Speciality>,
    val summary: String
)

data class Speciality(
    val icon_url: String,
    val id: Int,
    val name: String
)
