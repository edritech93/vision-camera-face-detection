import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation
import Accelerate
import TensorFlowLite

let batchSize = 1
let inputChannels = 1
let inputWidth = 112
let inputHeight = 112

// TensorFlow Lite `Interpreter` object for performing inference on a given model.
var interpreter: Interpreter? = nil

class FaceHelper {
  
  static func getImageFaceFromUIImage(from image: UIImage, rectImage: CGRect) -> UIImage? {
    let imageRef: CGImage = (image.cgImage?.cropping(to: rectImage)!)!
    let imageCrop: UIImage = UIImage(cgImage: imageRef, scale: 0.5, orientation: image.imageOrientation)
    return imageCrop
  }
  
  static func convertImageToBase64(image: UIImage) -> String {
    let imageData = image.pngData()!
    return imageData.base64EncodedString()
  }
  
  static func rgbDataFromBuffer(_ buffer: CVPixelBuffer) -> Data? {
    let byteCount = batchSize * inputWidth * inputHeight * inputChannels
    CVPixelBufferLockBaseAddress(buffer, .readOnly)
    defer {
      CVPixelBufferUnlockBaseAddress(buffer, .readOnly)
    }
    guard let sourceData = CVPixelBufferGetBaseAddress(buffer) else {
      return nil
    }
    
    let width = CVPixelBufferGetWidth(buffer)
    let height = CVPixelBufferGetHeight(buffer)
    let sourceBytesPerRow = CVPixelBufferGetBytesPerRow(buffer)
    let destinationChannelCount = 3
    let destinationBytesPerRow = destinationChannelCount * width
    
    var sourceBuffer = vImage_Buffer(data: sourceData,
                                     height: vImagePixelCount(height),
                                     width: vImagePixelCount(width),
                                     rowBytes: sourceBytesPerRow)
    
    guard let destinationData = malloc(height * destinationBytesPerRow) else {
      print("Error: out of memory")
      return nil
    }
    
    defer {
      free(destinationData)
    }
    
    var destinationBuffer = vImage_Buffer(data: destinationData,
                                          height: vImagePixelCount(height),
                                          width: vImagePixelCount(width),
                                          rowBytes: destinationBytesPerRow)
    
    let pixelBufferFormat = CVPixelBufferGetPixelFormatType(buffer)
    
    switch (pixelBufferFormat) {
    case kCVPixelFormatType_32BGRA:
      vImageConvert_BGRA8888toRGB888(&sourceBuffer, &destinationBuffer, UInt32(kvImageNoFlags))
    case kCVPixelFormatType_32ARGB:
      vImageConvert_ARGB8888toRGB888(&sourceBuffer, &destinationBuffer, UInt32(kvImageNoFlags))
    case kCVPixelFormatType_32RGBA:
      vImageConvert_RGBA8888toRGB888(&sourceBuffer, &destinationBuffer, UInt32(kvImageNoFlags))
    default:
      // Unknown pixel format.
      return nil
    }
    
    let byteData = Data(bytes: destinationBuffer.data, count: destinationBuffer.rowBytes * height)
    let bytes = Array<UInt8>(unsafeData: byteData)!
    var floats = [Float]()
    for i in 0..<bytes.count {
      floats.append(Float(bytes[i]) / 255.0)
    }
    return Data(copyingBufferOf: floats)
  }
  
  static func uiImageToPixelBuffer(image: UIImage, size: Int) -> CVPixelBuffer? {
    let attrs = [kCVPixelBufferCGImageCompatibilityKey: kCFBooleanTrue, kCVPixelBufferCGBitmapContextCompatibilityKey: kCFBooleanTrue] as CFDictionary
    var pixelBuffer : CVPixelBuffer?
    let status = CVPixelBufferCreate(kCFAllocatorDefault, size, size, kCVPixelFormatType_32ARGB, attrs, &pixelBuffer)
    guard (status == kCVReturnSuccess) else {
      return nil
    }
    
    CVPixelBufferLockBaseAddress(pixelBuffer!, CVPixelBufferLockFlags(rawValue: 0))
    let pixelData = CVPixelBufferGetBaseAddress(pixelBuffer!)
    
    let rgbColorSpace = CGColorSpaceCreateDeviceRGB()
    let context = CGContext(data: pixelData, width: size, height: size, bitsPerComponent: 8, bytesPerRow: CVPixelBufferGetBytesPerRow(pixelBuffer!), space: rgbColorSpace, bitmapInfo: CGImageAlphaInfo.noneSkipFirst.rawValue)
    
    context?.translateBy(x: 0, y: CGFloat(size))
    context?.scaleBy(x: 1.0, y: -1.0)
    
    UIGraphicsPushContext(context!)
    image.draw(in: CGRect(x: 0, y: 0, width: size, height: size))
    UIGraphicsPopContext()
    CVPixelBufferUnlockBaseAddress(pixelBuffer!, CVPixelBufferLockFlags(rawValue: 0))
    return pixelBuffer
  }
  
  static func getImageFaceFromBuffer(from sampleBuffer: CMSampleBuffer, rectImage: CGRect, orientation: UIImage.Orientation) -> CVPixelBuffer? {
    autoreleasepool {
      guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
        print("Error: Failed to get image buffer from sample buffer")
        return nil
      }
      
      let ciimage = CIImage(cvPixelBuffer: imageBuffer)
      let context = CIContext(options: nil)
      
      guard let cgImage = context.createCGImage(ciimage, from: ciimage.extent) else {
        print("Error: Failed to create CGImage from CIImage")
        return nil
      }
      
      // Ensure rectImage is within bounds of the image
      let imageRect = CGRect(x: 0, y: 0, width: cgImage.width, height: cgImage.height)
      let clampedRect = rectImage.intersection(imageRect)
      
      guard !clampedRect.isEmpty, let imageRef = cgImage.cropping(to: clampedRect) else {
        print("Error: Failed to crop image. Face rect: \(rectImage), Image bounds: \(imageRect)")
        return nil
      }
      
      let imageCrop: UIImage = UIImage(cgImage: imageRef, scale: 0.5, orientation: orientation)
      return uiImageToPixelBuffer(image: imageCrop, size: inputWidth)
    }
  }
}

// MARK: - Extensions
extension Data {
  // Creates a new buffer by copying the buffer pointer of the given array.
  init<T>(copyingBufferOf array: [T]) {
    self = array.withUnsafeBufferPointer(Data.init)
  }
}

extension Array {
  // Creates a new array from the bytes of the given unsafe data.
  init?(unsafeData: Data) {
    guard unsafeData.count % MemoryLayout<Element>.stride == 0 else { return nil }
    self = unsafeData.withUnsafeBytes { .init($0.bindMemory(to: Element.self)) }
  }
}
