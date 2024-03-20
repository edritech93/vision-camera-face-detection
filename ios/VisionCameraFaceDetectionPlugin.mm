#import <VisionCamera/FrameProcessorPlugin.h>
#import <VisionCamera/FrameProcessorPluginRegistry.h>

#if __has_include("VisionCameraFaceDetection/VisionCameraFaceDetection-Swift.h")
#import "VisionCameraFaceDetection/VisionCameraFaceDetection-Swift.h"
#else
#import "VisionCameraFaceDetection-Swift.h"
#endif

// VISION_EXPORT_SWIFT_FRAME_PROCESSOR(VisionCameraFaceDetectionPlugin, scanFace)

@interface VisionCameraFaceDetectionPlugin (FrameProcessorPluginLoader)
@end

@implementation VisionCameraFaceDetectionPlugin (FrameProcessorPluginLoader)

+ (void)load
{
    [FrameProcessorPluginRegistry addFrameProcessorPlugin:@"scanFace"
                                          withInitializer:^FrameProcessorPlugin* _Nonnull(VisionCameraProxyHolder* _Nonnull proxy,
                                                                                          NSDictionary* _Nullable options) {
        return [[VisionCameraFaceDetectionPlugin alloc] initWithProxy:proxy withOptions:options];
    }];
}

@end
