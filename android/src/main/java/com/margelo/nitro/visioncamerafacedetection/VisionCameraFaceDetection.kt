package com.margelo.nitro.visioncamerafacedetection

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Base64
import androidx.core.graphics.createBitmap
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.ReactApplicationContext
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

@DoNotStrip
class VisionCameraFaceDetection(private val reactContext: ReactApplicationContext) :
  HybridVisionCameraFaceDetectionSpec() {
  private var faceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    .setMinFaceSize(0.15f)
    .build()

  private var faceDetector = FaceDetection.getClient(faceDetectorOptions)

  override fun initTensor(modelPath: String, count: Double?): String {
    try {
      val assetManager = reactContext.assets
      val byteFile: MappedByteBuffer = loadModelFile(assetManager, modelPath)
      val options = Interpreter.Options()
      options.numThreads = count?.toInt() ?: 1
      interpreter = Interpreter(byteFile, options)
      interpreter?.allocateTensors()
      return "initialization tflite success"
    } catch (e: Exception) {
      e.printStackTrace()
      return e.toString()
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

  override fun detectFromBase64(imageString: String): DetectBas64Type {
    try {
      val decodedString = Base64.decode(imageString, Base64.DEFAULT)
      val bmpStorageResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
      val image = InputImage.fromBitmap(bmpStorageResult, 0)
      val task = faceDetector.process(image)
      val faces = Tasks.await(task)
      var map: DetectBas64Type
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
        val arrayData: DoubleArray = doubleArrayOf()
        for (i: Float in output.array()) {
          arrayData.plus(i.toDouble())
        }
        val base64Image = FaceHelper().getBase64Image(bmpFaceStorage)
        return DetectBas64Type(
          base64 = base64Image ?: "",
          data = arrayData,
          message = "Successfully Get Face",
          leftEyeOpenProbability = face.leftEyeOpenProbability?.toDouble() ?: 0.0,
          rightEyeOpenProbability = face.rightEyeOpenProbability?.toDouble() ?: 0.0,
          smilingProbability = face.smilingProbability?.toDouble() ?: 0.0
        )
      } else {
        return DetectBas64Type(
          base64 = "",
          data = doubleArrayOf(),
          message = "No Face",
          leftEyeOpenProbability = 0.0,
          rightEyeOpenProbability = 0.0,
          smilingProbability = 0.0
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return DetectBas64Type(
        base64 = "",
        data = doubleArrayOf(),
        message = e.printStackTrace().toString(),
        leftEyeOpenProbability = 0.0,
        rightEyeOpenProbability = 0.0,
        smilingProbability = 0.0
      )
    }
  }
}
