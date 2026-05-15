import { NitroModules } from 'react-native-nitro-modules';
import type {
  FaceScannerFactory,
  FaceScannerOptions,
  FaceScannerOutputOptions,
} from './specs/FaceScannerFactory.nitro';
import type { FaceScanner } from './specs/FaceScanner.nitro';
import type { CameraOutput } from 'react-native-vision-camera';

const factory =
  NitroModules.createHybridObject<FaceScannerFactory>('FaceScannerFactory');

export function createFaceScanner(options: FaceScannerOptions): FaceScanner {
  return factory.createFaceScanner(options);
}

export function createFaceScannerOutput(
  options: FaceScannerOutputOptions
): CameraOutput {
  return factory.createFaceScannerOutput(options);
}
