package com.homemedics.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageActivity
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass

class CameraCrop : ActivityResultContract<CropImageContractOptions, CropImageView.CropResult>() {

    override fun createIntent(context: Context, input: CropImageContractOptions): Intent {
        input.options.validate()
        return Intent(context, CropImageActivity::class.java).apply {
            val bundle = Bundle()
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.options)
            putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle)
        }
    }


    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): CropImageView.CropResult {
        ApplicationClass.localeManager.updateLocaleData(ApplicationClass.getContext(), TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))
        val result =
            intent?.getParcelableExtra<Parcelable>(CropImage.CROP_IMAGE_EXTRA_RESULT) as? CropImage.ActivityResult?
        return if (result == null || resultCode == Activity.RESULT_CANCELED) {
            CropImage.CancelledResult
        } else {
            result
        }
    }
}
