import NitroModules
import VisionCamera

final class DummyHybridFace: HybridFaceSpec {
  let bounds: Bounds = Bounds(width: 100, height: 100, x: 10, y: 20)
  let landmarks: Landmarks? = nil
  let contours: Contours? = nil
  let leftEyeOpenProbability: Double? = 0.9
  let rightEyeOpenProbability: Double? = 0.95
  let smilingProbability: Double? = 0.8
  let trackingId: Double? = 1
  let pitchAngle: Double = 0
  let rollAngle: Double = 0
  let yawAngle: Double = 0
  let base64: String? = "dummy-base64"
  let data: [String]? = ["dummy"]
  let message: String? = "dummy face data"

  override init() {
    super.init()
  }
}

class HybridTensorFactory: HybridTensorFactorySpec {
  func initTensor() throws -> String {
    return "dummy-tensor-initialized"
  }

  func detectFromBase64(options: TensorFaceOptions) throws -> Variant_NullType__any_HybridFaceSpec_ {
    return .second(DummyHybridFace())
  }
}
