import type { HybridObject } from 'react-native-nitro-modules';
import type { DetectBase64 } from './DetectBase64';

export interface Tensor extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  initTensor(modelPath: string, count?: number): string;
  detectFromBase64(imageString: string): DetectBase64;
}
