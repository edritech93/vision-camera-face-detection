import Foundation
import NitroModules
import react_native_vision_camera

class VisionCameraFaceDetection: HybridVisionCameraFaceDetectionSpec {
    func call(frame: HybridFrameSpec) throws -> String {
        print("VisionCameraFaceDetection: call(frame:)")
        return "call native hybrid"
    }
}
