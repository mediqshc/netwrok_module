package com.homemedics.app.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.databinding.FragmentCheckoutBinding
import com.homemedics.app.interfaces.ContactUsInterface
import com.homemedics.app.interfaces.NegativeAmountInterface
import java.util.*


class DialogUtils(private val activity: Activity) {
    private lateinit var dialog: Dialog
    private val singleButtonView = R.layout.dialog_single_button
    private val twoButtonView = R.layout.dialog_two_button

    fun showSingleButtonsDialog(
        title: String = "",
        message: String = "",
        buttonOneText: String = "",
        buttonOneCallback: () -> Unit = {},
        cancellable: Boolean
    ): Dialog {
        dialog = Dialog(activity)
        dialog.setContentView(singleButtonView)

        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val bButtonOne: Button = dialog.findViewById(R.id.bPositive)

        tvTitle.apply { if (title.isNotEmpty()) text = title }
        tvMessage.apply { if (message.isNotEmpty()) text = message }
        bButtonOne.apply {
            if (buttonOneText.isNotEmpty()) text = buttonOneText
            setOnClickListener {
                dialog.dismiss()
                buttonOneCallback()
            }
        }

        dialog.setCancelable(cancellable)
        dialog.show()

        return dialog
    }

    fun showTwoButtonsDialog(
        title: String = "",
        message: String = "",
        buttonOneText: String = "",
        buttonTwoText: String = "",
        buttonOneCallback: () -> Unit = {},
        buttonTwoCallback: () -> Unit = {},
        cancellable: Boolean
    ): Dialog {
        dialog = Dialog(activity)
        dialog.setContentView(twoButtonView)

        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val bButtonOne: Button = dialog.findViewById(R.id.bPositive)
        val bButtonTwo: Button = dialog.findViewById(R.id.bNegative)

        tvTitle.apply { if (title.isNotEmpty()) text = title }
        tvMessage.apply { if (message.isNotEmpty()) text = message }
        bButtonOne.apply {
            if (buttonOneText.isNotEmpty()) text = buttonOneText
            setOnClickListener {
                dialog.dismiss()
                buttonOneCallback()
            }
        }
        bButtonTwo.apply {
            if (buttonTwoText.isNotEmpty()) text = buttonTwoText
            setOnClickListener {
                dialog.dismiss()
                buttonTwoCallback()
            }
        }
        dialog.setCancelable(cancellable)
        dialog.show()

        return dialog
    }

    fun showSingleButtonAlertDialog(
        title: String = "",
        message: String = "",
         buttonOneText: String = ApplicationClass.mGlobalData?.globalString?.ok.getSafe(),
        buttonCallback: () -> Unit = {},
        cancellable: Boolean = true
    ) {
        AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonOneText) { dialog, _ ->
                buttonCallback()
                dialog.dismiss()
            }
            .setCancelable(cancellable)
            .show()
    }

    fun showDoubleButtonsAlertDialog(
        title: String = "",
        message: String = "",
        positiveButtonText: Int = android.R.string.ok,
        negativeButtonText: Int = android.R.string.cancel,
        negativeButtonStringText: String = "",
        positiveButtonStringText: String = "",
        buttonCallback: () -> Unit = {},
        negativeButtonCallback: () -> Unit = {},
        cancellable: Boolean = true
    ) {

        val positiveClickListener = DialogInterface.OnClickListener { dialog, _ ->
            buttonCallback()
            dialog.dismiss()
        }
        val negButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
            negativeButtonCallback()
            dialog.dismiss()
        }
        val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(message)

        if (positiveButtonStringText.isNotEmpty())
            builder.setPositiveButton(positiveButtonStringText, positiveClickListener)
        else
            builder.setPositiveButton(positiveButtonText, positiveClickListener)
        if (positiveButtonStringText.isNotEmpty())
            builder.setNegativeButton(negativeButtonStringText, negButtonClickListener)
        else
            builder.setNegativeButton(negativeButtonText, negButtonClickListener)
        builder.setCancelable(cancellable)
        builder.show()
    }

    fun showYearAlertDialog(
        buttonCallback: ((String) -> Unit) = {}
    ) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, _, _ ->

                buttonCallback.invoke(year.toString())

            }
        // Date Picker Dialog
        val datePickerDialog = DatePickerDialog(
            activity,
            R.style.CustomDatePickerDialogTheme,
            dateSetListener,
            year,
            month,
            day
        )
        val tv = TextView(activity)

        // Create a TextView programmatically
        val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,  // Width of TextView
            LinearLayout.LayoutParams.WRAP_CONTENT
        ) // Height of TextView
        tv.layoutParams = lp
        val dp30 = tv.resources.getDimensionPixelSize(R.dimen.dp20)
        val dp10 = tv.resources.getDimensionPixelSize(R.dimen.dp10)
        val dp40 = tv.resources.getDimensionPixelSize(R.dimen.dp26)
        tv.setPadding(dp30, dp40, dp10, dp40)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        tv.text = ApplicationClass.mGlobalData?.globalString?.setYear.getSafe()
        tv.setTypeface(null, Typeface.BOLD);

        tv.setTextColor(activity.getColor(R.color.white))
        tv.setBackgroundColor(activity.getColor(R.color.primary))
        datePickerDialog.setCustomTitle(tv)
        datePickerDialog.show()
        datePickerDialog.datePicker.maxDate = c.timeInMillis - 86400000
        datePickerDialog.datePicker.minDate = c.timeInMillis -  1522880000000L
        val days = datePickerDialog.findViewById<View>(
            Resources.getSystem().getIdentifier("android:id/day", null, null)
        )
        if (days != null) {
            days.visibility = View.GONE
        }

        val months = datePickerDialog.findViewById<View>(
            Resources.getSystem().getIdentifier("android:id/month", null, null)
        )
        if (months != null) {
            months.visibility = View.GONE
        }
    }


    fun showForceUpdateAlertDialog(
        title: String = "",
        message: String = "",
        buttonOneText: String = ApplicationClass.mGlobalData?.globalString?.ok.getSafe(),
        buttonCallback: () -> Unit = {},
        cancellable: Boolean = true
    ) {
        AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonOneText) { dialog, _ ->
                buttonCallback()
            }
            .setCancelable(false)
            .show()
    }

    fun showImagePickerDialog(context: Context?, callBack: ContactUsInterface) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.contact_us)
        dialog.window!!
            .setLayout(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
        dialog.window!!.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 60))
        val number = dialog.findViewById<TextView>(R.id.onNumberPress)
        val email = dialog.findViewById<TextView>(R.id.onEmailPress)
        val contact_us = dialog.findViewById<TextView>(R.id.contactUs)
        contact_us.text=ApplicationClass.mGlobalData?.tabString?.contact_us
        number.setOnClickListener { view: View? ->
            dialog.dismiss()
            callBack.onNumberpress()
        }
        email.setOnClickListener { view: View? ->
            dialog.dismiss()
            callBack.onEmailPress()
        }
        dialog.show()
    }

    fun showNegativeAmountDialog(context: Context?, callBack: NegativeAmountInterface){
        val dialog=Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.amountlayout)
        dialog.window!!
            .setLayout(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
        val deletePackageAmountButton=dialog.findViewById<Button>(R.id.button2)
        deletePackageAmountButton.setOnClickListener {
            callBack.onDeletePackageAmount()

        }


    }

}