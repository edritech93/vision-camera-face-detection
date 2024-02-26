package com.visioncamerafacedetection

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Base64
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.ceil

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
    val map: MutableMap<String, Any> = HashMap()
    for (i in faceContoursTypesStrings.indices) {
      val contour = face.getContour(faceContoursTypes[i])
      val points = contour?.points
      val pointsArray: MutableCollection<Any> = ArrayList()
      if (points != null) {
        for (j in points.indices) {
          val currentPointsMap: MutableMap<String, Any> = HashMap()
          currentPointsMap["x"] = points[j].x.toDouble()
          currentPointsMap["y"] = points[j].y.toDouble()
          pointsArray.add(currentPointsMap)
        }
      }
      if (contour != null) {
        map[faceContoursTypesStrings[contour.faceContourType - 1]] = pointsArray
      }
    }
    return map
  }

  fun getBase64Image(bitmap: Bitmap): String? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
  }
}
