import type { HybridObject } from 'react-native-nitro-modules';
import type { Frame } from 'react-native-vision-camera';
import type { Face } from './Face.nitro';

/**
 * Represents a Face Scanner that uses MLKit Face Detection.
 *
 * The {@linkcode FaceScanner} can be used in a
 * Frame Processor by calling {@linkcode FaceScanner.scanFaces | scanFaces(...)}.
 *
 * @see {@linkcode useFaceScanner | useFaceScanner(...)}
 * @see {@linkcode createFaceScanner | createFaceScanner(...)}
 */
export interface FaceScanner extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  /**
   * @example
   * ```ts
   * const scanner = // ...
   * const frame = // ...
   * const previewView = // ...
   *
   * const faces = scanner.scanFaces(frame)
   * for (const face of faces) {
   *   console.log('Face detected:', face)
   *   for (const point of face.landmarks) {
   *     const cameraPoint = frame.convertFramePointToCameraPoint(point)
   *     const previewPoint = previewView.convertCameraPointToViewPoint(cameraPoint)
   *     console.log('Landmark Point:', previewPoint)
   *   }
   * }
   * ```
   */
  scanFaces(frame: Frame): Face[];
}
