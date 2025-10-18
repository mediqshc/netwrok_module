package com.fatron.network_module.models.response.aiChat

data class aiAssistant(
    val summary: String,
    val conversation: List<ConversationItem>,
    val recommended_specialty: List<String>,
    val sessionId: String,
    val userId: String
)

data class ConversationItem(
    val user: String,
    val assistant: String,
    val timestamp: String
)

data class BioMarkerVitalSign(
    val id: Int,
    val name: String,
    val value: String,
    val unit:String
)