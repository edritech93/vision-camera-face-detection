import NitroModules
import VisionCamera

class HybridTensorFactory: HybridTensorFactorySpec {
  func initTensor(modelName: String) throws -> String {
    return try HybridTensor().initTensor(modelName: modelName)
  }
  
  func detectFromBase64(options: TensorFaceOptions) throws -> Variant__any_HybridFaceSpec__NullType {
    return try HybridTensor().detectFromBase64(options: options)
  }
}
