import { useMemo, useRef } from 'react';
import type { CameraOutput } from 'react-native-vision-camera';
import { createFaceScannerOutput } from '../factory';
import type { FaceScannerOutputOptions } from '../specs/FaceScannerFactory.nitro';

/**
 * Use a Face Scanner {@linkcode CameraOutput}.
 *
 * The Face Scanner {@linkcode CameraOutput} can be
 * attached to a {@linkcode CameraSession} or {@linkcode Camera}
 * component.
 *
 * @example
 * Attach to a `<Camera />` component:
 * ```tsx
 * const device = ...
 * const scannerOutput = useFaceScannerOutput({
 *   onFaceScanned(faces) {
 *     console.log(`Detected ${faces.length} face(s)`)
 *   },
 *   onError(error) {
 *     console.error(`Failed to detect faces!`, error)
 *   }
 * })
 *
 * return (
 *   <Camera
 *     isActive={true}
 *     device={device}
 *     outputs={[scannerOutput]}
 *   />
 * )
 * ```
 * @example
 * Attach to a `CameraSession`:
 * ```ts
 * const device = ...
 * const scannerOutput = useFaceScannerOutput({
 *   onFaceScanned(faces) {
 *     console.log(`Detected ${faces.length} face(s)`)
 *   },
 *   onError(error) {
 *     console.error(`Failed to detect faces!`, error)
 *   }
 * })
 * const camera = useCamera({
 *   isActive: true,
 *   device: device,
 *   outputs: [scannerOutput]
 * })
 * ```
 */
export function useFaceScannerOutput({
  onFaceScanned,
  onError,
  outputResolution = 'preview',
  ...options
}: FaceScannerOutputOptions): CameraOutput {
  const stableOnFaceScanned = useRef(onFaceScanned);
  stableOnFaceScanned.current = onFaceScanned;

  const stableOnError = useRef(onError);
  stableOnError.current = onError;

  return useMemo(
    () =>
      createFaceScannerOutput({
        onFaceScanned(faces) {
          stableOnFaceScanned.current(faces);
        },
        onError(error) {
          stableOnError.current(error);
        },
        outputResolution: outputResolution,
        ...options,
      }),
    [options, outputResolution]
  );
}
