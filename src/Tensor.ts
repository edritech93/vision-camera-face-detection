import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'vision-camera-face-detection' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const VisionCameraFaceDetectorModule =
  NativeModules.VisionCameraFaceDetectorModule
    ? NativeModules.VisionCameraFaceDetectorModule
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export function initTensor(modelPath: string, count?: number): Promise<string> {
  return VisionCameraFaceDetectorModule.initTensor(modelPath, count);
}

export function detectFromBase64(imageString: string): Promise<any> {
  return VisionCameraFaceDetectorModule.detectFromBase64(imageString);
}
