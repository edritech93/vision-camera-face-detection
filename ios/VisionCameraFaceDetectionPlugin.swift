import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation

@objc(VisionCameraFaceDetectionPlugin)
public class VisionCameraFaceDetectionPlugin: FrameProcessorPlugin {
    public override init(proxy: VisionCameraProxyHolder, options: [AnyHashable: Any]! = [:]) {
        super.init(proxy: proxy, options: options)
    }

    static var FaceDetectorOption: FaceDetectorOptions = {
        let option = FaceDetectorOptions()
        option.contourMode = .none
        option.classificationMode = .all
        option.landmarkMode = .none
        option.performanceMode = .fast // doesn't work in fast mode!, why?
        return option
    }()
    
    static var faceDetector = FaceDetector.faceDetector(options: FaceDetectorOption)
    
    
    
    @objc override public func callback(_ frame: Frame, withArguments arguments: [AnyHashable : Any]?) -> Any? {
        let image = VisionImage(buffer: frame.buffer)
        image.orientation = .up
        do {
            let faces: [Face] =  try VisionCameraFaceDetectionPlugin.faceDetector.results(in: image)
            if (!faces.isEmpty){
                guard let face = faces.first else {
                    return nil
                }
                var map: [String: Any] = [:]
                map["rollAngle"] = face.headEulerAngleZ  // Head is tilted sideways rotZ degrees
                map["pitchAngle"] = face.headEulerAngleX  // Head is rotated to the uptoward rotX degrees
                map["yawAngle"] = face.headEulerAngleY   // Head is rotated to the right rotY degrees
                map["leftEyeOpenProbability"] = face.leftEyeOpenProbability
                map["rightEyeOpenProbability"] = face.rightEyeOpenProbability
                map["smilingProbability"] = face.smilingProbability
                map["bounds"] = FaceHelper.processBoundingBox(from: face)
                map["contours"] = FaceHelper.processContours(from: face)
                return map
            } else {
                return nil
            }
        } catch {
            print("error: \(error)")
            return nil
        }
    }
}
