///
/// NitroNetworkInfo-Swift-Cxx-Bridge.cpp
/// This file was generated by nitrogen. DO NOT MODIFY THIS FILE.
/// https://github.com/mrousavy/nitro
/// Copyright © 2025 Marc Rousavy @ Margelo
///

#include "NitroNetworkInfo-Swift-Cxx-Bridge.hpp"

// Include C++ implementation defined types
#include "HybridNitroNetworkInfoSpecSwift.hpp"
#include "NitroNetworkInfo-Swift-Cxx-Umbrella.hpp"

namespace margelo::nitro::nitronetworkinfo::bridge::swift {

  // pragma MARK: std::function<void()>
  Func_void create_Func_void(void* _Nonnull swiftClosureWrapper) {
    auto swiftClosure = NitroNetworkInfo::Func_void::fromUnsafe(swiftClosureWrapper);
    return [swiftClosure = std::move(swiftClosure)]() mutable -> void {
      swiftClosure.call();
    };
  }
  
  // pragma MARK: std::function<void(const NitroNetworkStatusInfo& /* networkInfo */)>
  Func_void_NitroNetworkStatusInfo create_Func_void_NitroNetworkStatusInfo(void* _Nonnull swiftClosureWrapper) {
    auto swiftClosure = NitroNetworkInfo::Func_void_NitroNetworkStatusInfo::fromUnsafe(swiftClosureWrapper);
    return [swiftClosure = std::move(swiftClosure)](const NitroNetworkStatusInfo& networkInfo) mutable -> void {
      swiftClosure.call(networkInfo);
    };
  }
  
  // pragma MARK: std::shared_ptr<margelo::nitro::nitronetworkinfo::HybridNitroNetworkInfoSpec>
  std::shared_ptr<margelo::nitro::nitronetworkinfo::HybridNitroNetworkInfoSpec> create_std__shared_ptr_margelo__nitro__nitronetworkinfo__HybridNitroNetworkInfoSpec_(void* _Nonnull swiftUnsafePointer) {
    NitroNetworkInfo::HybridNitroNetworkInfoSpec_cxx swiftPart = NitroNetworkInfo::HybridNitroNetworkInfoSpec_cxx::fromUnsafe(swiftUnsafePointer);
    return std::make_shared<margelo::nitro::nitronetworkinfo::HybridNitroNetworkInfoSpecSwift>(swiftPart);
  }
  void* _Nonnull get_std__shared_ptr_margelo__nitro__nitronetworkinfo__HybridNitroNetworkInfoSpec_(std__shared_ptr_margelo__nitro__nitronetworkinfo__HybridNitroNetworkInfoSpec_ cppType) {
    std::shared_ptr<margelo::nitro::nitronetworkinfo::HybridNitroNetworkInfoSpecSwift> swiftWrapper = std::dynamic_pointer_cast<margelo::nitro::nitronetworkinfo::HybridNitroNetworkInfoSpecSwift>(cppType);
  #ifdef NITRO_DEBUG
    if (swiftWrapper == nullptr) [[unlikely]] {
      throw std::runtime_error("Class \"HybridNitroNetworkInfoSpec\" is not implemented in Swift!");
    }
  #endif
    NitroNetworkInfo::HybridNitroNetworkInfoSpec_cxx& swiftPart = swiftWrapper->getSwiftPart();
    return swiftPart.toUnsafe();
  }

} // namespace margelo::nitro::nitronetworkinfo::bridge::swift
