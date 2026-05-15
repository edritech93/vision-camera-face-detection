import type { HybridObject } from 'react-native-nitro-modules';

/**
 * Result payload returned by {@linkcode TensorFactory.detectFromBase64}.
 *
 * Contains the (optionally annotated) image, raw model output, a status
 * message, and per-face probability scores produced by the detector.
 */
export interface DetectBase64 {
  /**
   * Base64-encoded image bytes returned by the native side
   * (e.g. the input image with detection overlays drawn on it).
   *
   * Does not include a `data:` URI prefix.
   */
  base64: string;
  /**
   * Raw detection output as an array of strings.
   *
   * Each entry typically encodes one detection (label, bounding box,
   * landmarks, ...) in the format produced by the native model.
   */
  data: string[];
  /**
   * Human-readable status or error message from the native detector.
   *
   * Empty (or a success marker) when detection completed normally.
   */
  message: string;
  /**
   * Probability in the range `[0, 1]` that the left eye is open.
   */
  leftEyeOpenProbability: number;
  /**
   * Probability in the range `[0, 1]` that the right eye is open.
   */
  rightEyeOpenProbability: number;
  /**
   * Probability in the range `[0, 1]` that the detected face is smiling.
   */
  smilingProbability: number;
}

export interface TensorFactory extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  /**
   * Initialize the underlying tensor / ML model from the given {@linkcode modelPath}.
   *
   * @param modelPath - Absolute path (or bundled asset path) to the model file.
   * @param count - Optional max number of detections / threads to allocate.
   * @returns A status string returned by the native implementation.
   *
   * @example
   * ```ts
   * import { NitroModules } from 'react-native-nitro-modules'
   * import type { TensorFactory } from 'vision-camera-face-detection'
   *
   * const tensor = NitroModules.createHybridObject<TensorFactory>('TensorFactory')
   * const status = tensor.initTensor('/path/to/model.tflite', 1)
   * console.log('Tensor init status:', status)
   * ```
   */
  initTensor(modelPath: string, count?: number): string;

  /**
   * Run detection on a Base64-encoded image and return the decoded result.
   *
   * @param imageString - Base64-encoded image data (without the `data:` URI prefix).
   * @returns A {@linkcode DetectBase64} payload describing the detection result.
   *
   * @example
   * ```ts
   * const base64 = '<...image bytes as base64...>'
   * const result = tensor.detectFromBase64(base64)
   * console.log('Detection result:', result)
   * ```
   */
  detectFromBase64(imageString: string): DetectBase64;
}
