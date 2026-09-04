package com.margelo.nitro.visioncamerafacedetection

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.face.FaceDetection
import com.margelo.nitro.NitroModules
import com.margelo.nitro.camera.CameraOrientation
import com.margelo.nitro.camera.HybridCameraOutputSpec
import com.margelo.nitro.camera.MediaType
import com.margelo.nitro.camera.MirrorMode
import com.margelo.nitro.camera.extensions.surfaceRotation
import com.margelo.nitro.camera.public.NativeCameraOutput
import com.margelo.nitro.visioncamerafacedetection.extensions.FaceHelper
import com.margelo.nitro.visioncamerafacedetection.extensions.TF_OD_API_INPUT_SIZE
import com.margelo.nitro.visioncamerafacedetection.extensions.interpreter
import com.margelo.nitro.visioncamerafacedetection.extensions.toMLFaceDetectorOptions
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class HybridFaceScannerOutput(
  private val options: FaceScannerOutputOptions,
) : HybridCameraOutputSpec(),
  ImageAnalysis.Analyzer,
  NativeCameraOutput {
  override val mediaType: MediaType = MediaType.VIDEO
  override val mirrorMode: MirrorMode = MirrorMode.AUTO
  override var outputOrientation: CameraOrientation = CameraOrientation.UP
    set(value) {
      field = value
      imageAnalysis?.targetRotation = value.surfaceRotation
    }
  override val currentResolution: com.margelo.nitro.camera.Size?
    get() = com.margelo.nitro.camera.Size(1280.0, 720.0)
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
  private val faceHelper = FaceHelper()
  private var isBusy = AtomicBoolean(false)
  private val executor = Executors.newSingleThreadExecutor()
  private var imageAnalysis: ImageAnalysis? = null
  private val recommendedResolutionForBarcodeScanning = Size(1280, 720)
  private val embeddingMinIntervalMs = 120L
  private var lastEmbeddingAtMs = 0L
  private var lastEmbeddingData: Array<String> = emptyArray()

  override fun createUseCase(
    mirrorMode: MirrorMode,
    config: NativeCameraOutput.Config,
  ): NativeCameraOutput.PreparedUseCase {
    val resolutionStrategy =
      if (options.outputResolution == FaceDetectorOutputResolution.FULL) {
        ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY
      } else {
        ResolutionStrategy(
          recommendedResolutionForBarcodeScanning,
          ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
        )
      }
    val resolutionSelector =
      ResolutionSelector
        .Builder()
        .setResolutionStrategy(resolutionStrategy)
        .setAllowedResolutionMode(
          if (options.outputResolution == FaceDetectorOutputResolution.FULL) {
            ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE
          } else {
            ResolutionSelector.PREFER_CAPTURE_RATE_OVER_HIGHER_RESOLUTION
          }
        )
        .build()
    val imageAnalysis =
      ImageAnalysis
        .Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .setOutputImageRotationEnabled(false)
        .setResolutionSelector(resolutionSelector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    return NativeCameraOutput.PreparedUseCase(imageAnalysis, {
      this.imageAnalysis = imageAnalysis
      imageAnalysis.setAnalyzer(executor, this)
    })
  }

  override fun dispose() {
    orientationManager.stopDeviceOrientationListener()
    faceDetector.close()
    executor.close()
  }

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    try {
      if (!isBusy.compareAndSet(false, true)) {
        // pipeline is busy. close image & return
        imageProxy.close()
        return
      }

      val mediaImage = imageProxy.image
      if (mediaImage == null) {
        // media image is null - error & return.
        imageProxy.close()
        isBusy.set(false)
        options.onError(Error("`ImageProxy` does not have an `Image`!"))
        return
      }
      val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
      val width = inputImage.height.toDouble()
      val height = inputImage.width.toDouble()
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
      faceDetector
        .process(inputImage)
        .addOnSuccessListener { faces ->
          if (faces.isEmpty()) {
            lastEmbeddingData = emptyArray()
            options.onFaceScanned(emptyArray())
            return@addOnSuccessListener
          }
          val nowMs = System.currentTimeMillis()
          val shouldRefreshEmbedding =
            nowMs - lastEmbeddingAtMs >= embeddingMinIntervalMs || lastEmbeddingData.isEmpty()
          if (shouldRefreshEmbedding) {
          val bmpFrameResult = ImageConvertUtils.getInstance().getUpRightBitmap(inputImage)
          val bmpFaceResult =
            createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE)
          val faceBB = RectF(faces[0].boundingBox)
          val cvFace = Canvas(bmpFaceResult)
          val sx = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.width()
          val sy = TF_OD_API_INPUT_SIZE.toFloat() / faceBB.height()
          val matrix = Matrix()
          matrix.postTranslate(-faceBB.left, -faceBB.top)
          matrix.postScale(sx, sy)
          cvFace.drawBitmap(bmpFrameResult, matrix, null)
          val input: ByteBuffer = faceHelper.bitmap2ByteBuffer(bmpFaceResult)
          val output: FloatBuffer = FloatBuffer.allocate(512)
          interpreter?.run(input, output)
          lastEmbeddingData = output.array().map { value -> value.toString() }.toTypedArray()
            lastEmbeddingAtMs = nowMs
          }
          val hybridFaces =
            faces
              .map {
                HybridFace(
                  it, config,
                  base64 = "",
                  data = lastEmbeddingData,
                  message = "Successfully Get Face"
                )
              }
              .toTypedArray<HybridFaceSpec>()
          options.onFaceScanned(hybridFaces)
        }.addOnFailureListener { error ->
          options.onError(error)
        }.addOnCompleteListener {
          imageProxy.close()
          isBusy.set(false)
        }
    } catch (error: Throwable) {
      imageProxy.close()
      isBusy.set(false)
      options.onError(error)
    }
  }
}
