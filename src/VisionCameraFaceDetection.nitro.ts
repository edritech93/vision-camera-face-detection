import type { HybridObject } from 'react-native-nitro-modules';
// import type { DetectBas64Type } from './Tensor';

export interface VisionCameraFaceDetection
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  initTensor(modelPath: string, count?: number): string;
  detectFromBase64(imageString: string): string;
}
