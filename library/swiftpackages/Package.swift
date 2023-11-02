// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "library",
    platforms: [
        .iOS(.vIOS(versionNumber=17)), .watchOS(.vWatchOS(versionNumber=10)), .macOS(.vMacOS(versionNumber=14)), .tvOS(.vTvOS(versionNumber=17))
    ],
    products: [
        .library(
            name: "library",
            targets: ["library"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "library",
            path: "./library-debug-0.1.zip"
        ),
    ]
)
