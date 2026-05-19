import type { HybridObject } from 'react-native-nitro-modules';
import type { FaceScannerOptions } from './FaceScannerFactory.nitro';
import type { Face } from './Face.nitro';

/**
 * Options for running face detection on a Base64-encoded image via
 * {@linkcode TensorFactory}.
 *
 * Extends {@linkcode FaceScannerOptions} with the image payload itself,
 * so callers can configure detector behavior (performance mode, landmarks,
 * contours, classifications, ...) alongside the input image.
 */
export interface TensorFaceOptions extends FaceScannerOptions {
  /**
   * Base64-encoded image bytes to run detection on.
   *
   * Must not include a `data:` URI prefix (e.g. `data:image/jpeg;base64,`);
   * provide only the raw Base64 payload.
   */
  base64Image: string;
}

export interface TensorFactory extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  /**
   * Initialize the underlying tensor / ML model.
   *
   * @returns A status string returned by the native implementation.
   *
   * @example
   * ```ts
   * import { NitroModules } from 'react-native-nitro-modules'
   * import type { TensorFactory } from 'vision-camera-face-detection'
   *
   * const tensor = NitroModules.createHybridObject<TensorFactory>('TensorFactory')
   * const status = tensor.initTensor()
   * console.log('Tensor init status:', status)
   * ```
   */
  initTensor(): string;

  /**
   * Run detection on a Base64-encoded image and return the decoded result.
   *
   * @param options - Options for running face detection, including the Base64-encoded image data.
   * @returns An array of {@linkcode Face} objects describing the detection result.
   *
   * @example
   * ```ts
   * const base64 = '<...image bytes as base64...>'
   * const result = tensor.detectFromBase64({ base64Image: base64 })
   * console.log('Detection result:', result)
   * ```
   */
  detectFromBase64(options: TensorFaceOptions): Face | null;
}
