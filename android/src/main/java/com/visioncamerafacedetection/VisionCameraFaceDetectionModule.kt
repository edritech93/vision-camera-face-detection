package com.visioncamerafacedetection

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Base64
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class VisionCameraFaceDetectionModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var faceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .setMinFaceSize(0.15f)
    .build()

  private var faceDetector = FaceDetection.getClient(faceDetectorOptions)

  @ReactMethod
  private fun initTensor(modelFile: String = "mobile_face_net", count: Int = 1, promise: Promise) {
    try {
      val assetManager = reactContext.assets
      val byteFile: MappedByteBuffer = loadModelFile(assetManager, modelFile)
      val options = Interpreter.Options()
      options.numThreads = count
      interpreter = Interpreter(byteFile, options)
      interpreter?.allocateTensors()
      promise.resolve("initialization tflite success")
    } catch (e: Exception) {
      e.printStackTrace()
      promise.reject(Throwable(e))
    }
  }

  @Throws(IOException::class)
  private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
    val fileDescriptor = assets.openFd("$modelFilename.tflite")
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
  }

  @ReactMethod
  fun detectFromBase64(imageString: String?, promise: Promise) {
    try {
      val decodedString = Base64.decode(imageString, Base64.DEFAULT)
      val bmpStorageResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
      val image = InputImage.fromBitmap(bmpStorageResult, 0)
      val task = faceDetector.process(image)
      val faces = Tasks.await(task)
      val map: WritableMap = WritableNativeMap()
      if (faces.size > 0) {
        val face = faces[0]
        val bmpFaceStorage =
          Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888)
        val faceBB = RectF(face.boundingBox)
        val cvFace = Canvas(bmpFaceStorage)
        val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
        val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
        val matrix = Matrix()
        matrix.postTranslate(-faceBB.left, -faceBB.top)
        matrix.postScale(sx, sy)
        cvFace.drawBitmap(bmpStorageResult, matrix, null)
        val input: ByteBuffer = FaceHelper().bitmap2ByteBuffer(bmpFaceStorage)
        val output: FloatBuffer = FloatBuffer.allocate(192)
        interpreter?.run(input, output)
        val arrayData = Arguments.createArray()
        for (i: Float in output.array()) {
          arrayData.pushDouble(i.toDouble())
        }
        map.putString("message", "Successfully Get Face")
        map.putArray("data", arrayData)
        map.putString("base64", FaceHelper().getBase64Image(bmpFaceStorage))
        promise.resolve(map)
      } else {
        map.putString("message", "No Face")
        map.putArray("data", Arguments.createArray())
        map.putString("base64", "")
        promise.resolve(map)
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
