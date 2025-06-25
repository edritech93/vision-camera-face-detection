#import "VisionCameraFaceDetection.h"
#import "VisionCameraFaceDetection-Swift.h"

@implementation VisionCameraFaceDetection
RCT_EXPORT_MODULE()

VisionCameraFaceDetectionModule *faceDetectionModule = [[VisionCameraFaceDetectionModule alloc] init];

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeVisionCameraFaceDetectionSpecJSI>(params);
}

- (NSNumber *)multiply:(double)a b:(double)b {
  return [faceDetectionModule getMultiplyWithA:a b:b];
}

@end
