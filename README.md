# vision-camera-face-detection

Face Detection plugin for [react-native-vision-camera](https://github.com/mrousavy/react-native-vision-camera) v5, powered by [Google ML Kit](https://developers.google.com/ml-kit/vision/face-detection) and [Nitro Modules](https://nitro.margelo.com/).

- Real-time face detection on the camera stream
- Bounds, contours, landmarks, head Euler angles, eye-open / smiling probabilities, face tracking
- Optional on-device tensor embedding extraction (`initTensor` / `detectFromBase64`) for face recognition pipelines
- Built on Nitro Modules — zero-bridge, synchronous, fully typed

## Requirements

- `react-native` >= 0.85
- `react-native-vision-camera` >= 5
- `react-native-nitro-modules` >= 0.35.6
- iOS 13+ / Android `minSdkVersion` 24+

## Installation

```sh
npm install vision-camera-face-detection react-native-nitro-modules react-native-vision-camera
# or
yarn add vision-camera-face-detection react-native-nitro-modules react-native-vision-camera
```

iOS:

```sh
cd ios && bundle exec pod install
```

> `react-native-nitro-modules` is required because this library is built on [Nitro Modules](https://nitro.margelo.com/).

### Permissions

Follow the [Vision Camera permissions guide](https://react-native-vision-camera.com/docs/guides) to add `NSCameraUsageDescription` (iOS) and `android.permission.CAMERA` (Android) to your app.

## Usage

### Drop-in `Camera` component

The package ships a `Camera` view that wraps `react-native-vision-camera` and attaches the face scanner output for you.

```tsx
import { useEffect, useRef } from 'react';
import { StyleSheet, useWindowDimensions } from 'react-native';
import {
  useCameraDevice,
  useCameraPermission,
  type CameraRef,
} from 'react-native-vision-camera';
import {
  Camera,
  type Face,
  type FaceScannerOptions,
} from 'vision-camera-face-detection';

export default function App() {
  const { hasPermission, requestPermission } = useCameraPermission();
  const { width, height } = useWindowDimensions();
  const camera = useRef<CameraRef>(null);
  const device = useCameraDevice('front');

  const options: FaceScannerOptions = {
    performanceMode: 'fast',
    runClassifications: true,
    runContours: true,
    runLandmarks: true,
    windowWidth: width,
    windowHeight: height,
  };

  useEffect(() => {
    if (!hasPermission) requestPermission();
  }, [hasPermission, requestPermission]);

  if (!hasPermission || !device) return null;

  return (
    <Camera
      ref={camera}
      style={StyleSheet.absoluteFill}
      device={device}
      isActive
      orientationSource="device"
      cameraFacing="front"
      autoMode
      {...options}
      onFaceScanned={(faces: Face[]) => {
        console.log(`Detected ${faces.length} face(s)`);
      }}
      onError={(error) => console.error('Face detection failed', error)}
    />
  );
}
```

### Scanner options

`FaceScannerOptions` (passed as props to `<Camera />` or to `createFaceScanner`):

| Option               | Type                   | Default     | Description                                                                                                                |
| -------------------- | ---------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------- |
| `performanceMode`    | `'fast' \| 'accurate'` | `'fast'`    | ML Kit detector performance mode.                                                                                          |
| `runLandmarks`       | `boolean`              | `false`     | Detect facial landmarks (eyes, nose, mouth, ears).                                                                         |
| `runContours`        | `boolean`              | `false`     | Detect face contours.                                                                                                      |
| `runClassifications` | `boolean`              | `false`     | Detect smile and eye-open probabilities.                                                                                   |
| `minFaceSize`        | `number`               | `0.15`      | Minimum face size as a fraction of the image.                                                                              |
| `trackingEnabled`    | `boolean`              | `false`     | Assign and track a `trackingId` per face.                                                                                  |
| `cameraFacing`       | `'front' \| 'back'`    | `'front'`   | Active camera. Used for mirroring math in `autoMode`.                                                                      |
| `autoMode`           | `boolean`              | `false`     | Scale & rotate bounds/contours/landmarks to screen coordinates natively. Disable when drawing with a Skia Frame Processor. |
| `windowWidth`        | `number`               | `1.0`       | Required when `autoMode` is enabled.                                                                                       |
| `windowHeight`       | `number`               | `1.0`       | Required when `autoMode` is enabled.                                                                                       |
| `outputResolution`   | `'preview' \| 'full'`  | `'preview'` | Camera buffer resolution to feed the detector.                                                                             |

### `Face` result

```ts
interface Face {
  readonly bounds: { x: number; y: number; width: number; height: number };
  readonly landmarks?: Landmarks;
  readonly contours?: Contours;
  readonly leftEyeOpenProbability?: number;
  readonly rightEyeOpenProbability?: number;
  readonly smilingProbability?: number;
  readonly trackingId?: number;
  readonly pitchAngle: number;
  readonly rollAngle: number;
  readonly yawAngle: number;
  readonly base64?: string; // populated when tensor embedding is computed
  readonly data?: string[]; // tensor embedding vector
  readonly message?: string;
}
```

### Tensor / face embedding API

For face-recognition workflows you can run detection on a single Base64-encoded image and extract a tensor embedding.

```ts
import {
  initTensor,
  detectFromBase64,
  type TensorFaceOptions,
} from 'vision-camera-face-detection';

// Once during app startup (after camera permission is granted)
const status = initTensor();
console.log('Tensor init:', status);

const options: TensorFaceOptions = {
  base64Image: '<raw base64 without data: prefix>',
  performanceMode: 'fast',
  runClassifications: true,
  runContours: true,
  runLandmarks: true,
  windowWidth,
  windowHeight,
};

const face = detectFromBase64(options);
if (face?.data) {
  const embedding = face.data.map((v) => parseFloat(parseFloat(v).toFixed(5)));
  // compare against a stored embedding using L2 / cosine distance
}
```

### Lower-level factories & hooks

If you prefer to wire the output manually:

```ts
import {
  createFaceScanner,
  createFaceScannerOutput,
  useFaceScanner,
  useFaceScannerOutput,
} from 'vision-camera-face-detection';
```

- `createFaceScanner(options)` — returns a Nitro `FaceScanner` hybrid object.
- `createFaceScannerOutput(options)` — returns a `CameraOutput` you can pass to `<VisionCamera outputs={[output]} />`.
- `useFaceScanner` / `useFaceScannerOutput` — React hook variants used internally by the `Camera` component.

## Example app

A full example (including image-picker based enrollment and live distance computation) lives in [example/src/App.tsx](example/src/App.tsx).

```sh
yarn
yarn example pods
yarn example ios   # or: yarn example android
```

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
