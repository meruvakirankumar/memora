import Foundation

enum EventType: String, Codable, CaseIterable, Identifiable {
    case expiration = "EXPIRATION"
    case payment = "PAYMENT"
    case renewal = "RENEWAL"
    case returnItem = "RETURN"
    case general = "GENERAL"

    var id: String { rawValue }

    var label: String {
        switch self {
        case .expiration: return "Expiration"
        case .payment: return "Payment"
        case .renewal: return "Renewal"
        case .returnItem: return "Return"
        case .general: return "General"
        }
    }
}

enum Confidence: String, Codable {
    case high = "High"
    case medium = "Medium"
    case low = "Low"

    var label: String { rawValue }
}

enum MemoryStatus {
    case upcoming
    case dueToday
    case overdue
    case completed

    var label: String {
        switch self {
        case .upcoming: return "Upcoming"
        case .dueToday: return "Due Today"
        case .overdue: return "Overdue"
        case .completed: return "Completed"
        }
    }

    var urgency: Int {
        switch self {
        case .overdue: return 0
        case .dueToday: return 1
        case .upcoming: return 2
        case .completed: return 3
        }
    }
}

struct Memory: Codable, Identifiable {
    let id: String
    var title: String
    var eventType: EventType
    var dateISO: String
    var confidence: Confidence
    var sourceText: String
    var createdAtISO: String
    var completedAtISO: String?
    var notificationId: String?
}

struct MemoryCandidate {
    var title: String
    var eventType: EventType
    var dateISO: String
    var confidence: Confidence
    var sourceText: String
}
