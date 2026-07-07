import { useEffect, useRef, useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  useWindowDimensions,
  Alert,
  Button,
  ActivityIndicator,
  Image,
  TextInput,
} from 'react-native';
import {
  useCameraDevice,
  useCameraPermission,
  type CameraRef,
} from 'react-native-vision-camera';
import {
  Camera,
  initTensor,
  detectFromBase64,
  type Face,
  type FaceScannerOptions,
  type TensorFaceOptions,
} from 'vision-camera-face-detection';
import Animated, {
  useAnimatedProps,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';
import {
  type Asset,
  type ImageLibraryOptions,
  type ImagePickerResponse,
  launchImageLibrary,
} from 'react-native-image-picker';
import { getPermissionReadStorage } from './permission';

export default function App() {
  const { hasPermission, requestPermission } = useCameraPermission();
  const { width: widthScreen, height: heightScreen } = useWindowDimensions();
  const [autoMode, setAutoMode] = useState<boolean>(true);
  const [cameraMounted, setCameraMounted] = useState<boolean>(false);
  const [cameraPaused, setCameraPaused] = useState<boolean>(false);
  const [facingFront, setFacingFront] = useState<boolean>(true);
  const [loadingSample, setLoadingSample] = useState<boolean>(false);
  const [dataSample, setDataSample] = useState<number[]>([]);
  const [imageSample, setImageSample] = useState<string>('');
  const distanceNum = useSharedValue<number>(2);
  const AnimatedTextInput = Animated.createAnimatedComponent(TextInput);

  const distanceAnimatedProps = useAnimatedProps(() => {
    return {
      text: distanceNum.value.toFixed(5),
    } as any;
  });

  const faceDetectorOptions = useRef<FaceScannerOptions>({
    performanceMode: 'fast',
    runClassifications: true,
    runContours: true,
    runLandmarks: true,
    windowWidth: widthScreen,
    windowHeight: heightScreen,
  }).current;
  //
  // vision camera ref
  //
  const camera = useRef<CameraRef>(null);
  const cameraDevice = useCameraDevice(facingFront ? 'front' : 'back');
  //
  // face rectangle position
  //
  const aFaceW = useSharedValue(0);
  const aFaceH = useSharedValue(0);
  const aFaceX = useSharedValue(0);
  const aFaceY = useSharedValue(0);
  const boundingBoxStyle = useAnimatedStyle(() => ({
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
  }));

  useEffect(() => {
    if (hasPermission) return;
    requestPermission();
  }, [hasPermission, requestPermission]);

  useEffect(() => {
    if (!hasPermission) return;
    const result = initTensor('facenet_512');
    console.log(`Tensor initialized with result: ${result}`);
  }, [hasPermission]);

  async function _pickImageSample() {
    try {
      await getPermissionReadStorage().catch((error) => {
        console.log(error);
        return;
      });
      setLoadingSample(true);
      const options: ImageLibraryOptions = {
        mediaType: 'photo',
        selectionLimit: 1,
        includeBase64: true,
      };
      const response: ImagePickerResponse = await launchImageLibrary(options);
      if (!response.assets) {
        throw { message: 'Invalid Attachment' };
      }
      const dataAsset: Asset[] = response.assets;
      if (dataAsset.length === 0) {
        throw { message: 'No Attachment' };
      }
      const itemAsset = dataAsset[0];
      if (!itemAsset?.uri) {
        throw { message: 'Invalid URI' };
      }
      const imageFull = itemAsset.base64 ?? '';
      const tensorOptions: TensorFaceOptions = {
        base64Image: imageFull,
        performanceMode: 'fast',
        runClassifications: true,
        runContours: true,
        runLandmarks: true,
        windowWidth: widthScreen,
        windowHeight: heightScreen,
      };
      const imageFace: Face | null = detectFromBase64(tensorOptions);
      if (
        !imageFace ||
        imageFace.base64 === '' ||
        imageFace.data === undefined
      ) {
        throw { message: 'No Face detected!' };
      }
      const arrayRes: number[] = imageFace.data.map((e: string) => {
        const stringFixed: string = parseFloat(e).toFixed(5);
        return parseFloat(stringFixed);
      });
      setDataSample(arrayRes);
      setImageSample(imageFace?.base64 ?? '');
      console.log('Load Sample Successfully');
    } catch (error) {
      console.log(error);
      Alert.alert('Error', JSON.stringify(error));
      setDataSample([]);
      setImageSample('');
    } finally {
      setLoadingSample(false);
    }
  }

  const onFaceScanned = (faces: Face[]) => {
    console.log(`Detected ${faces.length} face(s)`);
    if (Object.keys(faces).length <= 0) {
      aFaceW.value = 0;
      aFaceH.value = 0;
      aFaceX.value = 0;
      aFaceY.value = 0;
      return;
    }
    const face = faces[0];
    if (face) {
      const { bounds } = face;
      const { width, height, x, y } = bounds;
      aFaceW.value = width;
      aFaceH.value = height;
      aFaceX.value = x;
      aFaceY.value = y;
      if (face.data) {
        const arrayCamera: any = face.data.map((e: string) => {
          const stringFixed: string = parseFloat(e).toFixed(5);
          return parseFloat(stringFixed);
        });
        const knownEmb: any = dataSample;
        let distance = 0.0;
        for (let i = 0; i < arrayCamera.length; i++) {
          const diff = arrayCamera[i] - knownEmb[i];
          distance += diff * diff;
        }
        distanceNum.value = distance;
        console.log(`Distance: ${distanceNum.value}`);
      }
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.wrapCamera}>
        {hasPermission && cameraDevice ? (
          <>
            {cameraMounted && (
              <>
                <Camera
                  ref={camera}
                  style={StyleSheet.absoluteFill}
                  isActive={!cameraPaused}
                  device={cameraDevice}
                  orientationSource={'device'}
                  onFaceScanned={onFaceScanned}
                  onError={(error: Error) => {
                    console.error(`Failed to detect faces!`, error);
                  }}
                  {...faceDetectorOptions}
                  autoMode={autoMode}
                  cameraFacing={facingFront ? 'front' : 'back'}
                />
                <Animated.View style={boundingBoxStyle}>
                  <AnimatedTextInput
                    editable={false}
                    underlineColorAndroid={'transparent'}
                    style={styles.textDistance}
                    animatedProps={distanceAnimatedProps}
                  />
                </Animated.View>
                {cameraPaused && (
                  <Text style={styles.textPaused}>Camera is PAUSED</Text>
                )}
              </>
            )}
            {!cameraMounted && (
              <View style={styles.wrapCenter}>
                <Text style={styles.textNoMounted}>Camera is NOT mounted</Text>
                <Button
                  title={'Pick Image Sample'}
                  onPress={() => _pickImageSample()}
                />
                <ActivityIndicator
                  color={'red'}
                  size={'large'}
                  animating={loadingSample}
                />
                {dataSample.length > 0 && (
                  <Image
                    source={{ uri: `data:image/png;base64,${imageSample}` }}
                    style={styles.imageBase64Face}
                  />
                )}
              </View>
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
            onPress={() => setAutoMode((current) => !current)}
            title={`${autoMode ? 'Disable' : 'Enable'} AutoMode`}
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
  wrapCenter: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textDistance: {
    backgroundColor: 'rgb(0,255,0)',
    color: 'black',
    paddingHorizontal: 8,
    minWidth: 80,
  },
  imageBase64Face: {
    height: 100,
    width: 100,
  },
});
