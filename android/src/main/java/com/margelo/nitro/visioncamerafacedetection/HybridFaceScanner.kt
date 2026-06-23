package com.margelo.nitro.visioncamerafacedetection

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.camera.core.ExperimentalGetImage
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.face.FaceDetection
import com.margelo.nitro.NitroModules
import com.margelo.nitro.camera.HybridFrameSpec
import com.margelo.nitro.visioncamerafacedetection.extensions.FaceHelper
import com.margelo.nitro.visioncamerafacedetection.extensions.TF_OD_API_INPUT_SIZE
import com.margelo.nitro.visioncamerafacedetection.extensions.interpreter
import com.margelo.nitro.visioncamerafacedetection.extensions.toInputImage
import com.margelo.nitro.visioncamerafacedetection.extensions.toMLFaceDetectorOptions
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class HybridFaceScanner(
  options: FaceScannerOptions,
) : HybridFaceScannerSpec() {

  private val context =
    NitroModules.applicationContext ?: throw Error("Face Scanner - No Context available!")
  private val orientationManager = FaceDetectorOrientation.get(context.applicationContext)
  private val runLandmarks = options.runLandmarks ?: false
  private val runContours = options.runContours ?: false
  private val runClassifications = options.runClassifications ?: false
  private val trackingEnabled = options.trackingEnabled ?: false
  private val autoMode = options.autoMode ?: false
  private val cameraFacing: CameraPosition = options.cameraFacing ?: CameraPosition.FRONT
  private val windowWidth = options.windowWidth ?: 1.0
  private val windowHeight = options.windowHeight ?: 1.0
  private val faceDetector = FaceDetection.getClient(
    options.toMLFaceDetectorOptions()
  )

  @OptIn(ExperimentalGetImage::class)
  override fun scanFaces(
    frame: HybridFrameSpec
  ): Array<HybridFaceSpec> {
    val image = frame.toInputImage()
    val width = image.height.toDouble()
    val height = image.width.toDouble()
    val scaleX = if (autoMode) windowWidth / width else 1.0
    val scaleY = if (autoMode) windowHeight / height else 1.0
    val config = FaceProcessConfig(
      width,
      height,
      scaleX,
      scaleY,
      runLandmarks,
      runContours,
      runClassifications,
      trackingEnabled,
      autoMode,
      cameraFacing,
      orientation = orientationManager.orientation
    )
    val task = faceDetector.process(image)
    val faces = Tasks.await(task).map {
      val bmpFrameResult = ImageConvertUtils.getInstance().getUpRightBitmap(image)
      val bmpFaceResult =
        createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE)
      val faceBB = RectF(it.boundingBox)
      val cvFace = Canvas(bmpFaceResult)
      val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
      val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
      val matrix = Matrix()
      matrix.postTranslate(-faceBB.left, -faceBB.top)
      matrix.postScale(sx, sy)
      cvFace.drawBitmap(bmpFrameResult, matrix, null)
      val input: ByteBuffer = FaceHelper().bitmap2ByteBuffer(bmpFaceResult)
      val output: FloatBuffer = FloatBuffer.allocate(512)
      interpreter?.run(input, output)
      val arrayData: Array<String> = output.array().map { it1 -> it1.toString() }.toTypedArray()
      HybridFace(
        it, config,
        base64 = FaceHelper().getBase64Image(bmpFaceResult),
        data = arrayData,
        message = "Successfully Get Face"
      )
    }.toTypedArray<HybridFaceSpec>()

    return faces
  }

  override fun dispose() {
    faceDetector.close()
    orientationManager.stopDeviceOrientationListener()

    super.dispose()
  }
}
