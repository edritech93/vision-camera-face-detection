package com.visioncamerafacedetection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mrousavy.camera.frameprocessor.Frame
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessor.VisionCameraProxy
import java.nio.ByteBuffer
import java.nio.FloatBuffer


class VisionCameraFaceDetectionPlugin(proxy: VisionCameraProxy, options: Map<String, Any>?) :
  FrameProcessorPlugin() {
  private var faceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .setMinFaceSize(0.15f)
    .build()

  private var faceDetector = FaceDetection.getClient(faceDetectorOptions)

  override fun callback(frame: Frame, params: Map<String, Any>?): Any? {
    try {
      val mediaImage = frame.image
      val image = InputImage.fromMediaImage(mediaImage, frame.orientation.toDegrees())
      val task = faceDetector.process(image)
      val faces = Tasks.await(task)
      if (faces.size > 0) {
        val face = faces[0]
//        val bmpFrameResult = ImageConvertUtils.getInstance().getUpRightBitmap(image)
//        val bmpFaceResult =
//          Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888)
//        val faceBB = RectF(face.boundingBox)
//        val cvFace = Canvas(bmpFaceResult)
//        val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
//        val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
//        val matrix = Matrix()
//        matrix.postTranslate(-faceBB.left, -faceBB.top)
//        matrix.postScale(sx, sy)
//        cvFace.drawBitmap(bmpFrameResult, matrix, null)
//        val input: ByteBuffer = FaceHelper().bitmap2ByteBuffer(bmpFaceResult)
//        val output: FloatBuffer = FloatBuffer.allocate(192)
//        interpreter?.run(input, output)
//
//        val arrayData: MutableList<Any> = ArrayList()
//        for (i: Float in output.array()) {
//          arrayData.add(i.toDouble())
//        }
        val map: MutableMap<String, Any> = HashMap()
//        map["data"] = arrayData
        map["rollAngle"] =
          face.headEulerAngleZ.toDouble()
        map["pitchAngle"] =
          face.headEulerAngleX.toDouble()
        map["yawAngle"] = face.headEulerAngleY.toDouble()
        map["leftEyeOpenProbability"] = face.leftEyeOpenProbability!!.toDouble()
        map["rightEyeOpenProbability"] = face.rightEyeOpenProbability!!.toDouble()
        map["smilingProbability"] = face.smilingProbability!!.toDouble()
        map["bounds"] = FaceHelper().processBoundingBox(face.boundingBox)
//        map["contours"] = FaceHelper().processFaceContours(face)
        return map
      }
      return null
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }
}
