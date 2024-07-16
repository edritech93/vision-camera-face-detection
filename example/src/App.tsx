import { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, Button, View } from 'react-native';
import {
  type Frame,
  Camera as VisionCamera,
  useCameraDevice,
  useCameraPermission,
} from 'react-native-vision-camera';
import {
  Camera,
  type Face,
  type FaceDetectionOptions,
  initTensor,
} from 'vision-camera-face-detection';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

export default function App() {
  const { hasPermission, requestPermission } = useCameraPermission();
  const [cameraMounted, setCameraMounted] = useState<boolean>(false);
  const [cameraPaused, setCameraPaused] = useState<boolean>(false);
  const [autoScale, setAutoScale] = useState<boolean>(true);
  const [facingFront, setFacingFront] = useState<boolean>(true);
  const faceDetectionOptions = useRef<FaceDetectionOptions>({
    performanceMode: 'fast',
    classificationMode: 'all',
  }).current;
  const cameraDevice = useCameraDevice(facingFront ? 'front' : 'back');
  //
  // vision camera ref
  //
  const camera = useRef<VisionCamera>(null);
  //
  // face rectangle position
  //
  const aFaceW = useSharedValue(0);
  const aFaceH = useSharedValue(0);
  const aFaceX = useSharedValue(0);
  const aFaceY = useSharedValue(0);
  const aRot = useSharedValue(0);
  const animatedStyle = useAnimatedStyle(() => ({
    position: 'absolute',
    borderWidth: 4,
    borderLeftColor: 'rgb(0,255,0)',
    borderRightColor: 'rgb(0,255,0)',
    borderBottomColor: 'rgb(0,255,0)',
    borderTopColor: 'rgb(255,0,0)',
    width: withTiming(aFaceW.value, {
      duration: 100,
    }),
    height: withTiming(aFaceH.value, {
      duration: 100,
    }),
    left: withTiming(aFaceX.value, {
      duration: 100,
    }),
    top: withTiming(aFaceY.value, {
      duration: 100,
    }),
    transform: [
      {
        rotate: `${aRot.value}deg`,
      },
    ],
  }));

  useEffect(() => {
    if (hasPermission) return;
    requestPermission();
  }, [hasPermission, requestPermission]);

  useEffect(() => {
    initTensor('mobile_face_net', 1)
      .then((response: any) => console.log(response))
      .catch((error: any) => console.log(error));
  }, []);

  /**
   * Handle camera UI rotation
   *
   * @param {number} rotation Camera rotation
   */
  function handleUiRotation(rotation: number) {
    aRot.value = rotation;
  }

  /**
   * Hanldes camera mount error event
   *
   * @param {any} error Error event
   */
  function handleCameraMountError(error: any) {
    console.error('camera mount error', error);
  }

  /**
   * Handle detection result
   *
   * @param {Face[]} faces Detection result
   * @returns {void}
   */
  function handleFacesDetected(faces: Face[], _: Frame) {
    if (Object.keys(faces).length <= 0) return;
    const face = faces[0];
    if (face) {
      const { bounds } = face;
      const { width, height, x, y } = bounds;
      aFaceW.value = width;
      aFaceH.value = height;
      aFaceX.value = x;
      aFaceY.value = y;
      console.log(new Date().toTimeString(), 'data', face.data.length);
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.wrapCamera}>
        {hasPermission && cameraDevice ? (
          <>
            {cameraMounted && (
              <>
                <Camera
                  ref={camera as any}
                  style={StyleSheet.absoluteFill}
                  isActive={true}
                  device={cameraDevice}
                  onError={handleCameraMountError}
                  faceDetectionCallback={handleFacesDetected}
                  outputOrientation={'device'}
                  onUIRotationChanged={handleUiRotation}
                  faceDetectionOptions={{
                    ...faceDetectionOptions,
                    autoScale,
                  }}
                />
                <Animated.View style={animatedStyle} />
                {cameraPaused && (
                  <Text style={styles.textPaused}>Camera is PAUSED</Text>
                )}
              </>
            )}
            {!cameraMounted && (
              <Text style={styles.textNoMounted}>Camera is NOT mounted</Text>
            )}
          </>
        ) : (
          <Text style={styles.textNoDevice}>
            No camera device or permission
          </Text>
        )}
      </View>

      <View style={styles.wrapMainBtn}>
        <View style={styles.wrapBtn}>
          <Button
            onPress={() => setFacingFront((current) => !current)}
            title={'Toggle Cam'}
          />
          <Button
            onPress={() => setAutoScale((current) => !current)}
            title={`${autoScale ? 'Disable' : 'Enable'} Scale`}
          />
        </View>
        <View style={styles.wrapBtn}>
          <Button
            onPress={() => setCameraPaused((current) => !current)}
            title={`${cameraPaused ? 'Resume' : 'Pause'} Cam`}
          />
          <Button
            onPress={() => setCameraMounted((current) => !current)}
            title={`${cameraMounted ? 'Unmount' : 'Mount'} Cam`}
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  wrapCamera: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  textPaused: {
    width: '100%',
    backgroundColor: 'rgb(0,0,255)',
    textAlign: 'center',
    color: 'white',
  },
  textNoMounted: {
    width: '100%',
    backgroundColor: 'rgb(255,255,0)',
    textAlign: 'center',
  },
  textNoDevice: {
    width: '100%',
    backgroundColor: 'rgb(255,0,0)',
    textAlign: 'center',
    color: 'white',
  },
  wrapMainBtn: {
    position: 'absolute',
    bottom: 20,
    left: 0,
    right: 0,
    display: 'flex',
    flexDirection: 'column',
  },
  wrapBtn: {
    width: '100%',
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
});
