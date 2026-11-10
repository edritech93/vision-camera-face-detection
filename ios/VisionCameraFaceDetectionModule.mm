#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(VisionCameraFaceDetectionModule, NSObject)

RCT_EXTERN_METHOD(detectFromBase64:(NSString)imageString
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(initTensor:(NSString)modelName
                  withCount:(NSNumber)count
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

@end
