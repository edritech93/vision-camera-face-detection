#if __has_include(<VisionCamera/FrameProcessorPlugin.h>)
#import <VisionCamera/FrameProcessorPlugin.h>
#import <VisionCamera/FrameProcessorPluginRegistry.h>

#import "VisionCameraFaceDetection-Swift.h"

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

#endif
