import NitroModules
import VisionCamera

class HybridFaceScannerFactory: HybridFaceScannerFactorySpec {
  func createFaceScanner(options: FaceScannerOptions) throws -> any HybridFaceScannerSpec {
    return HybridFaceScanner(options: options)
  }
  
  func createFaceScannerOutput(options: FaceScannerOutputOptions) throws
  -> any HybridCameraOutputSpec
  {
    return HybridFaceScannerOutput(options: options)
  }
}
