require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "VisionCameraFaceDetection"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "15.5" }
  s.source       = { :git => "https://github.com/edritech93/vision-camera-face-detection.git", :tag => "#{s.version}" }


  s.source_files = [
    "ios/**/*.{swift}",
    "ios/**/*.{m,mm}",
    "cpp/**/*.{hpp,cpp}",
  ]

  s.dependency 'React-jsi'
  s.dependency 'React-callinvoker'

  s.dependency 'GoogleMLKit/FaceDetection', '7.0.0'
  s.dependency "VisionCamera"
  s.dependency "TensorFlowLiteSwift"

  load 'nitrogen/generated/ios/VisionCameraFaceDetection+autolinking.rb'
  add_nitrogen_files(s)

  install_modules_dependencies(s)
end
