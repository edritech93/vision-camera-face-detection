import type { HybridObject } from 'react-native-nitro-modules';

export interface VisionCameraFaceDetection extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  initTensor(modelPath: string, count?: number): Promise<string>;
  detectFromBase64(imageString: string): Promise<DetectBas64Type>;
}

export type DetectBas64Type = {
  base64: string;
  data: any;
  message: string;
  leftEyeOpenProbability: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
};
