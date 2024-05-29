package com.homemedics.app.utils

import java.util.regex.Pattern

var expression: String =
    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

val regex = Regex("^[0-9+/.<<>>`|•√π÷×¶∆°^₹€¢£٪©®™؟✓!#\$%&(){|}~:;<=>?؛@*+,./^_`\\'\\\"\\t\\r\\n\\f-]+$")

val MULTIPLE_ZERO_REGEX = Regex("^0+$")

fun isValid(str: String?): Boolean {
    return str != null && str.isNotEmpty()
}
fun isValidRating(str: Double?): Boolean {
    return str != null && str!=0.toDouble()
}

fun isValidName(name: String?): Boolean {
    return name != null && name.isNotEmpty()
}

fun isMaxLengthExceeded(name: String, maxLength: Int): Boolean {
    return name.length > maxLength
}

fun isEmailValid(email: String): Boolean {
    val pattern = Pattern.compile(expression)
    val matcher = pattern.matcher(email)
    return matcher.matches()
}

fun isValidPhone(phone: String?): Boolean {
    return phone != null && phone.isNotEmpty()
}

fun isValidNameLength(text: String): Boolean {
    return text.length >= 30
}

fun isValidPasswordLength(text: String): Boolean {
    return text.length >= 8
}

fun isValidPhoneLength(text: String,phoneLength:Int): Boolean {
    return text.length == phoneLength
}

fun isValidCodeLength(text: String): Boolean {
    return text.length >= 6
}

fun isValidCnicLength(text: String): Boolean {
    return text.length >= 15
}

fun isChecked(isCheck: Boolean?): Boolean {
    return isCheck != null && isCheck
}