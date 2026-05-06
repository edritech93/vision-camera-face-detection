package com.margelo.nitro.visioncamerafacedetection

import androidx.camera.core.ExperimentalGetImage
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.gms.tasks.Tasks
import com.margelo.nitro.camera.HybridFrameSpec
import com.margelo.nitro.camera.MirrorMode
import com.margelo.nitro.camera.public.NativeCameraOutput

@DoNotStrip
class VisionCameraFaceDetection() : HybridVisionCameraFaceDetectionSpec() {


  @OptIn(ExperimentalGetImage::class)
  override fun scanFace(frame: HybridFrameSpec): Array<HybridBarcodeSpec> {
    val image = frame.toInputImage()
    val task = scanner.process(image)
    val barcodes = Tasks.await(task)
    return barcodes
      .map { HybridBarcode(it) }
      .toTypedArray()
  }

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }
}
