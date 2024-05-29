package com.homemedics.app.model

import com.twilio.conversations.Attributes
import org.json.JSONObject

data class MessageListViewItem(
    val sid: String?=null,
    val uuid: String? = "",
    val index: Long?=null,
    val direction: Int?=null,
    val body: String?=null,
    val author: String?=null,
    val isAuthorChange: Boolean?=null,
    val dateCreated: String?=null,
    val timeCreated: String?=null,
    val sendStatus: Int?=null,
    val type: Int?=null,
    var msgStatus: Int =0,
    val mimetype:String?=null,
    val mediaName:String?=null,
    val mediaSize:String?=null,
    val recordNum:String?=null,
    val recordType:String?=null,
    var mediaUrl:String?=null,
    var attribute: JSONObject? =null,
    var lastReceivedMessage: String? =null
){
    var tag:Int?=null
    var isSelected:Boolean=false
}