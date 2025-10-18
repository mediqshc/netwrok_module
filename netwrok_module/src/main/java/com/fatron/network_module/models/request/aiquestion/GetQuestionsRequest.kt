package com.fatron.network_module.models.request.aiquestion

import com.google.gson.annotations.SerializedName

data class GetQuestionsRequest(
    val is_first_question: Int,
    val question_id: Int?,
    val response_text: String?,
    val response_audio_url: String?,
    val question_text: String?,
)
