package com.visioncamerafacedetection

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry
import java.util.HashMap

class VisionCameraFaceDetectionPackage : BaseReactPackage() {
  companion object {
    init {
      FrameProcessorPluginRegistry.addFrameProcessorPlugin("detectFaces") { proxy, options ->
        VisionCameraFaceDetectionPlugin(proxy, options)
      }
    }
  }

  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == VisionCameraFaceDetectionModule.NAME) {
      VisionCameraFaceDetectionModule(reactContext)
    } else {
      null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      moduleInfos[VisionCameraFaceDetectionModule.NAME] = ReactModuleInfo(
        VisionCameraFaceDetectionModule.NAME,
        VisionCameraFaceDetectionModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        false,  // isCxxModule
        true // isTurboModule
      )
      moduleInfos
    }
  }
}
