package com.margelo.nitro.visioncamerafacedetection

import android.util.Log
import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.ModuleSpec
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry

class VisionCameraFaceDetectionPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return null
  }

  override fun getViewManagers(reactContext: ReactApplicationContext): List<ModuleSpec> {
    Log.d("VisionCameraFaceDetection", "ReactContext initialized!")
    ReactContextHolder.set(reactContext)
    return super.getViewManagers(reactContext)
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider { HashMap() }
  }

  companion object {
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
