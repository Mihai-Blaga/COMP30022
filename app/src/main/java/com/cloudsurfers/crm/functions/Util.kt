package com.cloudsurfers.crm.functions
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.util.*


class Util{
    companion object {
        fun hideKeyboard(v: View, context: Context){
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
        fun isValidEmail(email: String): Boolean {
            return TextUtils.isEmpty(email) || Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidPhoneNumber(phoneNumber: String): Boolean {
            return TextUtils.isEmpty(phoneNumber) || PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun isValidDate(date: String): Boolean {
            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US).apply { isLenient = false }
            try {
                sdf.parse(date)
            } catch (e: ParseException) {
                return TextUtils.isEmpty(date)
            }

            return true
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun isValidTime(date: String): Boolean {
            val myFormat = "HH:mm"
            val sdf = SimpleDateFormat(myFormat, Locale.US).apply { isLenient = false }
            try {
                sdf.parse(date)
            } catch (e: ParseException) {
                return TextUtils.isEmpty(date)
            }
            return true
        }
    }
}
