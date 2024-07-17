import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'vision-camera-face-detection' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const VisionCameraFaceDetectionModule =
  NativeModules.VisionCameraFaceDetectionModule
    ? NativeModules.VisionCameraFaceDetectionModule
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export function initTensor(modelPath: string, count?: number): Promise<string> {
  return VisionCameraFaceDetectionModule.initTensor(modelPath, count);
}

export function detectFromBase64(
  imageString: string
): Promise<DetectBas64Type> {
  return VisionCameraFaceDetectionModule.detectFromBase64(imageString);
}

export type DetectBas64Type = {
  base64: string;
  data: any;
  message: string;
};
