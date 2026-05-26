import NitroModules
import VisionCamera

class HybridTensorFactory: HybridTensorFactorySpec {
  func initTensor() throws -> String {
    return HybridTensor().initTensor()
  }
  
  func detectFromBase64(options: TensorFaceOptions) throws -> Variant_NullType__any_HybridFaceSpec_ {
    return HybridTensor().detectFromBase64(options)
  }
}
