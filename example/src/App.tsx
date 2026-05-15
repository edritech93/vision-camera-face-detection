import { useEffect, useRef } from 'react';
import { Text, View, StyleSheet, useWindowDimensions } from 'react-native';
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
  const faceDetectorOptions = useRef<FaceScannerOptions>({
    performanceMode: 'fast',
    runClassifications: true,
    runContours: true,
    runLandmarks: true,
    windowWidth: width,
    windowHeight: height,
  }).current;
  const device = useCameraDevice('front');

  useEffect(() => {
    if (hasPermission) return;
    requestPermission();
  }, [hasPermission, requestPermission]);

  if (device == null) {
    return (
      <View style={styles.container}>
        <Text>Loading...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Camera
        ref={camera}
        style={StyleSheet.absoluteFill}
        isActive={true}
        device={device}
        orientationSource={'device'}
        onFaceScanned={(faces: Face[]) => {
          console.log(`Detected ${faces.length} face(s)`);
        }}
        onError={(error: Error) => {
          console.error(`Failed to detect faces!`, error);
        }}
        {...faceDetectorOptions}
        autoMode={true}
        cameraFacing={'front'}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'red',
  },
});
