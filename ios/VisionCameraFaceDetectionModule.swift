import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation

@objc(VisionCameraFaceDetectionModule)
class VisionCameraFaceDetectionModule: NSObject {
    
    static var FaceDetectorOption: FaceDetectorOptions = {
        let option = FaceDetectorOptions()
        option.performanceMode = .accurate
        return option
    }()
    
    static var faceDetector = FaceDetector.faceDetector(options: FaceDetectorOption)
    
    @objc(detectFromBase64:withResolver:withRejecter:)
    func detectFromBase64(imageString: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let stringData = Data(base64Encoded: imageString) ?? nil
        let uiImage = UIImage(data: stringData!)
        if (uiImage != nil) {
            let image = VisionImage(image: uiImage!)
            do {
                let faces: [Face] =  try VisionCameraFaceDetectionPlugin.faceDetector.results(in: image)
                if (!faces.isEmpty){
                    guard let face = faces.first else {
                        resolve(nil)
                        return
                    }
                    let faceFrame = face.frame
                    let imageCrop = FaceHelper.getImageFaceFromUIImage(from: uiImage!, rectImage: faceFrame)
                    resolve(FaceHelper.convertImageToBase64(image:imageCrop!))
                } else {
                    resolve(nil)
                }
            } catch {
                reject("Error", error.localizedDescription, error)
            }
        } else {
            resolve(nil)
        }
    }
}
