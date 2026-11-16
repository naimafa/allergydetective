import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        PlatformModule_iosKt.startApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
