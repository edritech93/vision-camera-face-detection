#include <jni.h>
#include "visioncamerafacedetectionOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::visioncamerafacedetection::initialize(vm);
}
