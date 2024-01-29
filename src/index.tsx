import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'vision-camera-face-detection' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const VisionCameraFaceDetection = NativeModules.VisionCameraFaceDetection
  ? NativeModules.VisionCameraFaceDetection
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return VisionCameraFaceDetection.multiply(a, b);
}
