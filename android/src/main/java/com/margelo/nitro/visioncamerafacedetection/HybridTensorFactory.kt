package com.margelo.nitro.visioncamerafacedetection

import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.NitroModules
import com.margelo.nitro.visioncamerafacedetection.extensions.interpreter
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@DoNotStrip
@Keep
class HybridTensorFactory : HybridTensorFactorySpec() {

  private val context =
    NitroModules.applicationContext ?: throw Error("Face Tensor - No Context available!")
//  private val orientationManager = FaceDetectorOrientation.get(context.applicationContext)
//  private val runLandmarks = options.runLandmarks ?: false
//  private val runContours = options.runContours ?: false
//  private val runClassifications = options.runClassifications ?: false
//  private val trackingEnabled = options.trackingEnabled ?: false
//  private val autoMode = options.autoMode ?: false
//  private val cameraFacing: CameraPosition = options.cameraFacing ?: CameraPosition.FRONT
//  private val windowWidth = options.windowWidth ?: 1.0
//  private val windowHeight = options.windowHeight ?: 1.0
//  private val faceDetector = FaceDetection.getClient(
//    options.toMLFaceDetectorOptions()
//  )

  @DoNotStrip
  @Keep
  override fun initTensor(modelPath: String, count: Double?): String {
    // TODO("Not yet implemented")
    val assetManager = context.assets
    val fileDescriptor = assetManager.openFd("$modelPath.tflite")
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    val byteFile: MappedByteBuffer =
      fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    val options = Interpreter.Options()
    count?.let { options.numThreads = it.toInt() }
    interpreter = Interpreter(byteFile, options)
    interpreter?.allocateTensors()
    return "initialization tflite success"
  }

  @DoNotStrip
  @Keep
  override fun detectFromBase64(imageString: String): DetectBase64 {
    TODO("Not yet implemented")
  }
}
