import VisionCameraFaceDetection, {
  type NativeDetectBas64Type,
} from './NativeVisionCameraFaceDetection';

export type DetectBas64Type = NativeDetectBas64Type;

export function multiply(a: number, b: number): number {
  return VisionCameraFaceDetection.multiply(a, b);
}

export function initTensor(modelPath: string, count?: number): string {
  return VisionCameraFaceDetection.initTensor(modelPath, count);
}

export function detectFromBase64(imageString: string): DetectBas64Type {
  return VisionCameraFaceDetection.detectFromBase64(imageString);
}
