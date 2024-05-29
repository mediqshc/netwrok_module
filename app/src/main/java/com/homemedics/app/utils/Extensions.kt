package com.homemedics.app.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.bumptech.glide.Glide
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.user.UserResponse
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.BuildConfig
import com.homemedics.app.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun getAppContext(): Context {
    return ApplicationClass.getContext()
}

//for custom view state mgt
fun ViewGroup.saveChildViewStates(): SparseArray<Parcelable> {
    val childViewStates = SparseArray<Parcelable>()
    children.forEach { child -> child.saveHierarchyState(childViewStates) }
    return childViewStates
}

//for custom view state mgt
fun ViewGroup.restoreChildViewStates(childViewStates: SparseArray<Parcelable>) {
    children.forEach { child -> child.restoreHierarchyState(childViewStates) }
}

fun EditText.isValid(): Boolean {
    if (this.text.toString().isEmpty()) {
        this.requestFocus()
        val error = this.hint.toString() + " " + this.context.getString(R.string.is_empty)
        this.error = error
        return false
    }
    return true
}

fun EditText.getString(): String {
    return this.text.toString()
}

fun EditText.onDone(callback: () -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    maxLines = 1
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}

fun TextView.getString(): String {
    return this.text.toString()
}

fun Bundle.log() {
    for (key in this.keySet()) {
        Log.d("bundle", "$key => ${this.get(key)}")
    }
}

fun getString(@StringRes resId: Int): String {
    return ApplicationClass.getContext().getString(resId)
}

fun String?.getSafe(): String {
    return this ?: ""
}

fun String?.toMultipartBody(paramName: String): MultipartBody.Part? {
    if (this.isNullOrEmpty()) return null
    if (this.startsWith("http")) return null
    val file = File(this)

    val requestBody: RequestBody = file.asRequestBody("image/jpg".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(paramName, file.name, requestBody)
}

fun String?.firstCap(): String {
    if (this.isNullOrEmpty()) return this.getSafe()
    return this.replaceFirstChar { it.uppercase() }
}

fun String?.parseDouble(): Double {
    return this?.toDouble().getSafe()
}

fun getCommaFormatted(value: String): String {
    return try {
        "%,d".format(value.toInt())
    } catch (e: Exception) {
        value
    }
}

fun String?.getCommaRemoved(): String {
    return this.getSafe().replace(",", "")
}

fun String?.getMinusRemoved(): String {
    return this.getSafe().replace("-", "")
}

fun Int?.getSafe(): Int {
    return this ?: 0
}

fun Long?.getSafe(): Long {
    return this ?: 0L
}

fun Double?.round(upto: Int): String {
    if (this == null) return "0.0"
    return String.format("%.${upto}f", this)
}

fun Float?.getSafe(): Float {
    return this ?: 0f
}

fun Double?.getSafe(): Double {
    return this ?: 0.0
}

fun Int?.getBoolean(): Boolean {
    if (this != null && this == 1) return true
    return false
}

fun String?.getBoolean(): Boolean {
    if (this != null && (this == "1" || this == "true")) return true
    return false
}

fun Boolean?.getSafe(): Boolean {
    return this ?: false
}

fun Boolean?.getInt(): Int {
    if (this != null && this) return 1
    return 0
}

fun <T> ArrayList<T>?.getSafe(): ArrayList<T> {
    return this ?: arrayListOf()
}

fun <T> List<T>?.getSafe(): ArrayList<T> {
    return (this ?: arrayListOf()) as ArrayList<T>
}

fun setImage(iv: ImageView, image: String, placeholderRes: Int = 0) {

    Glide.with(iv.context)
        .load(image)
        .placeholder(placeholderRes)
        .fitCenter()
        .into(iv)

}

fun isOnline(context: Context?): Boolean {
    if (context != null) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        if (ni != null && ni.isConnected) return true
    }
    return false
}

fun View.setVisible(show: Boolean = true, invisible: Boolean = false) {
    if (show) this.visibility = View.VISIBLE
    else this.visibility = if (invisible) View.INVISIBLE else View.GONE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun AppCompatImageView.setSvgColor(@ColorRes color: Int) =
    setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)

fun ImageView.loadImage(url: String?, placeholderResID: Int = 0) {
    if (url.isNullOrEmpty()) {
        this.setImageResource(placeholderResID)
        return
    }
    Glide.with(this.context)
        .load(url)
        .placeholder(if (placeholderResID != 0) placeholderResID else R.drawable.ic_placeholder)
        .into(this)
}

fun ImageView.loadImage(url: Uri?, placeholderResID: Int = 0) {
    if (url == null) return
    Glide.with(this.context)
        .load(url)
        .placeholder(if (placeholderResID != 0) placeholderResID else R.drawable.ic_placeholder)
        .into(this)
}

fun ImageView.loadImage(url: Int?) {
    if (url == null) return
    Glide.with(this.context)
        .load(url)
        .into(this)
}

inline fun <reified T> HashMap<*, *>.toPOJO(): T {
    val json = Gson().toJson(this)
    return Gson().fromJson(json, T::class.java)
}

fun navigateToPlayStore(activity: Activity, packageName: String) {

    try {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(packageName)
            )
        )
    } catch (e: ActivityNotFoundException) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")
            )
        )
    }

}

fun showToast(message: String) {
    Toast.makeText(ApplicationClass.application.baseContext, message, Toast.LENGTH_SHORT).show()
}

fun showNetworkError(message: List<String?>?) {
    val context = ApplicationClass.application.baseContext
    try {
        message?.let {
            it[0]?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            } ?: run {
                showToast(context.getString(R.string.something_went_wrong))
            }
        } ?: run {
            showToast(context.getString(R.string.something_went_wrong))
        }
    } catch (e: Exception) {
        showToast(context.getString(R.string.something_went_wrong) + " ${e.message}")
    }
}

fun TextView.setHtmlText(html: String) {

    this.text =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(html)

}

fun getDateInFormat(date: Long, format: String, timeZone: String? = null): String {

    try {
        val calendar: Calendar = Calendar.getInstance()
        calendar.clear()

        calendar.timeInMillis = date
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        if (timeZone != null) sdf.timeZone = TimeZone.getTimeZone(timeZone)
        val currentTimeZone: Date = calendar.time as Date
        return sdf.format(currentTimeZone)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""

}

fun getDateInFormat(date: Long, format: String, locale: Locale): String {

    try {
        val calendar: Calendar = Calendar.getInstance()
        calendar.clear()

        calendar.time = (Date(date * 1000 /*Unix to epoc*/))
        val dateFormatter = SimpleDateFormat(format, locale)
        val currentTimeZone: Date = calendar.time as Date
        return dateFormatter.format(currentTimeZone)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""

}

fun getDateInFormat(
    time: String,
    currentFormat: String,
    format: String,
    timeZone: String? = null
): String {
    try {
        val dateFormat = SimpleDateFormat(currentFormat)
        if (timeZone != null)
            dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        val date = dateFormat.parse(time)
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())

        return dateFormatter.format(date)

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getCurrentDateTime(format: String): String {
    try {
        val calendar: Calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        val currentTimeZone: Date = calendar.time as Date
        return dateFormatter.format(currentTimeZone)

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getHumanReadDateTimeFromTZ(time: String, format: String): String {
    try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .parse(time.replace("Z$", "+0000"))

        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.format(date)

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getLongDateFromString(time: String, format: String, timeZone: String? = null): Long {
    try {
        val sdf = SimpleDateFormat(format)
        if (timeZone != null) sdf.timeZone = TimeZone.getTimeZone(timeZone)
        val date = sdf.parse(time)
        return (date.time) / 1000

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0L
}

fun getAgeFromDate(date: String?, currentFormat: String = "yyyy-mm-dd"): String {
    try {
        date?.let {
            val cal: Calendar = Calendar.getInstance(Locale.US)
            val format = SimpleDateFormat(currentFormat)
            format.parse(date)?.let { cal.time = it }
            val year: Int = cal.get(Calendar.YEAR)
            val month: Int = cal.get(Calendar.MONTH)
            val today = Calendar.getInstance(Locale.US)

            var age: Int = today.get(Calendar.YEAR) - year

            if (today.get(Calendar.YEAR) < year) {
                age--
            }
            var months: Int = today.get(Calendar.MONTH) - month

            if (today.get(Calendar.DAY_OF_MONTH) < month) {
                months--
            }

            return age.toString()

        } ?: run {
            return ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}


fun gotoAppSettings(context: Context) {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri = Uri.fromParts(
        "package",
        BuildConfig.APPLICATION_ID, null
    )
    intent.data = uri
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun getViewMeasurement(v: View): Rect? {
    val locInt = IntArray(2)
    try {
        v.getLocationOnScreen(locInt)
    } catch (npe: NullPointerException) {
        //Happens when the view doesn't exist on screen anymore.
        return null
    }
    val location = Rect()
    location.left = locInt[0]
    location.top = locInt[1]
    location.right = location.left + v.width
    location.bottom = location.top + v.height
    return location
}

fun NavController.safeNavigate(direction: NavDirections) {
    try {
        currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun NavController.safeNavigate(resId: Int, args: Bundle? = null) {
    try {
        if (resId != currentDestination?.id) {
            navigate(resId, args)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun NavController.safeNavigate(
    @IdRes currentDestinationId: Int,
    @IdRes id: Int,
    args: Bundle? = null
) {
    if (currentDestinationId == currentDestination?.id) {
        navigate(id, args)
    }
}

fun getAndroidID(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

fun getDeviceMeta(): String {
    return "${Build.MANUFACTURER} | ${Build.MODEL} | OS: ${Build.VERSION.RELEASE}"
}

fun TextInputEditText.onChangedText(
    mobileNumber: TextInputEditText,
    verifyCode: TextInputEditText,
    btn: Button
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled =
                mobileNumber.text?.isNotEmpty() == true && verifyCode.text?.length == 7
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun FragmentActivity.statusBarColor(color: Int, show: Boolean = true) {
    val window: Window = this.window
    window.apply {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (show) {
            decorView.systemUiVisibility = 0
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
    window.statusBarColor = ContextCompat.getColor(this, color)
}


fun openCalender(
    textView: TextInputEditText,
    format: String = "dd/MM/yyyy",
    restrictFutureDates: Boolean = true,
    restrictPastDates: Boolean = false,
    valueOnlyReturn: Boolean = false,
    canSelectToday: Boolean = true,
    currentDob: String = "",
    onDismiss: ((dateSelected: String) -> Unit)? = null
) {
    val todayCalendarDate = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    var selectedDate = ""

    // Parse the currentDob string into a Date object
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    val currentDate = if (currentDob.isNotEmpty()) {
        sdf.parse(currentDob)
    } else {
        calendar.time // Use the current date if currentDob is empty
    }

    val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            selectedDate = sdf.format(calendar.time)
            if (!valueOnlyReturn) {
                textView.setText(selectedDate)
                textView.setTextColor(textView.resources.getColor(R.color.black, null))
            }
        }

    val dialog = DatePickerDialog(
        textView.context, dateSetListener,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Set the initial selected date in the dialog
    calendar.time = currentDate
    dialog.datePicker.updateDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (restrictFutureDates) {
        if (canSelectToday) {
            dialog.datePicker.maxDate = todayCalendarDate.timeInMillis
        } else {
            dialog.datePicker.maxDate = todayCalendarDate.timeInMillis - 86400000
        }
    }

    if (restrictPastDates) {
        dialog.datePicker.minDate = todayCalendarDate.timeInMillis
    }

    dialog.show()

    dialog.setOnDismissListener {
        onDismiss?.invoke(selectedDate)
    }
}

private fun pad(c: Int): String? {
    return if (c >= 10) c.toString() else "0$c"
}

fun openTimeDialog(
    textView: TextInputEditText,
    dateTimeFormat: String = "hh:mm a",
    parentFragment: FragmentManager,
    valueOnlyReturn: Boolean = false,
    onDismiss: ((timeSelected: String) -> Unit)? = null
) {
    val calender = Calendar.getInstance()
    var selectedTime = ""
    val timePickerDialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
        { _, hour, minute, _ ->
            calender.set(Calendar.HOUR_OF_DAY, hour)
            calender.set(Calendar.MINUTE, minute)

            val myFormat = dateTimeFormat
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            selectedTime = sdf.format(calender.time)
            if (valueOnlyReturn.not())
                textView.setText(selectedTime)
            textView.setTextColor(textView.resources.getColor(R.color.black, null))
        },
        calender.get(Calendar.HOUR_OF_DAY),
        calender.get(Calendar.MINUTE),
        DateFormat.is24HourFormat(textView.context)
    )
    timePickerDialog.isThemeDark = false
    timePickerDialog.setTimeInterval(1, 5, 60)

    timePickerDialog.show(parentFragment, "Timepickerdialog")

    timePickerDialog.setOnDismissListener {
        onDismiss?.invoke(selectedTime)
    }
}

fun Activity.getRealScreenSize(): Pair<Int, Int> { //<width, height> result.first(), result.second()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val size = Point()
        val mode = display?.mode
        Pair(mode?.physicalWidth.getSafe(), mode?.physicalHeight.getSafe())
    } else {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        Pair(size.x, size.y)
    }
}

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}

fun Activity.turnScreenOffAndKeyguardOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}

fun Context.isAppIsInBackground(): Boolean {
    var isInBackground = true
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningProcesses = am.runningAppProcesses
    for (processInfo in runningProcesses) {
        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            for (activeProcess in processInfo.pkgList) {
                if (activeProcess == this.packageName) {
                    isInBackground = false
                }
            }
        }
    }
    return isInBackground
}

/////////////////////////////////////////////////////////////////////////////////////////

fun getLabelFromId(id: String, list: List<GenericItem>?): String {
    return list?.find { it.genericItemId.toString() == id }?.genericItemName.getSafe()
}

fun getLabelsFromId(id: String, list: List<GenericItem>?): String {
    return list?.find { it.genericItemId.toString() == id }?.label.getSafe()
}

fun getGenderIcon(id: String?): Int {
    return when (id) {
        "1" -> R.drawable.ic_male
        "2" -> R.drawable.ic_female_icon
        else -> R.drawable.ic_male
    }
}
// we make an xtension function on UserResponse object
fun UserResponse?.isDoctor(): Boolean {
    if (this == null) return false
    if (this.type == null) return false
    return this.type == Enums.Profession.DOCTOR.value
}

fun UserResponse?.isMedicalStaff(): Boolean {
    if (this == null) return false
    if (this.type == null) return false
    return this.type == Enums.Profession.MEDICAL_STAFF.value
}

fun UserResponse?.isCustomer(): Boolean {
    if (this == null) return false
    if (this.type == null) return false
    return this.type == Enums.Profession.CUSTOMER.value
}

fun getErrorMessage(message: String): String {
    return try {
        val json = Gson().toJson(ApplicationClass.mGlobalData?.messages)
        val jsonObj = JSONObject(json)
        jsonObj.get(message).toString().getSafe()
    } catch (e: Exception) {
        getString(R.string.something_went_wrong)
    }
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun openDefaultMap(latLng: Location, context: FragmentActivity) {
    val gmmIntentUri = Uri.parse(
        "geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}"
    )
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    mapIntent.resolveActivity(context.packageManager)?.let {
        context.startActivity(mapIntent)
    }
}

fun Context.openPhoneDialer(dialNumber: String?) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse(dialNumber.getSafe())
    startActivity(intent)
}

fun Context.getUriFromDrawable(drawableId: Int): Uri? {
    return Uri.parse(
        "android.resource://" + this.packageName.toString() + "/" + drawableId
    )
}

fun String.toFloatOrDefault(defaultValue: Float = 0.0f): Float {
    return try {
        this.toFloat()
    } catch (e: NumberFormatException) {
        if (this == ".") {
            defaultValue
        } else {
            throw e
        }
    }
}