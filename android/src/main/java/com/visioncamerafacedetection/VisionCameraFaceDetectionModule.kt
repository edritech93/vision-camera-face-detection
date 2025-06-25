package com.visioncamerafacedetection

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Base64
import androidx.core.graphics.createBitmap
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.module.annotations.ReactModule
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

@ReactModule(name = VisionCameraFaceDetectionModule.NAME)
class VisionCameraFaceDetectionModule(private val reactContext: ReactApplicationContext) :
  NativeVisionCameraFaceDetectionSpec(reactContext) {

  private var faceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .setMinFaceSize(0.15f)
    .build()

  private var faceDetector = FaceDetection.getClient(faceDetectorOptions)


  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "VisionCameraFaceDetection"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun initTensor(modelPath: String?, count: Double?): String? {
    try {
      if (modelPath== null || count == null) {
        return null
      }
      val assetManager = reactContext.assets
      val byteFile: MappedByteBuffer = loadModelFile(assetManager, modelPath)
      val options = Interpreter.Options()
      options.numThreads = count.toInt()
      interpreter = Interpreter(byteFile, options)
      interpreter?.allocateTensors()
      return "initialization tflite success"
    } catch (e: Exception) {
      return e.printStackTrace().toString()
    }
  }

  override fun detectFromBase64(imageString: String?): WritableMap? {
    try {
      val decodedString = Base64.decode(imageString, Base64.DEFAULT)
      val bmpStorageResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
      val image = InputImage.fromBitmap(bmpStorageResult, 0)
      val task = faceDetector.process(image)
      val faces = Tasks.await(task)
      val map: WritableMap = WritableNativeMap()
      if (faces.isNotEmpty()) {
        val face = faces[0]
        val bmpFaceStorage =
          createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE)
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
        return map
      } else {
        map.putString("message", "No Face")
        map.putArray("data", Arguments.createArray())
        map.putString("base64", "")
        return map
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return null
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
}
