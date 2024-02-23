import { NativeModules, Platform } from 'react-native';
import { VisionCameraProxy, type Frame } from 'react-native-vision-camera';

const plugin = VisionCameraProxy.initFrameProcessorPlugin('scanFace');

export function scanFaces(frame: Frame): FaceType {
  'worklet';
  if (plugin == null) {
    throw new Error(
      'Failed to load "vision-camera-face-detection" Frame Processor Plugin!'
    );
  }
  return plugin.call(frame) as unknown as FaceType;
}

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

export function detectFromBase64(imageString: string): Promise<any> {
  return VisionCameraFaceDetectionModule.detectFromBase64(imageString);
}

export type FaceType = {
  leftEyeOpenProbability: number;
  rollAngle: number;
  pitchAngle: number;
  yawAngle: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
  bounds: FaceBoundType;
  contours: FaceContourType;
  data: number[];
};

export type FaceBoundType = {
  boundingCenterX: number;
  boundingCenterY: number;
  boundingExactCenterX: number;
  boundingExactCenterY: number;
  height: number;
  width: number;
  x: number;
  y: number;
};

export type FaceContourType = {
  FACE: FacePointType[];
  NOSE_BOTTOM: FacePointType[];
  LOWER_LIP_TOP: FacePointType[];
  RIGHT_EYEBROW_BOTTOM: FacePointType[];
  LOWER_LIP_BOTTOM: FacePointType[];
  NOSE_BRIDGE: FacePointType[];
  RIGHT_CHEEK: FacePointType[];
  RIGHT_EYEBROW_TOP: FacePointType[];
  LEFT_EYEBROW_TOP: FacePointType[];
  UPPER_LIP_BOTTOM: FacePointType[];
  LEFT_EYEBROW_BOTTOM: FacePointType[];
  UPPER_LIP_TOP: FacePointType[];
  LEFT_EYE: FacePointType[];
  RIGHT_EYE: FacePointType[];
  LEFT_CHEEK: FacePointType[];
};

export type FacePointType = { x: number; y: number };
