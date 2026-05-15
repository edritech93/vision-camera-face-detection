package com.margelo.nitro.visioncamerafacedetection

import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.camera.HybridCameraOutputSpec

@DoNotStrip
@Keep
class HybridFaceScannerFactory : HybridFaceScannerFactorySpec() {
  @DoNotStrip
  @Keep
  override fun createFaceScanner(options: FaceScannerOptions): HybridFaceScannerSpec {
    return HybridFaceScanner(options)
  }

  @DoNotStrip
  @Keep
  override fun createFaceScannerOutput(options: FaceScannerOutputOptions): HybridCameraOutputSpec {
    return HybridFaceScannerOutput(options)
  }
}
