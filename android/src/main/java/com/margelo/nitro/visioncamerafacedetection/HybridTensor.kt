package com.margelo.nitro.visioncamerafacedetection

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Base64
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.NullType
import com.margelo.nitro.visioncamerafacedetection.extensions.FaceHelper
import com.margelo.nitro.visioncamerafacedetection.extensions.TF_OD_API_INPUT_SIZE
import com.margelo.nitro.visioncamerafacedetection.extensions.interpreter
import com.margelo.nitro.visioncamerafacedetection.extensions.toMLFaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HybridTensor : HybridTensorFactorySpec() {

  private val context =
    NitroModules.applicationContext ?: throw Error("Face Tensor - No Context available!")

  override fun initTensor(modelName: String): String {
    val assetManager = context.assets
    val fileDescriptor = assetManager.openFd("$modelName.tflite")
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    val byteFile: MappedByteBuffer =
      fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    val options = Interpreter.Options()
    options.numThreads = 1
    interpreter = Interpreter(byteFile, options)
    interpreter?.allocateTensors()
    return "initialization tflite success $modelName"
  }

  override fun detectFromBase64(options: TensorFaceOptions): Variant_NullType_HybridFaceSpec {
    val runLandmarks = options.runLandmarks ?: false
    val runContours = options.runContours ?: false
    val runClassifications = options.runClassifications ?: false
    val trackingEnabled = options.trackingEnabled ?: false
    val faceDetector = FaceDetection.getClient(
      options.toMLFaceDetectorOptions()
    )
    val decodedString = Base64.decode(options.base64Image, Base64.DEFAULT)
    val bmpStorageResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    val mlImage = InputImage.fromBitmap(bmpStorageResult, 0)
    val config = FaceProcessConfig(
      width = mlImage.height.toDouble(),
      height = mlImage.width.toDouble(),
      scaleX = 1.0,
      scaleY = 1.0,
      runLandmarks,
      runContours,
      runClassifications,
      trackingEnabled
    )
    val task = faceDetector.process(mlImage)
    val faces = Tasks.await(task)
    if (faces.isEmpty()) {
      return Variant_NullType_HybridFaceSpec.create(NullType.NULL)
    }
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
    val output: FloatBuffer = FloatBuffer.allocate(512)
    interpreter?.run(input, output)
    val arrayData: Array<String> = output.array().map { it.toString() }.toTypedArray()
    return Variant_NullType_HybridFaceSpec.create(
      HybridFace(
        face, config,
        base64 = FaceHelper().getBase64Image(bmpFaceStorage),
        data = arrayData,
        message = "Successfully Get Face"
      )
    )
  }
}
