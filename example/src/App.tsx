import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Button, Dimensions, SafeAreaView, StyleSheet } from 'react-native';
import {
  Camera,
  useFrameProcessor,
  type Frame,
  CameraRuntimeError,
  useCameraFormat,
  useCameraDevice,
} from 'react-native-vision-camera';
import {
  scanFaces,
  type FaceBoundType,
  type FaceType,
  detectFromBase64,
} from 'vision-camera-face-detection';
import Animated, {
  useSharedValue as useSharedValueR,
  useAnimatedStyle,
  withSpring,
} from 'react-native-reanimated';
import { Worklets, useSharedValue } from 'react-native-worklets-core';
import { launchImageLibrary } from 'react-native-image-picker';
import { getPermissionReadStorage } from './permission';

const SCREEN_WIDTH = Dimensions.get('window').width;
const SCREEN_HEIGHT = Dimensions.get('window').height;
const screenAspectRatio = SCREEN_HEIGHT / SCREEN_WIDTH;
const enableHdr = false;
const enableNightMode = false;
const targetFps = 30;

export default function App() {
  const [hasPermission, setHasPermission] = useState<boolean>(false);

  const camera = useRef<Camera>(null);
  const device = useCameraDevice('front', {
    physicalDevices: [
      'ultra-wide-angle-camera',
      'wide-angle-camera',
      'telephoto-camera',
    ],
  });
  const format = useCameraFormat(device, [
    { fps: targetFps },
    { photoAspectRatio: screenAspectRatio },
    { photoResolution: 'max' },
  ]);
  const fps = Math.min(format?.maxFps ?? 1, targetFps);
  const rectWidth = useSharedValue(100); // rect width
  const rectHeight = useSharedValue(100); // rect height
  const rectX = useSharedValue(100); // rect x position
  const rectY = useSharedValue(100); // rect y position
  const rectWidthR = useSharedValueR(100); // rect width
  const rectHeightR = useSharedValueR(100); // rect height
  const rectXR = useSharedValueR(0); // rect x position
  const rectYR = useSharedValueR(0); // rect y position
  const updateRect = Worklets.createRunInJsFn((frame: any) => {
    rectWidthR.value = frame.width;
    rectHeightR.value = frame.height;
    rectXR.value = frame.x;
    rectYR.value = frame.y;
  });

  const frameProcessor = useFrameProcessor((frame: Frame) => {
    'worklet';
    const dataFace: FaceType = scanFaces(frame);
    // console.log('dataFace => ', dataFace);
    // NOTE: handle face detection
    if (dataFace && dataFace.bounds) {
      const { width: frameWidth, height: frameHeight } = frame;
      const xFactor = SCREEN_WIDTH / frameWidth;
      const yFactor = SCREEN_HEIGHT / frameHeight;
      const bounds: FaceBoundType = dataFace.bounds;
      rectWidth.value = bounds.width * xFactor;
      rectHeight.value = bounds.height * yFactor;
      rectX.value = bounds.x * xFactor;
      rectY.value = bounds.y * yFactor;
      updateRect({
        width: rectWidth.value,
        height: rectHeight.value,
        x: rectX.value,
        y: rectY.value,
      });
    }
  }, []);

  const faceAnimStyle = useAnimatedStyle(() => {
    return {
      position: 'absolute',
      backgroundColor: 'red',
      width: withSpring(rectWidthR.value),
      height: withSpring(rectHeightR.value),
      transform: [
        { translateX: withSpring(rectXR.value) },
        { translateY: withSpring(rectYR.value) },
      ],
    };
  });

  useEffect(() => {
    async function _getPermission() {
      const status = await Camera.requestCameraPermission();
      setHasPermission(status === 'granted');
    }
    _getPermission();
  }, []);

  const onError = useCallback((error: CameraRuntimeError) => {
    console.error(error);
  }, []);

  const onInitialized = useCallback(() => {
    console.log('Camera initialized!');
  }, []);

  const _onOpenImage = async () => {
    await getPermissionReadStorage().catch((error: Error) => {
      console.log(error);
      return;
    });
    const result = await launchImageLibrary({
      mediaType: 'photo',
      includeBase64: true,
    }).catch((error) => {
      console.log(error);
      return;
    });
    if (
      result &&
      result.assets &&
      result.assets.length > 0 &&
      result.assets[0]?.uri &&
      result.assets[0]?.base64
    ) {
      const base64Face = await detectFromBase64(result.assets[0].base64).catch(
        (error: Error) => {
          console.log(error);
          return;
        }
      );
      console.log('base64Face => ', base64Face);
    }
  };

  if (device != null && format != null && hasPermission) {
    const pixelFormat = format.pixelFormats.includes('yuv') ? 'yuv' : 'native';
    return (
      <SafeAreaView style={styles.container}>
        <Camera
          ref={camera}
          style={StyleSheet.absoluteFill}
          device={device}
          format={format}
          fps={fps}
          photoHdr={enableHdr}
          lowLightBoost={device.supportsLowLightBoost && enableNightMode}
          isActive={true}
          onInitialized={onInitialized}
          onError={onError}
          enableZoomGesture={false}
          enableFpsGraph={false}
          orientation={'portrait'}
          pixelFormat={pixelFormat}
          photo={true}
          video={false}
          audio={false}
          frameProcessor={frameProcessor}
        />
        <Animated.View style={faceAnimStyle} />
        <Button title={'Open Photo'} onPress={_onOpenImage} />
      </SafeAreaView>
    );
  } else {
    return null;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  textResult: {
    color: 'black',
    marginHorizontal: 8,
  },
  wrapBottom: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 16,
  },
  imgPreview: {
    width: SCREEN_WIDTH,
    height: SCREEN_HEIGHT,
    position: 'absolute',
  },
  btnClose: {},
});
