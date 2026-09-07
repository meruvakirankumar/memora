import SwiftUI

@main
struct MemoraApp: App {
    init() {
        ReminderScheduler.requestAuthorization()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
