import { NitroModules } from 'react-native-nitro-modules';
import type {
  DetectBas64Type,
  VisionCameraFaceDetection,
} from './VisionCameraFaceDetection.nitro';

const VisionCameraFaceDetectionHybridObject =
  NitroModules.createHybridObject<VisionCameraFaceDetection>(
    'VisionCameraFaceDetection'
  );

export function initTensor(modelPath: string, count?: number): Promise<string> {
  return VisionCameraFaceDetectionHybridObject.initTensor(modelPath, count);
}

export function detectFromBase64(
  imageString: string
): Promise<DetectBas64Type> {
  return VisionCameraFaceDetectionHybridObject.detectFromBase64(imageString);
}
