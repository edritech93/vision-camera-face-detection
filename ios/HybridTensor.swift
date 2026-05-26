import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation
import TensorFlowLite

class HybridTensor: HybridTensorFactorySpec {
  func initTensor() throws -> String {
    guard let modelPath = Bundle.main.path(
      forResource: "mobile_face_net",
      ofType: "tflite"
    ) else {
      throw RuntimeError.error(
        withMessage: "Failed to load the model file"
      )
    }
    do {
      var options = Interpreter.Options()
      options.threadCount = 1
      interpreter = try Interpreter(modelPath: modelPath, options: options)
      try interpreter?.allocateTensors()
      return "initialization tflite success"
    } catch let error {
      throw error
    }
  }
  
  func detectFromBase64(options: TensorFaceOptions)throws -> Variant_NullType__any_HybridFaceSpec_ {
    let orientationManager = FaceDetectorOrientation()
    let runLandmarks = options.runLandmarks ?? false
    let runContours = options.runContours ?? false
    let runClassifications = options.runClassifications ?? false
    let trackingEnabled = options.trackingEnabled ?? false
    let autoMode = options.autoMode ?? false
    let cameraFacing = options.cameraFacing ?? .front
    let faceDetector = FaceDetector.faceDetector(
      options: options.toMLFaceDetectorOptions()
    )
    guard let stringData = Data(base64Encoded: options.base64Image) else {
      throw RuntimeError.error(
        withMessage: "Error base64 encoded"
      )
    }
    guard let uiImage = UIImage(data: stringData) else {
      throw RuntimeError.error(
        withMessage: "UIImage can't created"
      )
    }
    let config = FaceProcessConfig(
      width: uiImage.size.width,
      height: uiImage.size.height,
      scaleX: 1.0,
      scaleY: 1.0,
      runLandmarks: runLandmarks,
      runContours: runContours,
      runClassifications: runClassifications,
      trackingEnabled: trackingEnabled,
      autoMode: autoMode,
      cameraFacing: cameraFacing,
      orientation: orientationManager.orientation
    )
    let mlImage = VisionImage(image: uiImage)
    mlImage.orientation = .up
    let faces = try faceDetector.results(in: mlImage)
    guard let face = faces.first else {
      throw RuntimeError.error(withMessage: "No face detected")
    }
    guard let interpreter = interpreter else {
      throw RuntimeError.error(
        withMessage: "Interpreter is not initialized. Call initTensor() first."
      )
    }
    guard let imageCrop = FaceHelper.getImageFaceFromUIImage(
      from: uiImage,
      rectImage: face.frame
    ) else {
      throw RuntimeError.error(withMessage: "Failed to crop face image")
    }
    guard let pixelBuffer = FaceHelper.uiImageToPixelBuffer(
      image: imageCrop,
      size: inputWidth
    ) else {
      throw RuntimeError.error(withMessage: "Failed to create pixel buffer")
    }
    guard let rgbData = FaceHelper.rgbDataFromBuffer(pixelBuffer) else {
      throw RuntimeError.error(
        withMessage: "Failed to convert the image buffer to RGB data"
      )
    }
    try interpreter.copy(rgbData, toInputAt: 0)
    try interpreter.invoke()

    let outputTensor = try interpreter.output(at: 0)
    let embedding: [Float] = [Float32](unsafeData: outputTensor.data) ?? []
    let data = embedding.map { String($0) }
    let base64 = FaceHelper.convertImageToBase64(image: imageCrop)

    return .second(
      HybridFace(
        face: face,
        config: config,
        base64: base64,
        data: data,
        message: "Successfully Get Face"
      )
    )
  }
}
