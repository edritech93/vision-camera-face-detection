import type { DetectBas64Type } from './VisionCameraFaceDetection.nitro';

export function initTensor(_: string, __?: number): Promise<string> {
  throw new Error(
    "'vision-camera-face-detection' is only supported on native platforms."
  );
}

export function detectFromBase64(_: string): Promise<DetectBas64Type> {
  throw new Error(
    "'vision-camera-face-detection' is only supported on native platforms."
  );
}
