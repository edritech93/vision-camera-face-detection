import NitroModules
import VisionCamera

class HybridTensorFactory: HybridTensorFactorySpec {
  func initTensor() throws -> String {
    return try HybridTensor().initTensor()
  }
  
  func detectFromBase64(options: TensorFaceOptions) throws -> Variant_NullType__any_HybridFaceSpec_ {
    return try HybridTensor().detectFromBase64(options: options)
  }
}
