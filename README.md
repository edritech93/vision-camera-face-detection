# vision-camera-face-detection

Face detection for [React Native Vision Camera](https://github.com/mrousavy/react-native-vision-camera) v5, powered by [Google ML Kit](https://developers.google.com/ml-kit/vision/face-detection) and [Nitro Modules](https://nitro.margelo.com/).

- Real-time face detection from camera output buffers
- Bounds, contours, landmarks, head Euler angles, eye-open and smiling probabilities, and face tracking
- Optional on-device embeddings for face-recognition workflows
- Fully typed Nitro APIs for camera outputs, frame outputs, and Base64 images

## Requirements

- React Native New Architecture
- `react-native-vision-camera` >= 5.2
- `react-native-nitro-modules` ^0.37.1
- iOS 15.5+
- Android `minSdkVersion` 24+

## Installation

```sh
npm install vision-camera-face-detection react-native-nitro-modules react-native-vision-camera
# or
yarn add vision-camera-face-detection react-native-nitro-modules react-native-vision-camera
```

On iOS, install the native pods:

```sh
cd ios && bundle exec pod install
```

### Permissions

Follow the [Vision Camera permissions guide](https://react-native-vision-camera.com/docs/guides) and configure camera access for your app:

- iOS: add `NSCameraUsageDescription` to `Info.plist`.
- Android: add `android.permission.CAMERA` to `AndroidManifest.xml`.

## Live camera detection

Create a face scanner output with `useFaceScannerOutput` and pass it to Vision Camera's `outputs` prop.

```tsx
import { useEffect, useMemo, useRef } from 'react';
import { StyleSheet, useWindowDimensions } from 'react-native';
import {
  Camera,
  useCameraDevice,
  useCameraPermission,
  type CameraRef,
} from 'react-native-vision-camera';
import {
  useFaceScannerOutput,
  type Face,
  type FaceScannerOutputOptions,
} from 'vision-camera-face-detection';

export default function FaceCamera() {
  const camera = useRef<CameraRef>(null);
  const device = useCameraDevice('front');
  const { hasPermission, requestPermission } = useCameraPermission();
  const { width, height } = useWindowDimensions();

  const scannerOptions = useMemo<FaceScannerOutputOptions>(
    () => ({
      performanceMode: 'fast',
      runClassifications: true,
      runContours: false,
      runLandmarks: true,
      trackingEnabled: true,
      autoMode: true,
      cameraFacing: 'front',
      windowWidth: width,
      windowHeight: height,
      outputResolution: 'preview',
      onFaceScanned(faces: Face[]) {
        console.log(`Detected ${faces.length} face(s)`);
      },
      onError(error) {
        console.error('Face detection failed', error);
      },
    }),
    [height, width]
  );

  const faceOutput = useFaceScannerOutput(scannerOptions);

  useEffect(() => {
    if (!hasPermission) {
      requestPermission();
    }
  }, [hasPermission, requestPermission]);

  if (!hasPermission || !device) {
    return null;
  }

  return (
    <Camera
      ref={camera}
      style={StyleSheet.absoluteFill}
      device={device}
      isActive
      orientationSource="device"
      outputs={[faceOutput]}
    />
  );
}
```

Keep the options object stable with `useMemo` or `useRef`. Changing scanner options recreates the native camera output.

## Scanner options

| Option               | Type                   | Default         | Description                                                                                                                                             |
| -------------------- | ---------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `performanceMode`    | `'fast' \| 'accurate'` | `'fast'`        | ML Kit detector performance mode.                                                                                                                       |
| `runLandmarks`       | `boolean`              | `false`         | Detect facial landmarks.                                                                                                                                |
| `runContours`        | `boolean`              | `false`         | Detect contours. Contours are only detected for the most prominent face.                                                                                |
| `runClassifications` | `boolean`              | `false`         | Detect smiling and eye-open probabilities.                                                                                                              |
| `minFaceSize`        | `number`               | `0.15`          | Minimum face width as a fraction of the image width.                                                                                                    |
| `trackingEnabled`    | `boolean`              | `false`         | Assign a `trackingId` to a detected face. Avoid combining this with contours.                                                                           |
| `cameraFacing`       | `'front' \| 'back'`    | `'front'`       | Camera direction used by native mirroring logic.                                                                                                        |
| `autoMode`           | `boolean`              | `false`         | Scale and rotate bounds, contours, and landmarks to screen coordinates natively. Disable it when drawing with frame coordinates, for example with Skia. |
| `windowWidth`        | `number`               | `1.0`           | Screen width used when `autoMode` is enabled.                                                                                                           |
| `windowHeight`       | `number`               | `1.0`           | Screen height used when `autoMode` is enabled.                                                                                                          |
| `tensorModel`        | `TensorModelEnum`      | `MobileFaceNet` | Model configuration used to interpret embeddings.                                                                                                       |
| `outputResolution`   | `'preview' \| 'full'`  | `'preview'`     | Camera buffer resolution. `preview` favors lower latency; `full` uses the highest available resolution.                                                 |

`outputResolution` is available on `FaceScannerOutputOptions`. The callbacks below are also required when creating a camera output:

```ts
onFaceScanned: (faces: Face[]) => void
onError: (error: Error) => void
```

## Face result

```ts
interface Face {
  readonly bounds: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
  readonly landmarks?: Landmarks;
  readonly contours?: Contours;
  readonly leftEyeOpenProbability?: number;
  readonly rightEyeOpenProbability?: number;
  readonly smilingProbability?: number;
  readonly trackingId?: number;
  readonly pitchAngle: number;
  readonly rollAngle: number;
  readonly yawAngle: number;

  // Populated by embedding workflows.
  readonly base64?: string;
  readonly data?: string[];
  readonly message?: string;
}
```

`landmarks` and `contours` contain Vision Camera `Point` values. Their coordinates are native camera coordinates unless `autoMode` is enabled.

## Face embeddings

The embedding API expects TensorFlow Lite models to be included in the host application:

- Android: place `<model-name>.tflite` in `android/app/src/main/assets/`.
- iOS: add `<model-name>.tflite` to the main application target and ensure it is included in **Copy Bundle Resources**.

The library exposes these model configurations:

```ts
enum TensorModelEnum {
  MobileFaceNet = 'mobile_face_net',
  FaceNet512 = 'facenet_512',
}
```

The corresponding models must accept the expected input sizes and emit `192` values for MobileFaceNet or `512` values for FaceNet512. The example model files are available under the [Android assets](example/android/app/src/main/assets) and [iOS app](example/ios/VisionCameraFaceDetectionExample) directories.

Initialize the model once before requesting embeddings:

```ts
import { initTensor, TensorModelEnum } from 'vision-camera-face-detection';

const status = initTensor(TensorModelEnum.MobileFaceNet);
console.log(status);
```

For Base64 image detection:

```ts
import {
  detectFromBase64,
  type TensorFaceOptions,
  TensorModelEnum,
} from 'vision-camera-face-detection';

const options: TensorFaceOptions = {
  base64Image, // Raw Base64 payload, without a data: prefix.
  performanceMode: 'fast',
  tensorModel: TensorModelEnum.MobileFaceNet,
  runClassifications: true,
};

const face = detectFromBase64(options);

if (face?.data) {
  const embedding = face.data.map(Number);
  const croppedFaceBase64 = face.base64;
  // Compare the embedding with stored vectors using L2 or cosine distance.
}
```

`detectFromBase64` returns `null` when no face is detected. Call `initTensor` with the matching model before using `detectFromBase64` or expecting live `face.data` embeddings.

## Lower-level APIs

### Manual scanner output

`createFaceScannerOutput(options)` creates a Vision Camera `CameraOutput` outside React:

```ts
import { createFaceScannerOutput } from 'vision-camera-face-detection';

const output = createFaceScannerOutput({
  performanceMode: 'fast',
  autoMode: true,
  onFaceScanned(faces) {
    console.log(faces);
  },
  onError(error) {
    console.error(error);
  },
});
```

Pass the result to a Vision Camera `Camera` or `CameraSession` as an output.

### Frame processor scanning

`createFaceScanner(options)` and `useFaceScanner(options)` create a scanner with a synchronous `scanFaces(frame)` method:

```ts
import { useFaceScanner } from 'vision-camera-face-detection';
import { useFrameOutput } from 'react-native-vision-camera';

const scanner = useFaceScanner({
  performanceMode: 'fast',
  autoMode: false,
});

const frameOutput = useFrameOutput({
  onFrame(frame) {
    'worklet';
    const faces = scanner.scanFaces(frame);
    console.log(`Detected ${faces.length} face(s)`);
    frame.dispose();
  },
});
```

Use scanner calls according to Vision Camera's frame output/worklet requirements. Keep `autoMode` disabled when converting native frame points for custom overlays.

## Example app

The [example app](example/src/App.tsx) demonstrates live scanning, camera output composition, photo capture, model selection, image enrollment, and embedding distance calculation.

From the repository root:

```sh
yarn
yarn nitrogen
cd example/ios && bundle exec pod install && cd ../..
yarn example ios
# or
yarn example android
```

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob).
