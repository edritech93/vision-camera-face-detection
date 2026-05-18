package com.margelo.nitro.visioncamerafacedetection.extensions

import android.graphics.Bitmap
import android.util.Base64
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

var interpreter: Interpreter? = null
const val TF_OD_API_INPUT_SIZE = 112

class FaceHelper {

  private val imageTensorProcessor: ImageProcessor = ImageProcessor.Builder()
    .add(ResizeOp(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
    .add(NormalizeOp(127.5f, 127.5f))
    .build()

  fun bitmap2ByteBuffer(bitmap: Bitmap?): ByteBuffer {
    val imageTensor: TensorImage = imageTensorProcessor.process(TensorImage.fromBitmap(bitmap))
    return imageTensor.buffer
  }

  fun getBase64Image(bitmap: Bitmap): String? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
  }
}
