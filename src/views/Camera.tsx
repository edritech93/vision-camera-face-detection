import type { RefObject } from 'react';
import { Camera as VisionCamera } from 'react-native-vision-camera';
import type { CameraViewProps, CameraRef } from 'react-native-vision-camera';
import { useFaceScannerOutput } from '../hooks/useFaceScannerOutput';
import type { FaceScannerOutputOptions } from '../specs/FaceScannerFactory.nitro';

interface ComponentType
  extends Omit<CameraViewProps, 'onError'>, FaceScannerOutputOptions {
  ref?: RefObject<CameraRef | null>;
}

/**
 * A view that detects {@linkcode Face}s in a Camera
 * using the default front {@linkcode CameraDevice}.
 *
 *
 * @example
 * ```tsx
 * function App() {
 *   const isFocused = useIsFocused()
 *   const appState = useAppState()
 *   const isActive = isFocused && appState === 'active'
 *   return (
 *     <Camera
 *       isActive={isActive}
 *       barcodeFormats={['all']}
 *       onFaceScanned={(faces) => {
 *         console.log(`Detected ${faces.length} faces!`)
 *       }}
 *       onError={(error) => {
 *         console.error(`Error detecting faces:`, error)
 *       }}
 *     />
 *   )
 * }
 * ```
 */
export function Camera({
  onFaceScanned,
  onError,
  outputResolution,
  cameraFacing,
  autoMode,
  windowWidth,
  windowHeight,
  performanceMode,
  runLandmarks,
  runContours,
  runClassifications,
  minFaceSize,
  trackingEnabled,
  ...cameraProps
}: ComponentType) {
  const output = useFaceScannerOutput({
    onFaceScanned,
    onError,
    outputResolution,
    cameraFacing,
    autoMode,
    windowWidth,
    windowHeight,
    performanceMode,
    runLandmarks,
    runContours,
    runClassifications,
    minFaceSize,
    trackingEnabled,
  });

  return <VisionCamera {...cameraProps} outputs={[output]} onError={onError} />;
}

export default Camera;
