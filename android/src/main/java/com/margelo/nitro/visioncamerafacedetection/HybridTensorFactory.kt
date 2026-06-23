package com.margelo.nitro.visioncamerafacedetection

import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip

@DoNotStrip
@Keep
class HybridTensorFactory : HybridTensorFactorySpec() {

  override fun initTensor(modelName: String): String {
    return HybridTensor().initTensor(modelName)
  }

  override fun detectFromBase64(options: TensorFaceOptions): Variant_NullType_HybridFaceSpec {
    return HybridTensor().detectFromBase64(options)
  }
}
