package com.fatron.network_module.models.response.aiChat

import com.fatron.network_module.models.response.meta.GenericItem

data class AIQuestion(
    val id: Int,
    val question_text: String,
    var question_audio_url: String?,
    val category_id: Int?,
    val type: Int
)

data class QuestionData(
    val status: Int,
    val question: AIQuestion?,
    val summary: String?,
    val specialities: List<GenericItem>?,
    val hospitals: String?,
)

enum class MessageType(val value: Int) {
    QUESTION(1),
    SUMMARY(2),
    EMERGENCY(3);
}


data class SummaryResponse(
    val id: Int,
    val user_id: Int,
    val summary: String?,
    val specialities: List<GenericItem>?,
    val hospitals: String?,
)




