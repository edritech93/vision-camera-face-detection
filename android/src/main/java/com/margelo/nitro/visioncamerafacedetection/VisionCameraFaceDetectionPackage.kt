package com.margelo.nitro.visioncamerafacedetection

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry

class VisionCameraFaceDetectionPackage : TurboReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return null
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider { HashMap() }
  }

  companion object {
    const val TAG_DEBUG = "edritech93"

    init {
      System.loadLibrary("visioncamerafacedetection")
      FrameProcessorPluginRegistry.addFrameProcessorPlugin("detectFaces") { proxy, options ->
        VisionCameraFaceDetectionPlugin(
          proxy,
          options
        )
      }
    }
  }
}
