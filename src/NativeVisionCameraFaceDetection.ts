import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initTensor(modelPath: string, count?: number): string;
  detectFromBase64(imageString: string): NativeDetectBas64Type;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'VisionCameraFaceDetection'
);

export type NativeDetectBas64Type = {
  base64: string;
  data: string[] | number[];
  message: string;
};
