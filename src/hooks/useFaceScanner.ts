import { useMemo } from 'react';
import type { FaceScannerOptions } from '../specs/FaceScannerFactory.nitro';
import type { FaceScanner } from '../specs/FaceScanner.nitro';
import { createFaceScanner } from '../factory';

/**
 * @example
 * ```ts
 * const {scanFaces} = useFaceScanner({})
 * const frameOutput = useFrameOutput({
 *   onFrame(frame) {
 *     'worklet'
 *     const result = scanFaces(frame)
 *     console.log(`Detected ${result.length} faces!`)
 *     frame.dispose()
 *   }
 * })
 * ```
 */
export function useFaceScanner(options: FaceScannerOptions): FaceScanner {
  return useMemo(() => createFaceScanner(options), [options]);
}
