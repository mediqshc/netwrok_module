package com.homemedics.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.homemedics.app.BuildConfig
import com.homemedics.app.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.net.URL
import java.net.URLConnection


class FileUtils {
    /**
     * TODO: INSTRUCTIONS TO USE
     * - initialize class instance anywhere you want to use
     * - call init method to initialize resultLaunchers
     * - call desired result launcher type method
     * - receive data in FileData class format
     *
     * - currentFileUri, currentFilePath, file -> are only related to camera feature
     * - createPDFFile() createImageFile() are being used for relative tasks
     *
     * - add queries in manifest if you are targeting android 11 for both image and file
     *
     * <queries>
    <intent>
    <action android:name="android.media.action.IMAGE_CAPTURE" />
    </intent>
    <intent>
    <action android:name="android.intent.action.GET_CONTENT" />
    </intent>
    <intent>
    <action android:name="android.intent.action.OPEN_DOCUMENT" />
    <data android:mimeType="document/pdf" />
    </intent>
    </queries>
     */

    val RC_ASK_PERMISSION = 101

    private lateinit var multiTypeResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var singleTypeResultLauncher: ActivityResultLauncher<String>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickOrCaptureResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    private lateinit var permissionsFileResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionsAudioResultLauncher: ActivityResultLauncher<String>
    private lateinit var permissionsResultLauncher: ActivityResultLauncher<Array<String>>
    private var successCallback: ((FileData?) -> Unit)? = null
    private var voiceSuccessCallback: ((Boolean) -> Unit)? = null
    private var permissionSuccessCallback: ((FileData?) -> Unit)? = null
    private var currentFileUri: Uri? = null
    private var currentFilePath: String? = null
    private var addImageCheck: Boolean = false
    private var docCheck: Boolean = false
    private var cameraCaptureOnly: Boolean = false
    private var isChooser: Boolean = false
    var file: File? = null

    companion object {
        val typePDF = "application/pdf"
        val typeDOC = "application/msword"
        val typeDOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        val typeImage = "image/*"
        val typeOther = "application/octet-stream"
    }

    lateinit var activity: Context

    fun init(activity: AppCompatActivity) {

        this.activity = activity.applicationContext
        multiTypeResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
                result?.let {
                    val path = createCopyAndReturnRealPath(activity.baseContext, it)
                    successCallback?.invoke(FileData(path = path, uri = it))
                }
            }

        singleTypeResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                result?.let {
                    val path = createCopyAndReturnRealPath(activity.baseContext, it)
                    successCallback?.invoke(FileData(path = path, uri = it))
                }
            }

        cameraResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    successCallback?.invoke(FileData(path = currentFilePath, uri = currentFileUri))
                }
            }

        pickOrCaptureResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {//dont check for data != null because it will be null in camera case
                    processFile(activity.baseContext, result.data?.data) {
                        successCallback?.invoke(it)
                    }
                }
            }
    }

    fun init(fragment: Fragment) {

        this.activity = fragment.requireContext()

        multiTypeResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->

                result?.let {
                    fragment.context?.let { it1 ->
                        processFile(it1, result) {
                            permissionSuccessCallback?.invoke(
                                FileData(
                                    path = it.path,
                                    uri = currentFileUri
                                )
                            )
                        }
                    }
                }

            }

        singleTypeResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                result?.let {
                    fragment.context?.let { it1 ->
                        processFile(it1, result) {
                            permissionSuccessCallback?.invoke(
                                FileData(
                                    path = it.path,
                                    uri = currentFileUri
                                )
                            )
                        }
                    }
                }
//                result?.let {
//                    val path = createCopyAndReturnRealPath(fragment.requireContext(), it)
//                    successCallback?.invoke(FileData(path = path, uri = it))
//                }
            }

        cameraResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {

                    fragment.context?.let { it1 ->
                        processFile(it1, currentFileUri) {
                            permissionSuccessCallback?.invoke(
                                FileData(
                                    path = it.path,
                                    uri = currentFileUri
                                )
                            )
                        }
                    }

                }
            }
        cropImage = fragment.registerForActivityResult(CameraCrop()) { result ->
            when {
                result.isSuccessful -> {
                    val imageUri = Uri.parse(result.uriContent.toString())
                    if (imageUri != null) {
                        if (file?.exists() == true) {
                            file?.delete()
                        }

                        permissionSuccessCallback?.invoke(
                            FileData(
                                path = result.uriContent.toString(),
                                uri = imageUri
                            )
                        )

                    }

                }
            }
        }

        pickOrCaptureResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {//dont check for data != null because it will be null in camera case
                    if (addImageCheck) {
                        var imageUri = currentFileUri
                        if (result.data != null && result?.data?.data != null)
                            imageUri = result.data?.data

                        cropImage.launch(
                            imageUri?.let { setCropOption(it) }
                        )
                    } else {
                        result?.let {
                            fragment.context?.let { it1 ->
                                processFile(it1, result.data?.data) {

                                    permissionSuccessCallback?.invoke(
                                        FileData(
                                            path = it.path,
                                            uri = currentFileUri
                                        )
                                    )
                                }
                            }
                        }
                    }

                }

            }

        permissionsResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
                if (success.containsValue(false)) {
                    permissionSuccessCallback?.invoke(null)
                } else {
                    if (isChooser) {
                        chooseOrCaptureFile()
                    } else {
                        if (cameraCaptureOnly) {
                            if (addImageCheck) {
                                chooseFile(fileType = typeImage, permissionSuccessCallback)
                            } else
                                captureFromCamera(permissionSuccessCallback)
                        } else {
                            chooseOrCaptureImage()
                        }
                    }
                }
            }
        permissionsAudioResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
                if (success) {
                    voiceSuccessCallback?.invoke(true)
                } else {
                    voiceSuccessCallback?.invoke(false)
                }
            }

        permissionsFileResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
                if (success.containsValue(false)) {
                    permissionSuccessCallback?.invoke(null)
                } else {
                    var fileTypes = arrayOf(typePDF)
                    if (docCheck) {
                        fileTypes += typeDOC
                        fileTypes += typeDOCX
                    }
                    if (addImageCheck) {
                        fileTypes += typeImage
                    }
                    chooseFile(fileTypes = fileTypes, permissionSuccessCallback)
                }
            }

    }

    fun photoUploadValidation(file: File, size: Long): Boolean {
        val maxFileSize = size * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize >= maxFileSize
    }

    private fun setCropOption(uri: Uri): CropImageContractOptions {
        return options(uri) {
            setScaleType(CropImageView.ScaleType.FIT_CENTER)
            setCropShape(CropImageView.CropShape.RECTANGLE)
            setGuidelines(CropImageView.Guidelines.ON_TOUCH)
            setAspectRatio(1, 1)
            setMaxZoom(4)
            setAutoZoomEnabled(true)
            setMultiTouchEnabled(true)
            setCenterMoveEnabled(true)
            setShowCropOverlay(true)
            setAllowFlipping(true)
            setSnapRadius(3f)
            setTouchRadius(48f)
            setInitialCropWindowPaddingRatio(0.1f)
            setBorderLineThickness(3f)
            setBorderLineColor(Color.argb(170, 255, 255, 255))
            setBorderCornerThickness(2f)
            setBorderCornerOffset(5f)
            setBorderCornerLength(14f)
            setBorderCornerColor(Color.WHITE)
            setGuidelinesThickness(1f)
            setGuidelinesColor(R.color.white)
            setBackgroundColor(Color.argb(119, 0, 0, 0))
            setMinCropWindowSize(24, 24)
            setMinCropResultSize(20, 20)
            setMaxCropResultSize(99999, 99999)
            setActivityTitle("")
            setActivityMenuIconColor(0)
            setOutputUri(null)
            setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
            setOutputCompressQuality(10)
            setRequestedSize(0, 0)
            setRequestedSize(0, 0, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
            setInitialCropWindowRectangle(null)
//            setInitialRotation(0)
            setAllowCounterRotation(false)
            setFlipHorizontally(false)
            setFlipVertically(false)
            setCropMenuCropButtonTitle(null)
            setCropMenuCropButtonIcon(0)
            setAllowRotation(false)
            setNoOutputImage(false)
            setFixAspectRatio(true)
        }
    }

    fun chooseFile(fileTypes: Array<String>, onSuccess: ((FileData?) -> Unit)? = null) {
        permissionSuccessCallback = onSuccess
        multiTypeResultLauncher.launch(fileTypes)
    }


    fun chooseFile(fileType: String, onSuccess: ((FileData?) -> Unit)? = null) {
        permissionSuccessCallback = onSuccess
        singleTypeResultLauncher.launch(fileType)
    }

    fun captureFromCamera(onSuccess: ((FileData?) -> Unit)? = null) {
        permissionSuccessCallback = onSuccess
        createImageFile("", activity)
        cameraResultLauncher.launch(currentFileUri)
    }

    fun chooseOrCaptureImage(onSuccess: ((FileData?) -> Unit)? = null) {
        if (hasPermissions(activity).not()) {
            onSuccess?.let {
                requestPermissions(
                    activity as Activity,
                    addImageCheck,
                    callback = it
                )
            }
        } else {
            successCallback = onSuccess
            pickOrCaptureResultLauncher.launch(getPickImageIntent(activity))
        }
    }

    fun chooseOrCaptureFile(onSuccess: ((FileData?) -> Unit)? = null) {
        successCallback = onSuccess
        pickOrCaptureResultLauncher.launch(getCaptureOrPickFileIntent(activity))
    }

    fun processFile(context: Context, data: Uri?, callback: ((FileData) -> Unit)? = null) {
        Log.d("FileUtils", "filepath: $currentFilePath")
        val fileData = FileData()
        try {
            data?.let {//file from explorer or gallery
                val inputStream: InputStream =
                    context.contentResolver.openInputStream(data)!!
                if (getMimeType(context, it)?.contains("image", true) == true) {
                    val bm = BitmapFactory.decodeStream(inputStream)
                    saveBitmap(data.path, bm) { filePath ->
                        fileData.path = filePath
                    }
                    fileData.bitmap = bm
                    fileData.type = typeImage
                } else if (getMimeType(context, it)?.contains("pdf", true) == true) {
                    var path = data.path
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        path = data.toString()
                    saveFile(path = path, inputStream) { filePath ->
                        fileData.path = filePath
                    }
                    fileData.type = typePDF
                } else if (getMimeType(context, it)?.contains("msword", true) == true || getMimeType(context, it)?.contains(
                        typeDOCX, true) == true) {
                    var path = data.path
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        path = data.toString()
                    saveFile(path = path, inputStream) { filePath ->
                        fileData.path = filePath
                    }
                 if(getMimeType(context, it)?.contains(
                         typeDOCX, true) == true)
                     fileData.type = typeDOCX
                    else
                    fileData.type = typeDOC
                }
            } ?: run { //may be camera image
                val bm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            currentFileUri ?: Uri.parse("")

                        )
                    )

                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(file))
                }
                 val l =file?.length().toString()
                 var m =l.toInt()
                if (m>2048000) {
                    saveBitmap(currentFileUri.toString(), bm)
                }
                fileData.bitmap = bm
                fileData.path = currentFilePath
                fileData.type = typeImage
                fileData.uri = currentFileUri
            }

            if (callback != null) {
                callback(fileData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun getPickImageIntent(context: Context): Intent? {
        var chooserIntent: Intent? = null
        var intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = createImageFile("", context)
        currentFileUri = getUriFromFile(context, file ?: File(""))
        currentFilePath = file?.absolutePath.getSafe()
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentFileUri)

        intentList = addIntentsToList(context, intentList, pickIntent)
        intentList = addIntentsToList(context, intentList, takePhotoIntent)

        if (intentList.size > 0) {
            chooserIntent = Intent.createChooser(
                intentList.removeAt(intentList.size - 1),
                "Select an option"
            )
            chooserIntent!!.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intentList.toTypedArray<Parcelable>()
            )
        }

        return chooserIntent
    }

    private fun getCaptureOrPickFileIntent(context: Context): Intent? {
        var chooserIntent: Intent? = null
        var intentList: MutableList<Intent> = ArrayList()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = createImageFile("", context)
        currentFileUri = getUriFromFile(context, file ?: File(""))
        currentFilePath = file?.absolutePath.getSafe()
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentFileUri)

        val fileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        fileIntent.type = typePDF

        intentList = addIntentsToList(context, intentList, takePhotoIntent)
        intentList = addIntentsToList(context, intentList, pickIntent)
        intentList = addIntentsToList(context, intentList, fileIntent)

        if (intentList.size > 0) {
            chooserIntent = Intent.createChooser(
                intentList.removeAt(intentList.size - 1),
                "Select an option"
            )
            chooserIntent!!.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intentList.toTypedArray<Parcelable>()
            )
        }

        return chooserIntent
    }

    fun getUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
    }

    @Throws(IOException::class)
    fun createImageFile(path: String?, context: Context): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        val fileName = "${timeStamp}.jpg"
        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file.apply {
            currentFileUri = getUriFromFile(context = context, this)
            currentFilePath = absolutePath
        }

    }

    @Throws(IOException::class)
    fun createVoiceFile(context: Context): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
        val fileName = "${timeStamp}.mp3"
        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file.apply {
            currentFileUri = getUriFromFile(context = context, this)
            currentFilePath = absolutePath
        }

//       return File.createTempFile(
//            "${timeStamp}_WAV_", /* prefix */
//            ".wav", /* suffix */
//            storageDir /* directory */
//        ).apply {
//            currentFileUri = getUriFromFile(context = context, this)
//            currentFilePath = absolutePath
//        }
    }


    @Throws(IOException::class)
    fun createFile(context: Context, path: String?): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        var fileName = "${timeStamp}.pdf"
        if (getMimeType(context, Uri.parse(path)) == typeDOC)
            fileName = "${timeStamp}.doc"
        if (path.isNullOrEmpty().not())
            fileName = getFileNameFromUri(context = context, Uri.parse(path))

        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file.apply {
            currentFileUri = getUriFromFile(context = context, this)
            currentFilePath = absolutePath
        }

//        File.createTempFile(
//            "abc" , /* prefix */
//            ".pdf", /* suffix */
//            storageDir /* directory */
//        ).apply {
//            currentFileUri = getUriFromFile(context = context, this)
//            currentFilePath = absolutePath
//
//        }
    }

    @Throws(IOException::class)
    fun createPDFFile(context: Context, path: String?): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        var fileName = "${timeStamp}.pdf"
        if (path.isNullOrEmpty().not())
            fileName = getFileNameFromUri(context = context, Uri.parse(path))

        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file.apply {
            currentFileUri = getUriFromFile(context = context, this)
            currentFilePath = absolutePath
        }
    }

    private fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "IMG_" + System.currentTimeMillis(),
                null
            )
        return Uri.parse(path)
    }


    private fun addIntentsToList(
        context: Context,
        list: MutableList<Intent>,
        intent: Intent
    ): MutableList<Intent> {
        val resInfo = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfo) {
            val packageName = resolveInfo.activityInfo.packageName
            val targetedIntent = Intent(intent)
            targetedIntent.setPackage(packageName)
            list.add(targetedIntent)
        }
        return list
    }


    fun saveBitmap(path: String?, bitmap: Bitmap, callback: ((String) -> Unit)? = null) {

        try {

            val file = createImageFile(path, activity)
            val oStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, oStream)
            oStream.flush()
            oStream.close()
            callback?.invoke(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveFile(
        path: String?,
        inputStream: InputStream,
        callback: ((String) -> Unit)? = null
    ) {
        try {
            val file = createFile(activity, path)
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            callback?.invoke(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun hasPermissions(context: Context): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

    }

    fun hasPermissions(context: Context, permission: Array<String>): Boolean {
        permission.forEach { perm ->
            if (ContextCompat.checkSelfPermission(
                    context, perm
                ) != PackageManager.PERMISSION_GRANTED
            )
                return false
        }

        return true
    }

    fun requestPermissions(activity: Activity) {
//        permissionsResultLauncher.launch(
//            arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA
//            )
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissionSuccessCallback = callback
            permissionsResultLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
//            permissionSuccessCallback = callback
            permissionsResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                )
            )
        }
    }

    fun requestFilePermission(activity: Activity) {
        permissionsFileResultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    fun requestAudioPermission(activity: Activity, onSuccess: ((Boolean) -> Unit)? = null) {
        voiceSuccessCallback = onSuccess
        permissionsAudioResultLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun requestPermissions(
        activity: Activity,
        mOnlyImageCheck: Boolean = false,
        mCameraCaptureOnly: Boolean? = false,
        mIsChooser: Boolean? = false,
        callback: (fileUtils: FileData?) -> Unit
    ) {
        addImageCheck = mOnlyImageCheck
        cameraCaptureOnly = mCameraCaptureOnly.getSafe()
        isChooser = mIsChooser.getSafe()
        permissionSuccessCallback = callback
//        permissionsResultLauncher.launch(
//            arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA
//            )
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsResultLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            permissionsResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                )
            )
        }
    }

    fun requestFilePermissions(
        activity: Activity,
        mOnlyImageCheck: Boolean = true,
        mDocCheck: Boolean = false,
        callback: (fileUtils: FileData?) -> Unit
    ) {
        addImageCheck = mOnlyImageCheck
        docCheck = mDocCheck
        permissionSuccessCallback = callback
//        permissionsFileResultLauncher.launch(
//            arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsFileResultLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            permissionsFileResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                )
            )
        }
    }


    fun createCopyAndReturnRealPath(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver ?: return null
        val mimeType = getMimeType(context, uri).getSafe()
        val fileExt = "." + mimeType.substring(mimeType.indexOf('/') + 1)
        val file = createImageFile("", context)
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (ignore: IOException) {
            return null
        }
        return file.absolutePath
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase()
            )
        }
        if (mimeType?.contains("audio/mpeg").getSafe())
            mimeType = "audio/mp3"

        return mimeType
    }

    fun getUriFromPath(context: Context, path: String): Uri {
        return getUriFromFile(context, File(path))
    }


    ///////////////////// generic file path work start /////////////////////

    @SuppressLint("NewApi")
    fun getGeneralFilePath(context: Context, uri: Uri): String? {
        // check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // DocumentProvider
        if (isKitKat) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                val fullPath: String? = getPathFromExtSD(split)
                return if (fullPath !== "") {
                    fullPath
                } else {
                    null
                }
            }


            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val id: String
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(
                            uri,
                            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val fileName = cursor.getString(0)
                            val path = Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                            if (!TextUtils.isEmpty(path)) {
                                return path
                            }
                        }
                    } finally {
                        cursor?.close()
                    }
                    id = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        )
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            return try {
                                val contentUri = ContentUris.withAppendedId(
                                    Uri.parse(contentUriPrefix),
                                    java.lang.Long.valueOf(id)
                                )
                                getDataColumn(context, contentUri, null.toString(), emptyArray())
                            } catch (e: NumberFormatException) {
                                //In Android 8 and Android P the id is not a number
                                uri.path!!.replaceFirst("^/document/raw:".toRegex(), "")
                                    .replaceFirst("^raw:".toRegex(), "")
                            }
                        }
                    }
                } else {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    var contentUri: Uri? = null
                    try {
                        contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null.toString(), emptyArray())
                    }
                }
            }


            // MediaProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return contentUri?.let {
                    getDataColumn(
                        context, it, selection,
                        selectionArgs
                    )
                }
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(context, uri)
            }
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(context, uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(context, uri)
                }
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    // return getFilePathFromURI(context,uri);
                    copyFileToInternalStorage(context, uri, "userfiles")
                    // return getRealPathFromURI(context,uri);
                } else {
                    getDataColumn(context, uri, null.toString(), emptyArray())
                }
            }
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } else {
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(context, uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                val projection = arrayOf(
                    MediaStore.Images.Media.DATA
                )
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver
                        .query(uri, projection, selection, selectionArgs, null)

                    if (cursor == null) return null

                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    private fun getPathFromExtSD(pathData: Array<String>): String? {
        val type = pathData[0]
        val relativePath = "/" + pathData[1]
        var fullPath = ""

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
        if (fileExists(fullPath)) {
            return fullPath
        }
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
        return if (fileExists(fullPath)) {
            fullPath
        } else fullPath
    }

    private fun getDriveFilePath(context: Context, uri: Uri): String? {
        val returnCursor: Cursor = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        ) ?: return null

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name)
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream.available()

            //int bufferSize = 1024;
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private fun copyFileToInternalStorage(context: Context, uri: Uri, newDirName: String): String? {
        val returnCursor: Cursor = context.getContentResolver().query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        ) ?: return null

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val output: File
        if (newDirName != "") {
            val dir = File(context.filesDir.toString() + "/" + newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            output = File(context.filesDir.toString() + "/" + newDirName + "/" + name)
        } else {
            output = File(context.filesDir.toString() + "/" + name)
        }
        try {
            val inputStream: InputStream =
                context.contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return output.path
    }

    private fun getFilePathForWhatsApp(context: Context, uri: Uri): String? {
        return copyFileToInternalStorage(context, uri, "whatsapp")
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String,
        selectionArgs: Array<String>
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getBitmapFromPDF(filePath: String): Bitmap {
        val file = File(filePath)

        // Create the page renderer for the PDF document.
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)

        // Open the page to be rendered.
        val page = pdfRenderer.openPage(0)

        // Render the page to the bitmap.
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // Close the page when you are done with it.
        page.close()

        // Close the `PdfRenderer` when you are done with it.
        pdfRenderer.close()

        return bitmap
    }

    fun downloadFile(
        activity: Activity,
        url: String,
        success: ((fileData: FileData) -> Unit)? = null
    ) {
        Thread {
            try {
                val outputFile = createPDFFile(activity, "")

                val u = URL(url)
                val conn: URLConnection = u.openConnection()
                val contentLength: Int = conn.contentLength
                val stream = DataInputStream(u.openStream())
                val buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                stream.close()
                val fos = DataOutputStream(FileOutputStream(outputFile))
                fos.write(buffer)
                fos.flush()
                fos.close()

                val fileData = FileData(path = outputFile.path)
                activity.runOnUiThread {
                    success?.invoke(fileData)
                }

            } catch (e: FileNotFoundException) {
                return@Thread  // swallow a 404
            } catch (e: IOException) {
                return@Thread  // swallow a 404
            }
        }.start()
    }

    fun requestPermissions(activity: FragmentActivity, callback: () -> Unit) {
        permissionsResultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
    }

    fun copyUriToFile(
        context: Context,
        uri: Uri,
        fileNameFromUri: String,
        mediaType: String
    ): File? {
        val f = File(context.externalCacheDir, fileNameFromUri)
        try {
            val `in`: InputStream? = context.contentResolver.openInputStream(uri)
            val out: OutputStream = FileOutputStream(f)
            val buf = ByteArray(1024)
            var len: Int
            if (`in` != null) {
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
            out.close()
            `in`?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return f
    }

    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = ""
        context.contentResolver
            .query(uri, null, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    ) ?: cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                }
            }
        if (name.isEmpty()) {
            name = uri.lastPathSegment.toString()
        }
        return name
    }

    fun convertFileToMultiPart(
        file: File,
        imageType: String,
        imageName: String
    ): MultipartBody.Part {
        return file.asRequestBody(imageType.toMediaType()).let {
            MultipartBody.Part.createFormData(
                imageName,
                file.name,
                it
            )
        }
    }

    fun downloadFile(url: String) {
        if (::activity.isInitialized) {
            val downloadManager: DownloadManager =
                activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri =
                Uri.parse(url)
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //call the create file function

//        request.setDestinationUri(currentFileUri)
            downloadManager.enqueue(request)
        }

    }
    ///////////////////// generic file path work end /////////////////////

    class FileData {
        var path: String? = null
        var uri: Uri? = null
        var type: String = ""
        var data: Intent? = null
        var bitmap: Bitmap? = null

        constructor(path: String? = null, uri: Uri? = null, type: String = "") {
            this.path = path
            this.uri = uri
            this.type = type
        }

        constructor(data: Intent? = null, bitmap: Bitmap? = null) {
            this.data = data
            this.bitmap = bitmap
        }
    }
}