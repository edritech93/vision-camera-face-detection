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
    // static func processContours(from face: Face) -> [String:[[String:CGFloat]]] {
    //     let faceContoursTypes = [
    //         FaceContourType.face,
    //         FaceContourType.leftEyebrowTop,
    //         FaceContourType.leftEyebrowBottom,
    //         FaceContourType.rightEyebrowTop,
    //         FaceContourType.rightEyebrowBottom,
    //         FaceContourType.leftEye,
    //         FaceContourType.rightEye,
    //         FaceContourType.upperLipTop,
    //         FaceContourType.upperLipBottom,
    //         FaceContourType.lowerLipTop,
    //         FaceContourType.lowerLipBottom,
    //         FaceContourType.noseBridge,
    //         FaceContourType.noseBottom,
    //         FaceContourType.leftCheek,
    //         FaceContourType.rightCheek,
    //     ]
    
    //     let faceContoursTypesStrings = [
    //         "FACE",
    //         "LEFT_EYEBROW_TOP",
    //         "LEFT_EYEBROW_BOTTOM",
    //         "RIGHT_EYEBROW_TOP",
    //         "RIGHT_EYEBROW_BOTTOM",
    //         "LEFT_EYE",
    //         "RIGHT_EYE",
    //         "UPPER_LIP_TOP",
    //         "UPPER_LIP_BOTTOM",
    //         "LOWER_LIP_TOP",
    //         "LOWER_LIP_BOTTOM",
    //         "NOSE_BRIDGE",
    //         "NOSE_BOTTOM",
    //         "LEFT_CHEEK",
    //         "RIGHT_CHEEK",
    //     ];
    
    //     var faceContoursTypesMap: [String:[[String:CGFloat]]] = [:]
    
    //     for i in 0..<faceContoursTypes.count {
    //         let contour = face.contour(ofType: faceContoursTypes[i]);
    
    //         var pointsArray: [[String:CGFloat]] = []
    
    //         if let points = contour?.points {
    //             for point in points {
    //                 let currentPointsMap = [
    //                     "x": point.x,
    //                     "y": point.y,
    //                 ]
    
    //                 pointsArray.append(currentPointsMap)
    //             }
    
    //             faceContoursTypesMap[faceContoursTypesStrings[i]] = pointsArray
    //         }
    //     }
    
    //     return faceContoursTypesMap
    // }
    
    // static func processBoundingBox(from face: Face) -> [String:Any] {
    //     let frameRect = face.frame
    
    //     let offsetX = (frameRect.midX - ceil(frameRect.width)) / 2.0
    //     let offsetY = (frameRect.midY - ceil(frameRect.height)) / 2.0
    
    //     let x = frameRect.maxX + offsetX
    //     let y = frameRect.minY + offsetY
    
    //     return [
    //         "x": frameRect.midX + (frameRect.midX - x),
    //         "y": frameRect.midY + (y - frameRect.midY),
    //         "width": frameRect.width,
    //         "height": frameRect.height,
    //         "boundingCenterX": frameRect.midX,
    //         "boundingCenterY": frameRect.midY
    //     ]
    // }
    
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
    
    static func getImageFaceFromBuffer(from sampleBuffer: CMSampleBuffer?, rectImage: CGRect, orientation: UIImage.Orientation) -> UIImage? {
        guard let sampleBuffer = sampleBuffer else {
            print("Sample buffer is NULL.")
            return nil
        }
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
            print("Invalid sample buffer.")
            return nil
        }
        let ciimage = CIImage(cvPixelBuffer: imageBuffer)
        let context = CIContext(options: nil)
        let cgImage = context.createCGImage(ciimage, from: ciimage.extent)!
        
        if (!rectImage.isNull) {
            let imageRef: CGImage = cgImage.cropping(to: rectImage)!
            let imageCrop: UIImage = UIImage(cgImage: imageRef, scale: 0.5, orientation: orientation)
            return imageCrop
        } else {
            return nil
        }
    }
    
    //    static func getDataFromPixel(pixelBuffer: CVPixelBuffer) -> Data {
    //        CVPixelBufferLockBaseAddress(pixelBuffer, [.readOnly])
    //        defer { CVPixelBufferUnlockBaseAddress(pixelBuffer, [.readOnly]) }
    //
    //        // Calculate sum of planes' size
    //        var totalSize = 0
    //        for plane in 0 ..< CVPixelBufferGetPlaneCount(pixelBuffer) {
    //            let height      = CVPixelBufferGetHeightOfPlane(pixelBuffer, plane)
    //            let bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, plane)
    //            let planeSize   = height * bytesPerRow
    //            totalSize += planeSize
    //        }
    //
    //        guard let rawFrame = malloc(totalSize) else { fatalError() }
    //        var dest = rawFrame
    //
    //        for plane in 0 ..< CVPixelBufferGetPlaneCount(pixelBuffer) {
    //            let source      = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, plane)
    //            let height      = CVPixelBufferGetHeightOfPlane(pixelBuffer, plane)
    //            let bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, plane)
    //            let planeSize   = height * bytesPerRow
    //
    //            memcpy(dest, source, planeSize)
    //            dest += planeSize
    //        }
    //
    //        return Data(bytesNoCopy: rawFrame, count: totalSize, deallocator: .free)
    //    }
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
