import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation
import TensorFlowLite

@objc(VisionCameraFaceDetectionModule)
class VisionCameraFaceDetectionModule: NSObject {
    
    static var FaceDetectorOption: FaceDetectorOptions = {
        let option = FaceDetectorOptions()
        option.performanceMode = .accurate
        return option
    }()
    
    static var faceDetector = FaceDetector.faceDetector(options: FaceDetectorOption)

    @objc(initTensor:withCount:withResolver:withRejecter:)
    func initTensor(modelName: String, count: Int = 1, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        // Construct the path to the model file.
        guard let modelPath = Bundle.main.path(
            forResource: modelName,
            ofType: "tflite"
        ) else {
            print("Failed to load the model file with name: \(modelName).")
            return
        }
        do {
            var options = Interpreter.Options()
            options.threadCount = count
            interpreter = try Interpreter(modelPath: modelPath, options: options)
            try interpreter?.allocateTensors()
            resolve("initialization tflite success")
        } catch let error {
            print("Failed to create the interpreter with error: \(error.localizedDescription)")
            reject("Error", "tflite error", error)
            return
        }
    }
    
    @objc(detectFromBase64:withResolver:withRejecter:)
    func detectFromBase64(imageString: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let stringData = Data(base64Encoded: imageString) else {
            print("Error base64 encoded")
            return
        }
        guard let uiImage = UIImage(data: stringData) else {
            print("UIImage can't created")
            return
        }
        let image = VisionImage(image: uiImage)
        image.orientation = .up
        do {
            var map: [String: Any] = [:]
            let faces: [Face] =  try VisionCameraFaceDetectionPlugin.faceDetector.results(in: image)
            if (!faces.isEmpty){
                let face = faces.first
                let faceFrame = face!.frame
                guard let imageCrop = FaceHelper.getImageFaceFromUIImage(from: uiImage, rectImage: faceFrame) else {
                    reject("imageCrop can't created", nil, nil)
                    return
                }
                guard let pixelBuffer = FaceHelper.uiImageToPixelBuffer(image: imageCrop, size: inputWidth) else {
                    reject("Failed to get pixelBuffer", nil, nil)
                    return
                }
                guard let rgbData = FaceHelper.rgbDataFromBuffer(
                    pixelBuffer,
                    byteCount: batchSize * inputWidth * inputHeight * inputChannels,
                    isModelQuantized: false
                ) else {
                    reject("Failed to convert the image buffer to RGB data.", nil, nil)
                    return
                }
                map["message"] = "Successfully Get Face"
                map["data"] = rgbData.self
                resolve(map)
            } else {
                map["message"] = "No Face"
                map["data"] = []
                resolve(map)
            }
        } catch {
            reject("error: ", nil, error)
        }
    }
}
