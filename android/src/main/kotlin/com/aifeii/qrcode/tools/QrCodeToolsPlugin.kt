package com.aifeii.qrcode.tools

import android.graphics.BitmapFactory
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileInputStream
import java.util.*

class QrCodeToolsPlugin : FlutterPlugin, MethodCallHandler {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "qr_code_tools")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "decoder") {
            val filePath = call.argument<String>("file")
            if(filePath == null){
                result.error("File path is null", null, null)
                return
            }
            val file = File(filePath)
            if (!file.exists()) {
                result.error("File not found. filePath: $filePath", null, null)
                return
            }

            val fis = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(fis)

            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val hints = Hashtable<DecodeHintType, Any>()
            val decodeFormats = ArrayList<BarcodeFormat>()
            decodeFormats.add(BarcodeFormat.QR_CODE)
            hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
            hints[DecodeHintType.CHARACTER_SET] = "utf-8"
            hints[DecodeHintType.TRY_HARDER] = true

            try {
                val decodeResult = MultiFormatReader().decode(binaryBitmap, hints)
                result.success(decodeResult.text)
            } catch (_: NotFoundException) {
                result.error("Not found data", null, null)
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

}
