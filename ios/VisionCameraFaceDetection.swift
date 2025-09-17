import AVFoundation
import CoreML
import MLKitFaceDetection
import MLKitVision
import TensorFlowLite
import UIKit
import VisionCamera

class VisionCameraFaceDetection: HybridVisionCameraFaceDetectionSpec {
  static var FaceDetectorOption: FaceDetectorOptions = {
    let option = FaceDetectorOptions()
    option.performanceMode = .accurate
    return option
  }()

  static var faceDetector = FaceDetector.faceDetector(
    options: FaceDetectorOption
  )

  public func initTensor(modelPath: String, count: Double?) throws -> String {
    // Construct the path to the model file.
    guard
      let modelPath = Bundle.main.path(
        forResource: modelName,
        ofType: "tflite"
      )
    else {
      print("Failed to load the model file with name: \(modelName).")
      return
    }
    do {
      var options = Interpreter.Options()
      options.threadCount = count
      interpreter = try Interpreter(modelPath: modelPath, options: options)
      try interpreter?.allocateTensors()
      return "initialization tflite success"
    } catch let error {
      print(
        "Failed to create the interpreter with error: \(error.localizedDescription)"
      )
      return
        "Failed to create the interpreter with error: \(error.localizedDescription)"
    }
  }

  public func detectFromBase64(imageString: String) throws -> String {
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
      let faces: [Face] = try VisionCameraFaceDetectionModule.faceDetector
        .results(in: image)
      if !faces.isEmpty {
        let face = faces.first
        let faceFrame = face!.frame
        guard
          let imageCrop = FaceHelper.getImageFaceFromUIImage(
            from: uiImage,
            rectImage: faceFrame
          )
        else {
          reject("imageCrop can't created", nil, nil)
          return
        }
        guard
          let pixelBuffer = FaceHelper.uiImageToPixelBuffer(
            image: imageCrop,
            size: inputWidth
          )
        else {
          reject("Failed to get pixelBuffer", nil, nil)
          return
        }
        guard let rgbData = FaceHelper.rgbDataFromBuffer(pixelBuffer) else {
          reject("Failed to convert the image buffer to RGB data.", nil, nil)
          return
        }
        try interpreter?.copy(rgbData, toInputAt: 0)
        try interpreter?.invoke()

        let outputTensor: Tensor? = try interpreter?.output(at: 0)
        if (outputTensor?.data) != nil {
          let result: [Float] = [Float32](unsafeData: outputTensor!.data) ?? []
          map["message"] = "Successfully Get Face"
          map["data"] = result
          map["base64"] = FaceHelper.convertImageToBase64(image: imageCrop)
          map["leftEyeOpenProbability"] = face?.leftEyeOpenProbability ?? 0.0
          map["rightEyeOpenProbability"] = face?.rightEyeOpenProbability ?? 0.0
          map["smilingProbability"] = face?.smilingProbability ?? 0.0
          return map
        } else {
          map["message"] = "No Face"
          map["data"] = []
          map["base64"] = ""
          map["leftEyeOpenProbability"] = 0.0
          map["rightEyeOpenProbability"] = 0.0
          map["smilingProbability"] = 0.0
          return map
        }
      } else {
        map["message"] = "No Face"
        map["data"] = []
        map["base64"] = ""
        map["leftEyeOpenProbability"] = 0.0
        map["rightEyeOpenProbability"] = 0.0
        map["smilingProbability"] = 0.0
        return map
      }
    } catch error {
      return error
    }
  }
}
