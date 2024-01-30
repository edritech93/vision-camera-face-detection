package com.visioncamerafacedetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Base64
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


class VisionCameraFaceDetectionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var faceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .setMinFaceSize(0.15f)
    .build()

  private var faceDetector = FaceDetection.getClient(faceDetectorOptions)

  @ReactMethod
  fun detectFromBase64(imageString: String?, promise: Promise) {
    try {
      val decodedString = Base64.decode(imageString, Base64.DEFAULT)
      val bmpStorageResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
      val image = InputImage.fromBitmap(bmpStorageResult, 0)
      val task = faceDetector.process(image)
      val faces = Tasks.await(task)
      if (faces.size > 0) {
        val face = faces[0]
        val bmpFaceStorage =
          Bitmap.createBitmap(112, 112, Bitmap.Config.ARGB_8888)
        val faceBB = RectF(face.boundingBox)
        val cvFace = Canvas(bmpFaceStorage)
        val sx = 112 / faceBB.width()
        val sy = 112 / faceBB.height()
        val matrix = Matrix()
        matrix.postTranslate(-faceBB.left, -faceBB.top)
        matrix.postScale(sx, sy)
        cvFace.drawBitmap(bmpStorageResult, matrix, null)
        promise.resolve(FaceHelper().getBase64Image(bmpFaceStorage))
      } else {
        promise.resolve(null)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      promise.reject(Throwable(e))
    }
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "VisionCameraFaceDetectionModule"
  }
}
