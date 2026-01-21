# vision-camera-face-detection

A high-performance Face Detection plugin for [React Native Vision Camera](https://github.com/mrousavy/react-native-vision-camera) V4. Powered by Google ML Kit Face Detection for real-time face detection with landmarks, contours, classification, and TensorFlow Lite support for face recognition.

[![npm version](https://img.shields.io/npm/v/vision-camera-face-detection.svg)](https://www.npmjs.com/package/vision-camera-face-detection)
[![license](https://img.shields.io/npm/l/vision-camera-face-detection.svg)](https://github.com/edritech93/vision-camera-face-detection/blob/main/LICENSE)

## Features

- 🚀 Real-time face detection using Google ML Kit
- 📍 Face landmarks detection (eyes, ears, nose, cheeks, mouth)
- 🎯 Face contours detection
- 😊 Face classification (smiling, eyes open probability)
- 📐 Face angles (pitch, roll, yaw)
- 🔄 Face tracking across frames
- 🧠 TensorFlow Lite integration for face recognition/embedding
- ⚡ Optimized async processing without blocking camera preview

## Requirements

- React Native >= 0.83
- Node.js >= 20
- react-native-vision-camera >= 4.6
- react-native-worklets-core >= 1.5
- iOS 15.5+
- Android minSdkVersion 24+

## Installation

```sh
npm install vision-camera-face-detection
# or
yarn add vision-camera-face-detection
```

### iOS

Add the following to your `Podfile`:

```ruby
pod 'GoogleMLKit/FaceDetection', '~> 6.0.0'
```

Then run:

```sh
cd ios && pod install
```

For TensorFlow Lite support, add the TFLite model file (e.g., `mobile_face_net.tflite`) to your iOS project.

### Android

No additional setup required. The library automatically links the required ML Kit dependencies.

For TensorFlow Lite support, place your model file in the `assets` folder.

## Usage

### Basic Face Detection

```tsx
import React, { useRef } from 'react';
import { StyleSheet, View } from 'react-native';
import {
  useCameraDevice,
  useCameraPermission,
} from 'react-native-vision-camera';
import {
  Camera,
  type Face,
  type FaceDetectionOptions,
} from 'vision-camera-face-detection';

export default function App() {
  const { hasPermission, requestPermission } = useCameraPermission();
  const device = useCameraDevice('front');

  const faceDetectionOptions: FaceDetectionOptions = {
    performanceMode: 'fast',
    classificationMode: 'all',
    landmarkMode: 'all',
    contourMode: 'none',
  };

  const handleFacesDetected = (faces: Face[], frame: Frame) => {
    console.log('Detected faces:', faces.length);

    if (faces.length > 0) {
      const face = faces[0];
      console.log('Face bounds:', face.bounds);
      console.log('Smiling probability:', face.smilingProbability);
      console.log('Left eye open:', face.leftEyeOpenProbability);
      console.log('Right eye open:', face.rightEyeOpenProbability);
    }
  };

  if (!hasPermission) {
    requestPermission();
    return null;
  }

  if (!device) return null;

  return (
    <View style={styles.container}>
      <Camera
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={true}
        faceDetectionOptions={faceDetectionOptions}
        faceDetectionCallback={handleFacesDetected}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
```

### Using the `useFaceDetector` Hook

For more control, you can use the `useFaceDetector` hook directly with a custom frame processor:

```tsx
import { useFrameProcessor } from 'react-native-vision-camera';
import {
  useFaceDetector,
  type FaceDetectionOptions,
} from 'vision-camera-face-detection';

const faceDetectionOptions: FaceDetectionOptions = {
  performanceMode: 'accurate',
  landmarkMode: 'all',
  contourMode: 'all',
  classificationMode: 'all',
};

const { detectFaces } = useFaceDetector(faceDetectionOptions);

const frameProcessor = useFrameProcessor(
  (frame) => {
    'worklet';
    const faces = detectFaces(frame);
    console.log('Faces:', faces);
  },
  [detectFaces]
);
```

### TensorFlow Lite Integration

For face recognition/embedding using TensorFlow Lite:

```tsx
import {
  initTensor,
  detectFromBase64,
  type DetectBas64Type,
} from 'vision-camera-face-detection';

// Initialize TensorFlow Lite model
await initTensor('mobile_face_net', 1);

// Detect face from base64 image and get embedding
const result: DetectBas64Type = await detectFromBase64(base64Image);
console.log('Face embedding:', result.data);
console.log('Cropped face base64:', result.base64);
```

## API Reference

### `<Camera />` Component

A wrapper around Vision Camera that includes face detection.

| Prop                    | Type                                    | Description                      |
| ----------------------- | --------------------------------------- | -------------------------------- |
| `faceDetectionOptions`  | `FaceDetectionOptions`                  | Configuration for face detection |
| `faceDetectionCallback` | `(faces: Face[], frame: Frame) => void` | Callback when faces are detected |
| `...props`              | `CameraProps`                           | All Vision Camera props          |

### `FaceDetectionOptions`

| Option               | Type                   | Default   | Description                              |
| -------------------- | ---------------------- | --------- | ---------------------------------------- |
| `performanceMode`    | `'fast' \| 'accurate'` | `'fast'`  | Favor speed or accuracy                  |
| `landmarkMode`       | `'none' \| 'all'`      | `'none'`  | Detect facial landmarks                  |
| `contourMode`        | `'none' \| 'all'`      | `'none'`  | Detect face contours                     |
| `classificationMode` | `'none' \| 'all'`      | `'none'`  | Classify faces (smiling, eyes open)      |
| `minFaceSize`        | `number`               | `0.15`    | Minimum face size ratio                  |
| `trackingEnabled`    | `boolean`              | `false`   | Enable face tracking across frames       |
| `autoMode`           | `boolean`              | `false`   | Auto scale bounds for screen coordinates |
| `windowWidth`        | `number`               | `1.0`     | Screen width for auto scaling            |
| `windowHeight`       | `number`               | `1.0`     | Screen height for auto scaling           |
| `cameraFacing`       | `'front' \| 'back'`    | `'front'` | Current camera position                  |
| `enableTensor`       | `boolean`              | `false`   | Enable TensorFlow Lite processing        |

### `Face` Interface

```typescript
interface Face {
  pitchAngle: number; // Head rotation around X-axis
  rollAngle: number; // Head rotation around Z-axis
  yawAngle: number; // Head rotation around Y-axis
  bounds: Bounds; // Face bounding box
  leftEyeOpenProbability: number; // 0.0 to 1.0
  rightEyeOpenProbability: number; // 0.0 to 1.0
  smilingProbability: number; // 0.0 to 1.0
  contours?: Contours; // Face contour points
  landmarks?: Landmarks; // Face landmark points
  data: number[]; // Face embedding (when TensorFlow enabled)
}

interface Bounds {
  x: number;
  y: number;
  width: number;
  height: number;
}
```

### `Landmarks` Interface

```typescript
interface Landmarks {
  LEFT_CHEEK: Point;
  LEFT_EAR: Point;
  LEFT_EYE: Point;
  MOUTH_BOTTOM: Point;
  MOUTH_LEFT: Point;
  MOUTH_RIGHT: Point;
  NOSE_BASE: Point;
  RIGHT_CHEEK: Point;
  RIGHT_EAR: Point;
  RIGHT_EYE: Point;
}
```

### `Contours` Interface

```typescript
interface Contours {
  FACE: Point[];
  LEFT_EYEBROW_TOP: Point[];
  LEFT_EYEBROW_BOTTOM: Point[];
  RIGHT_EYEBROW_TOP: Point[];
  RIGHT_EYEBROW_BOTTOM: Point[];
  LEFT_EYE: Point[];
  RIGHT_EYE: Point[];
  UPPER_LIP_TOP: Point[];
  UPPER_LIP_BOTTOM: Point[];
  LOWER_LIP_TOP: Point[];
  LOWER_LIP_BOTTOM: Point[];
  NOSE_BRIDGE: Point[];
  NOSE_BOTTOM: Point[];
  LEFT_CHEEK: Point[];
  RIGHT_CHEEK: Point[];
}
```

### TensorFlow Lite Functions

#### `initTensor(modelPath: string, count?: number): Promise<string>`

Initialize TensorFlow Lite model for face recognition.

- `modelPath`: Name of the TFLite model file (without extension)
- `count`: Number of threads (optional)

#### `detectFromBase64(imageString: string): Promise<DetectBas64Type>`

Detect face from base64 image and return face embedding.

```typescript
type DetectBas64Type = {
  base64: string; // Cropped face image
  data: number[]; // Face embedding array
  message: string; // Status message
  leftEyeOpenProbability: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
};
```

## Example: Face Comparison

```tsx
// Calculate Euclidean distance between two face embeddings
function compareFaces(embedding1: number[], embedding2: number[]): number {
  let distance = 0.0;
  for (let i = 0; i < embedding1.length; i++) {
    const diff = embedding1[i] - embedding2[i];
    distance += diff * diff;
  }
  return distance; // Lower = more similar
}

// Usage
const distance = compareFaces(knownFaceEmbedding, detectedFaceEmbedding);
const isSamePerson = distance < 1.0; // Threshold may vary
```

## Troubleshooting

### iOS Build Issues

If you encounter build issues on iOS, ensure you have:

1. Run `pod install` after adding the package
2. Added the required permissions in `Info.plist`:
   ```xml
   <key>NSCameraUsageDescription</key>
   <string>Camera access is required for face detection</string>
   ```

### Android Build Issues

If you encounter build issues on Android:

1. Ensure `minSdkVersion` is at least 24
2. Ensure `compileSdkVersion` and `targetSdkVersion` are at least 36
3. Enable `multiDexEnabled` if needed

### Performance Tips

- Use `performanceMode: 'fast'` for real-time applications
- Disable `contourMode` and `landmarkMode` if not needed
- Use `minFaceSize` to filter small faces
- Disable `trackingEnabled` when using `contourMode`

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with ❤️ by [Yudi Edri Alviska](https://github.com/edritech93)
