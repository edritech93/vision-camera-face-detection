package com.margelo.nitro.visioncamerafacedetection.extensions

import com.margelo.nitro.visioncamerafacedetection.FaceScannerOptions
import com.margelo.nitro.visioncamerafacedetection.FaceScannerOutputOptions
import com.margelo.nitro.visioncamerafacedetection.TensorFaceOptions
import com.google.mlkit.vision.face.FaceDetectorOptions as MLFaceDetectorOptions

fun FaceScannerOptions.toMLFaceDetectorOptions(): MLFaceDetectorOptions {
  return buildMLFaceDetectorOptions(
    performanceMode,
    runLandmarks,
    runContours,
    runClassifications,
    minFaceSize,
    trackingEnabled
  )
}

fun FaceScannerOutputOptions.toMLFaceDetectorOptions(): MLFaceDetectorOptions {
  return buildMLFaceDetectorOptions(
    performanceMode,
    runLandmarks,
    runContours,
    runClassifications,
    minFaceSize,
    trackingEnabled
  )
}

fun TensorFaceOptions.toMLFaceDetectorOptions(): MLFaceDetectorOptions {
  return buildMLFaceDetectorOptions(
    performanceMode,
    runLandmarks,
    runContours,
    runClassifications,
    minFaceSize,
    trackingEnabled
  )
}

private fun buildMLFaceDetectorOptions(
  performanceMode: Any?,
  runLandmarks: Boolean?,
  runContours: Boolean?,
  runClassifications: Boolean?,
  minFaceSize: Double?,
  trackingEnabled: Boolean?
): MLFaceDetectorOptions {
  var performanceModeValue = MLFaceDetectorOptions.PERFORMANCE_MODE_FAST
  var landmarkModeValue = MLFaceDetectorOptions.LANDMARK_MODE_NONE
  var classificationModeValue = MLFaceDetectorOptions.CLASSIFICATION_MODE_NONE
  var contourModeValue = MLFaceDetectorOptions.CONTOUR_MODE_NONE

  if (performanceMode.toString() == "accurate") {
    performanceModeValue = MLFaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
  }

  if (runLandmarks == true) {
    landmarkModeValue = MLFaceDetectorOptions.LANDMARK_MODE_ALL
  }

  if (runContours == true) {
    contourModeValue = MLFaceDetectorOptions.CONTOUR_MODE_ALL
  }

  if (runClassifications == true) {
    classificationModeValue = MLFaceDetectorOptions.CLASSIFICATION_MODE_ALL
  }

  val optionsBuilder = MLFaceDetectorOptions
    .Builder()
    .setPerformanceMode(performanceModeValue)
    .setLandmarkMode(landmarkModeValue)
    .setContourMode(contourModeValue)
    .setClassificationMode(classificationModeValue)
    .setMinFaceSize((minFaceSize ?: 0.15).toFloat())

  if (trackingEnabled == true) {
    optionsBuilder.enableTracking()
  }

  return optionsBuilder.build()
}
