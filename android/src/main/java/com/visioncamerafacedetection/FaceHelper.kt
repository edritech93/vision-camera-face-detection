package com.visioncamerafacedetection

import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.ceil

class FaceHelper {
  fun processBoundingBox(boundingBox: Rect): MutableMap<String, Any> {
    val bounds: MutableMap<String, Any> = HashMap()
    // Calculate offset (we need to center the overlay on the target)
    val offsetX = (boundingBox.exactCenterX() - ceil(boundingBox.width().toDouble())) / 2.0f
    val offsetY = (boundingBox.exactCenterY() - ceil(boundingBox.height().toDouble())) / 2.0f
    val x = boundingBox.right + offsetX
    val y = boundingBox.top + offsetY
    bounds["x"] = boundingBox.centerX() + (boundingBox.centerX() - x)
    bounds["y"] = boundingBox.centerY() + (y - boundingBox.centerY())
    bounds["width"] = boundingBox.width().toDouble()
    bounds["height"] = boundingBox.height().toDouble()
    bounds["boundingCenterX"] = boundingBox.centerX().toDouble()
    bounds["boundingCenterY"] = boundingBox.centerY().toDouble()
    bounds["boundingExactCenterX"] = boundingBox.exactCenterX().toDouble()
    bounds["boundingExactCenterY"] = boundingBox.exactCenterY().toDouble()
    return bounds
  }

  fun processFaceContours(face: Face): MutableMap<String, Any> {
    // All faceContours
    val faceContoursTypes = intArrayOf(
      FaceContour.FACE,
      FaceContour.LEFT_EYEBROW_TOP,
      FaceContour.LEFT_EYEBROW_BOTTOM,
      FaceContour.RIGHT_EYEBROW_TOP,
      FaceContour.RIGHT_EYEBROW_BOTTOM,
      FaceContour.LEFT_EYE,
      FaceContour.RIGHT_EYE,
      FaceContour.UPPER_LIP_TOP,
      FaceContour.UPPER_LIP_BOTTOM,
      FaceContour.LOWER_LIP_TOP,
      FaceContour.LOWER_LIP_BOTTOM,
      FaceContour.NOSE_BRIDGE,
      FaceContour.NOSE_BOTTOM,
      FaceContour.LEFT_CHEEK,
      FaceContour.RIGHT_CHEEK
    )
    val faceContoursTypesStrings = arrayOf(
      "FACE",
      "LEFT_EYEBROW_TOP",
      "LEFT_EYEBROW_BOTTOM",
      "RIGHT_EYEBROW_TOP",
      "RIGHT_EYEBROW_BOTTOM",
      "LEFT_EYE",
      "RIGHT_EYE",
      "UPPER_LIP_TOP",
      "UPPER_LIP_BOTTOM",
      "LOWER_LIP_TOP",
      "LOWER_LIP_BOTTOM",
      "NOSE_BRIDGE",
      "NOSE_BOTTOM",
      "LEFT_CHEEK",
      "RIGHT_CHEEK"
    )
    val faceContoursTypesMap: MutableMap<String, Any> = HashMap()
    for (i in faceContoursTypesStrings.indices) {
      val contour = face.getContour(faceContoursTypes[i])
      val points = contour!!.points
      val pointsArray: MutableCollection<Any> = ArrayList()
      for (j in points.indices) {
        val currentPointsMap: MutableMap<String, Any> = HashMap()
        currentPointsMap["x"] = points[j].x.toDouble()
        currentPointsMap["y"] = points[j].y.toDouble()
        pointsArray.add(currentPointsMap)
      }
      faceContoursTypesMap[faceContoursTypesStrings[contour.faceContourType - 1]] = pointsArray
    }
    return faceContoursTypesMap
  }
}
