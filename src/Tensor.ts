import { NitroModules } from 'react-native-nitro-modules';
import type { VisionCameraFaceDetection } from './VisionCameraFaceDetection.nitro';

const VisionCameraFaceDetectionHybridObject =
  NitroModules.createHybridObject<VisionCameraFaceDetection>(
    'VisionCameraFaceDetection'
  );

export function initTensor(modelPath: string, count?: number): string {
  return VisionCameraFaceDetectionHybridObject.initTensor(modelPath, count);
}

export function detectFromBase64(imageString: string): DetectBas64Type {
  return VisionCameraFaceDetectionHybridObject.detectFromBase64(imageString);
}

export type DetectBas64Type = {
  base64: string;
  data: number[];
  message: string;
  leftEyeOpenProbability: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
};
