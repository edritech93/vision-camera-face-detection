package com.visioncamerafacedetection

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mrousavy.camera.frameprocessor.Frame
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessor.VisionCameraProxy

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
        val map: MutableMap<String, Any> = HashMap()
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
