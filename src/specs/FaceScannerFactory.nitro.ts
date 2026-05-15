import type { HybridObject } from 'react-native-nitro-modules';
import type { FaceScanner } from './FaceScanner.nitro';
import type { CameraOutput, CameraPosition } from 'react-native-vision-camera';
import type { ImageFaceDetectorOptions } from './ImageFaceDetectorOptions';
import type { Face } from './Face.nitro';

export interface FaceScannerOptions extends ImageFaceDetectorOptions {
  /**
   * Current active camera
   *
   * @default front
   */
  cameraFacing?: CameraPosition;

  /**
   * Should auto scale (face bounds, contour and landmarks) and rotation on native side?
   * This option must be disabled/undefined if you want to draw on frame using `Skia Frame Processor`.
   * See [this](https://github.com/luicfrr/react-native-vision-camera-face-detector/issues/30#issuecomment-2058805546) and [this](https://github.com/luicfrr/react-native-vision-camera-face-detector/issues/35) for more details.
   *
   * @default false
   */
  autoMode?: boolean;

  /**
   * Required if you want to use `autoMode`. You must handle your own logic to get screen sizes, with or without statusbar size, etc...
   *
   * @default 1.0
   */
  windowWidth?: number;

  /**
   * Required if you want to use `autoMode`. You must handle your own logic to get screen sizes, with or without statusbar size, etc...
   *
   * @default 1.0
   */
  windowHeight?: number;
}

/**
 * Controls the camera buffer resolution used for barcode scanning.
 *
 * - `'preview'`: Prefer preview-sized buffers for lower latency.
 * - `'full'`: Prefer full/highest available buffers for better detail.
 */
export type FaceDetectorOutputResolution = 'preview' | 'full';

export interface FaceScannerOutputOptions extends FaceScannerOptions {
  /**
   * Controls which camera buffer resolution should be used.
   *
   * - `'preview'`: Prefer preview-sized buffers for lower latency.
   * - `'full'`: Prefer full/highest available buffers for better detail.
   *
   * @default 'preview'
   */
  outputResolution?: FaceDetectorOutputResolution;
  /**
   * Called whenever faces have been detected.
   */
  onFaceScanned: (faces: Face[]) => void;
  /**
   * Called when there was an error detecting faces.
   */
  onError: (error: Error) => void;
}

export interface FaceScannerFactory extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  /**
   * Create a new {@linkcode FaceScanner} with the given {@linkcode options}.
   *
   * @example
   * ```ts
   * import { createFaceScanner } from 'vision-camera-face-detection'
   * import { useFrameProcessor } from 'react-native-vision-camera'
   *
   * const scanner = createFaceScanner({})
   *
   * const frameProcessor = useFrameProcessor((frame) => {
   *   'worklet'
   *   const faces = scanner.scanFaces(frame)
   *   console.log(`Detected ${faces.length} face(s)`)
   * }, [scanner])
   * ```
   */
  createFaceScanner(options: FaceScannerOptions): FaceScanner;

  /**
   * Create a new {@linkcode CameraOutput} that can
   * detect Face.
   */
  createFaceScannerOutput(options: FaceScannerOutputOptions): CameraOutput;
}
