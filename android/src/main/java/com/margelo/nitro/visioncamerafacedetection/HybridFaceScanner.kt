package com.margelo.nitro.visioncamerafacedetection

import androidx.camera.core.ExperimentalGetImage
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.face.FaceDetection
import com.margelo.nitro.NitroModules
import com.margelo.nitro.camera.HybridFrameSpec
import com.margelo.nitro.visioncamerafacedetection.extensions.toInputImage
import com.margelo.nitro.visioncamerafacedetection.extensions.toMLFaceDetectorOptions

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
      HybridFace(
        it, config, base64 = "",
        data = emptyArray(),
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
