#include <jni.h>
#include "visioncamerafacedetectionOnLoad.hpp"

#include <fbjni/fbjni.h>


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return facebook::jni::initialize(vm, []() {
    margelo::nitro::visioncamerafacedetection::registerAllNatives();
  });
}