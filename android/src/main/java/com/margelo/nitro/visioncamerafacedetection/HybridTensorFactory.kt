package com.margelo.nitro.visioncamerafacedetection

import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip

@DoNotStrip
@Keep
class HybridTensorFactory : HybridTensorFactorySpec() {
  @DoNotStrip
  @Keep
  override fun initTensor(modelPath: String, count: Double?): String {
    TODO("Not yet implemented")
  }

  @DoNotStrip
  @Keep
  override fun detectFromBase64(imageString: String): DetectBase64 {
    TODO("Not yet implemented")
  }
}
