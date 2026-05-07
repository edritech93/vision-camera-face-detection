import { NitroModules } from 'react-native-nitro-modules';
import type { DetectBase64Type, Tensor } from './specs/Tensor.nitro';

const TensorHybridObject = NitroModules.createHybridObject<Tensor>('Tensor');

export function initTensor(modelPath: string, count?: number): Promise<string> {
  return TensorHybridObject.initTensor(modelPath, count);
}

export function detectFromBase64(
  imageString: string
): Promise<DetectBase64Type> {
  return TensorHybridObject.detectFromBase64(imageString);
}
