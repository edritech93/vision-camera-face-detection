import { NitroModules } from 'react-native-nitro-modules';
import type { Tensor } from './specs/Tensor.nitro';
import type { DetectBase64 } from './specs/DetectBase64';

const TensorHybridObject = NitroModules.createHybridObject<Tensor>('Tensor');

export function initTensor(modelPath: string, count?: number): Promise<string> {
  return TensorHybridObject.initTensor(modelPath, count);
}

export function detectFromBase64(imageString: string): Promise<DetectBase64> {
  return TensorHybridObject.detectFromBase64(imageString);
}
