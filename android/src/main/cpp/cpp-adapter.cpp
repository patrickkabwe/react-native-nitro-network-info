#include <jni.h>
#include "NitroNetworkInfoOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::nitronetworkinfo::initialize(vm);
}
