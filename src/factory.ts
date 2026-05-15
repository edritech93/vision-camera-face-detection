import { NitroModules } from 'react-native-nitro-modules';
import type {
  FaceScannerFactory,
  FaceScannerOptions,
  FaceScannerOutputOptions,
} from './specs/FaceScannerFactory.nitro';
import type { FaceScanner } from './specs/FaceScanner.nitro';
import type { CameraOutput } from 'react-native-vision-camera';
import type { TensorFactory } from './specs/TensorFactory.nitro';

const factoryFace =
  NitroModules.createHybridObject<FaceScannerFactory>('FaceScannerFactory');

export function createFaceScanner(options: FaceScannerOptions): FaceScanner {
  return factoryFace.createFaceScanner(options);
}

export function createFaceScannerOutput(
  options: FaceScannerOutputOptions
): CameraOutput {
  return factoryFace.createFaceScannerOutput(options);
}

const factoryTensor =
  NitroModules.createHybridObject<TensorFactory>('TensorFactory');

export function initTensor(modelPath: string, count?: number): string {
  return factoryTensor.initTensor(modelPath, count);
}

export function detectFromBase64(imageString: string) {
  return factoryTensor.detectFromBase64(imageString);
}
