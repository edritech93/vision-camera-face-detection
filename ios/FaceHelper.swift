import VisionCamera
import MLKitFaceDetection
import MLKitVision
import CoreML
import UIKit
import AVFoundation

class FaceHelper {
    static func processContours(from face: Face) -> [String:[[String:CGFloat]]] {
        let faceContoursTypes = [
            FaceContourType.face,
            FaceContourType.leftEyebrowTop,
            FaceContourType.leftEyebrowBottom,
            FaceContourType.rightEyebrowTop,
            FaceContourType.rightEyebrowBottom,
            FaceContourType.leftEye,
            FaceContourType.rightEye,
            FaceContourType.upperLipTop,
            FaceContourType.upperLipBottom,
            FaceContourType.lowerLipTop,
            FaceContourType.lowerLipBottom,
            FaceContourType.noseBridge,
            FaceContourType.noseBottom,
            FaceContourType.leftCheek,
            FaceContourType.rightCheek,
        ]
        
        let faceContoursTypesStrings = [
            "FACE",
            "LEFT_EYEBROW_TOP",
            "LEFT_EYEBROW_BOTTOM",
            "RIGHT_EYEBROW_TOP",
            "RIGHT_EYEBROW_BOTTOM",
            "LEFT_EYE",
            "RIGHT_EYE",
            "UPPER_LIP_TOP",
            "UPPER_LIP_BOTTOM",
            "LOWER_LIP_TOP",
            "LOWER_LIP_BOTTOM",
            "NOSE_BRIDGE",
            "NOSE_BOTTOM",
            "LEFT_CHEEK",
            "RIGHT_CHEEK",
        ];
        
        var faceContoursTypesMap: [String:[[String:CGFloat]]] = [:]
        
        for i in 0..<faceContoursTypes.count {
            let contour = face.contour(ofType: faceContoursTypes[i]);
            
            var pointsArray: [[String:CGFloat]] = []
            
            if let points = contour?.points {
                for point in points {
                    let currentPointsMap = [
                        "x": point.x,
                        "y": point.y,
                    ]
                    
                    pointsArray.append(currentPointsMap)
                }
                
                faceContoursTypesMap[faceContoursTypesStrings[i]] = pointsArray
            }
        }
        
        return faceContoursTypesMap
    }
    
    static func processBoundingBox(from face: Face) -> [String:Any] {
        let frameRect = face.frame
        
        let offsetX = (frameRect.midX - ceil(frameRect.width)) / 2.0
        let offsetY = (frameRect.midY - ceil(frameRect.height)) / 2.0
        
        let x = frameRect.maxX + offsetX
        let y = frameRect.minY + offsetY
        
        return [
            "x": frameRect.midX + (frameRect.midX - x),
            "y": frameRect.midY + (y - frameRect.midY),
            "width": frameRect.width,
            "height": frameRect.height,
            "boundingCenterX": frameRect.midX,
            "boundingCenterY": frameRect.midY
        ]
    }
}
