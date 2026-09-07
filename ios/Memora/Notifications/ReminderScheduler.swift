import Foundation
import UserNotifications

/// Schedules a local reminder the day before an action date at 9am.
enum ReminderScheduler {

    static func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    static func schedule(_ candidate: MemoryCandidate) -> String? {
        guard let actionDate = MemoryStatusResolver.parseDate(candidate.dateISO) else { return nil }
        let calendar = Calendar.current

        guard let reminderDay = calendar.date(byAdding: .day, value: -1, to: actionDate) else { return nil }
        var components = calendar.dateComponents([.year, .month, .day], from: reminderDay)
        components.hour = 9
        components.minute = 0

        guard let fireDate = calendar.date(from: components), fireDate > Date() else { return nil }

        let identifier = UUID().uuidString
        let content = UNMutableNotificationContent()
        content.title = "\(candidate.title) needs attention"
        content.body = "\(candidate.eventType.label) is coming up on \(MemoryStatusResolver.formatDate(candidate.dateISO))."
        content.sound = .default

        let triggerComponents = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: fireDate)
        let trigger = UNCalendarNotificationTrigger(dateMatching: triggerComponents, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request)
        return identifier
    }

    static func cancel(_ identifier: String) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }
}
