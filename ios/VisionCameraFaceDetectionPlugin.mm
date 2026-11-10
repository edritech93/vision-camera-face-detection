#import <Foundation/Foundation.h>
#import <VisionCamera/FrameProcessorPlugin.h>
#import <VisionCamera/FrameProcessorPluginRegistry.h>
#import <VisionCamera/Frame.h>

#if __has_include("VisionCameraFaceDetection/VisionCameraFaceDetection-Swift.h")
#import "VisionCameraFaceDetection/VisionCameraFaceDetection-Swift.h"
#else
#import "VisionCameraFaceDetection-Swift.h"
#endif

@interface VisionCameraFaceDetectionPlugin (FrameProcessorPluginLoader)
@end

@implementation VisionCameraFaceDetectionPlugin (FrameProcessorPluginLoader)
+ (void) load {
  [FrameProcessorPluginRegistry addFrameProcessorPlugin:@"detectFaces"
    withInitializer:^FrameProcessorPlugin*(VisionCameraProxyHolder* proxy, NSDictionary* options) {
    return [[VisionCameraFaceDetectionPlugin alloc] initWithProxy:proxy withOptions:options];
  }];
}
@end
