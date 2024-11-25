package com.example.webviewbridgesample

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback

interface IImageHandler {
    fun takePicture(callBack: ValueCallback<Array<Uri>>?)

    fun uploadImageOnPage(resultCode: Int, intent: Intent?)

    fun checkPermissionAfterReload()
}