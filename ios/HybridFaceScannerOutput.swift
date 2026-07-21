import AVFoundation
import CoreImage
import MLKitFaceDetection
import MLKitVision
import NitroModules
import UIKit
import VisionCamera

class HybridFaceScannerOutput:
  HybridCameraOutputSpec,
  NativeCameraOutput {
  
  private let queue: DispatchQueue
  private let onFacesDetected: (_ faces: [any HybridFaceSpec]) -> Void
  private let onError: (_ error: Error) -> Void
  private let autoMode: Bool
  private let windowWidth: Double
  private let windowHeight: Double
  private let runLandmarks: Bool
  private let runContours: Bool
  private let runClassifications: Bool
  private let trackingEnabled: Bool
  private let cameraFacing: CameraPosition
  private var delegate: FaceScannerDelegate? = nil
  private var isBusy = false
  let output: AVCaptureVideoDataOutput
  let requiresAudioInput: Bool = false
  let requiresDepthFormat: Bool = false
  let mediaType: MediaType = .video
  let streamType: StreamType = .video
  var outputOrientation: CameraOrientation = .up
  var targetResolution: ResolutionRule {
    return .closestTo(
      Size(width: 720.0, height: 1280.0)
    )
  }
  var currentResolution: Size? = Size(width: 720.0, height: 1280.0)
  private let orientationManager = FaceDetectorOrientation()
  private let faceDetector: FaceDetector
  
  init(options: FaceScannerOutputOptions) {
    self.queue = DispatchQueue(label: "FaceScannerQueue")
    self.output = AVCaptureVideoDataOutput()
    self.onFacesDetected = options.onFaceScanned
    self.onError = options.onError
    self.autoMode = options.autoMode ?? false
    self.windowWidth = options.windowWidth ?? 1.0
    self.windowHeight = options.windowHeight ?? 1.0
    self.runLandmarks = options.runLandmarks ?? false
    self.runContours = options.runContours ?? false
    self.runClassifications = options.runClassifications ?? false
    self.trackingEnabled = options.trackingEnabled ?? false
    self.cameraFacing = options.cameraFacing ?? .front
    self.faceDetector = FaceDetector.faceDetector(
      options: options.toMLFaceDetectorOptions()
    )
    super.init()
    self.delegate = FaceScannerDelegate(onSampleBuffer: { [weak self] buffer in
      self?.scanFaces(buffer)
    })
    self.output.setSampleBufferDelegate(delegate, queue: queue)
    self.output.alwaysDiscardsLateVideoFrames = true
    if #available(iOS 17.0, *), options.outputResolution != .full {
      self.output.automaticallyConfiguresOutputBufferDimensions = false
      self.output.deliversPreviewSizedOutputBuffers = true
    }
  }
  
  private func scanFaces(_ buffer: CMSampleBuffer)  {
    if isBusy { return }
    isBusy = true
    guard let image = MLImage(sampleBuffer: buffer) else {
      isBusy = false
      onError(RuntimeError.error(
        withMessage: "Failed to convert CMSampleBuffer to MLImage!"
      ))
      return
    }
    image.orientation = outputOrientation.toUIImageOrientation(
      orientation: orientationManager.orientation,
      cameraFacing: cameraFacing
    )
    let width = image.height
    let height = image.width
    let config = FaceProcessConfig(
      width: width,
      height: height,
      scaleX: autoMode ? windowWidth / width : 1.0,
      scaleY: autoMode ? windowHeight / height : 1.0,
      runLandmarks: runLandmarks,
      runContours: runContours,
      runClassifications: runClassifications,
      trackingEnabled: trackingEnabled,
      autoMode: autoMode,
      cameraFacing: cameraFacing,
      orientation: orientationManager.orientation
    )
    self.faceDetector.process(image) { [weak self] faces, error in
      guard let self else { return }
      self.isBusy = false
      if let error {
        self.onError(error)
        return
      }
      guard let faces, !faces.isEmpty else {
        return
      }
      let hybridFaces: [any HybridFaceSpec] = faces.compactMap { face -> HybridFace? in
        guard let interpreter = interpreter else {
          self.onError(RuntimeError.error(
            withMessage: "Interpreter is not initialized. Call initTensor() first."
          ))
          return nil
        }
        guard let imageCrop = FaceHelper.getImageFaceFromBuffer(
          from: buffer,
          rectImage: face.frame,
          orientation: image.orientation
        ) else {
          self.onError(RuntimeError.error(
            withMessage: "Failed to crop face image from buffer"
          ))
          return nil
        }
        guard let rgbData = FaceHelper.rgbDataFromBuffer(imageCrop) else {
          self.onError(RuntimeError.error(
            withMessage: "Failed to convert the image buffer to RGB data"
          ))
          return nil
        }
        do {
          try interpreter.copy(rgbData, toInputAt: 0)
          try interpreter.invoke()
          let outputTensor = try interpreter.output(at: 0)
          let embedding: [Float] = [Float32](unsafeData: outputTensor.data) ?? []
          let data = embedding.map { String($0) }
          var base64: String? = nil
          let ciImage = CIImage(cvPixelBuffer: imageCrop)
          if let cgImage = CIContext(options: nil).createCGImage(ciImage, from: ciImage.extent) {
            base64 = FaceHelper.convertImageToBase64(
              image: UIImage(cgImage: cgImage, scale: 1.0, orientation: image.orientation)
            )
          }
          return HybridFace(
            face: face,
            config: config,
            base64: base64 ?? "",
            data: data,
            message: "Successfully Get Face"
          )
        } catch {
          self.onError(error)
          return nil
        }
      }
      if !hybridFaces.isEmpty {
        self.onFacesDetected(hybridFaces)
      }
    }
  }
  
  func configure(config: OutputConfiguration) {
    guard let connection = self.output.connection(with: .video) else {
      return
    }
    connection.preferredVideoStabilizationMode = .off
  }
}
