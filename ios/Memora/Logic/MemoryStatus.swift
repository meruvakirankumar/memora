import Foundation

enum MemoryStatusResolver {

    private static let months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

    static func status(for memory: Memory) -> MemoryStatus {
        if memory.completedAtISO != nil { return .completed }
        return status(forDate: memory.dateISO)
    }

    static func status(forDate dateISO: String) -> MemoryStatus {
        guard let date = parseDate(dateISO) else { return .upcoming }
        let today = Calendar.current.startOfDay(for: Date())
        let target = Calendar.current.startOfDay(for: date)
        if target < today { return .overdue }
        if target == today { return .dueToday }
        return .upcoming
    }

    static func isValidISODate(_ value: String) -> Bool {
        guard value.range(of: "^\\d{4}-\\d{2}-\\d{2}$", options: .regularExpression) != nil else { return false }
        return parseDate(value) != nil
    }

    static func formatDate(_ dateISO: String) -> String {
        guard let date = parseDate(dateISO) else { return dateISO }
        let components = Calendar.current.dateComponents([.year, .month, .day], from: date)
        let monthIndex = (components.month ?? 1) - 1
        let monthName = months[min(max(monthIndex, 0), 11)]
        return "\(monthName) \(components.day ?? 1), \(components.year ?? 2000)"
    }

    static func parseDate(_ dateISO: String) -> Date? {
        let parts = dateISO.split(separator: "-").compactMap { Int($0) }
        guard parts.count == 3 else { return nil }
        var components = DateComponents()
        components.year = parts[0]
        components.month = parts[1]
        components.day = parts[2]
        return Calendar.current.date(from: components)
    }
}
