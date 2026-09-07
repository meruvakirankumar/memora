import Foundation

/// Turns recognized text into a structured memory candidate.
/// Mirrors the Memora intent: understand, identify what matters, extract.
enum MemoryExtractor {

    static func extract(_ text: String) -> MemoryCandidate {
        let normalized = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let lines = normalized
            .split(whereSeparator: \.isNewline)
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        let eventType = detectEventType(normalized)
        let dateISO = extractActionDate(normalized)
        let title = extractTitle(lines: lines, eventType: eventType)
        let hasExplicitDate = firstDateMatch(in: normalized) != nil

        let confidence: Confidence
        if eventType != .general && hasExplicitDate {
            confidence = .high
        } else if hasExplicitDate {
            confidence = .medium
        } else {
            confidence = .low
        }

        return MemoryCandidate(
            title: title,
            eventType: eventType,
            dateISO: dateISO,
            confidence: confidence,
            sourceText: normalized
        )
    }

    static func detectEventType(_ text: String) -> EventType {
        let upper = text.uppercased()
        if matches(upper, "EXP|EXPIR|BEST BY|USE BY") { return .expiration }
        if matches(upper, "DUE|PAY|BILL|INVOICE") { return .payment }
        if matches(upper, "RENEW|SUBSCRIPTION|POLICY") { return .renewal }
        if matches(upper, "RETURN|EXCHANGE") { return .returnItem }
        return .general
    }

    static func extractActionDate(_ text: String) -> String {
        if let match = labeledDateMatch(in: text) {
            return datePartsToISO(match.month, match.second, match.year)
        }
        if let match = looseDateMatch(in: text) {
            return datePartsToISO(match.month, match.second, match.year)
        }
        return isoString(from: Calendar.current.date(byAdding: .day, value: 1, to: startOfDay(Date()))!)
    }

    // MARK: - Date parsing

    private struct DateParts { let month: Int; let second: Int; let year: Int? }

    private static func datePartsToISO(_ month: Int, _ second: Int, _ yearPart: Int?) -> String {
        let calendar = Calendar.current
        let currentYear = calendar.component(.year, from: Date())
        let currentShortYear = currentYear % 100
        let hasYear = yearPart != nil
        let isLikelyMonthYear = !hasYear && second >= currentShortYear

        let year: Int
        if let yearPart = yearPart {
            year = normalizeYear(yearPart)
        } else if isLikelyMonthYear {
            year = normalizeYear(second)
        } else {
            year = currentYear
        }

        let day = (hasYear || !isLikelyMonthYear) ? second : lastDayOfMonth(year: year, month: month)
        var date = safeDate(year: year, month: month, day: day)

        if !hasYear && !isLikelyMonthYear && date < startOfDay(Date()) {
            date = calendar.date(byAdding: .year, value: 1, to: date) ?? date
        }
        return isoString(from: date)
    }

    private static func normalizeYear(_ year: Int) -> Int { year < 100 ? 2000 + year : year }

    private static func lastDayOfMonth(year: Int, month: Int) -> Int {
        let safeMonth = min(max(month, 1), 12)
        var components = DateComponents()
        components.year = year
        components.month = safeMonth
        let calendar = Calendar.current
        guard let date = calendar.date(from: components),
              let range = calendar.range(of: .day, in: .month, for: date) else { return 28 }
        return range.count
    }

    private static func safeDate(year: Int, month: Int, day: Int) -> Date {
        let safeMonth = min(max(month, 1), 12)
        let maxDay = lastDayOfMonth(year: year, month: safeMonth)
        var components = DateComponents()
        components.year = year
        components.month = safeMonth
        components.day = min(max(day, 1), maxDay)
        return Calendar.current.date(from: components) ?? startOfDay(Date())
    }

    // MARK: - Regex helpers

    private static func firstDateMatch(in text: String) -> DateParts? {
        labeledDateMatch(in: text) ?? looseDateMatch(in: text)
    }

    private static func labeledDateMatch(in text: String) -> DateParts? {
        let pattern = "(?:EXP|EXPIRES|EXPIRY|BEST BY|USE BY|DUE|PAY BY|RENEW(?:AL)?|RETURN BY)\\D*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?"
        return dateParts(from: text, pattern: pattern, caseInsensitive: true)
    }

    private static func looseDateMatch(in text: String) -> DateParts? {
        let pattern = "\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b"
        return dateParts(from: text, pattern: pattern, caseInsensitive: false)
    }

    private static func dateParts(from text: String, pattern: String, caseInsensitive: Bool) -> DateParts? {
        let options: NSRegularExpression.Options = caseInsensitive ? [.caseInsensitive] : []
        guard let regex = try? NSRegularExpression(pattern: pattern, options: options) else { return nil }
        let range = NSRange(text.startIndex..<text.endIndex, in: text)
        guard let match = regex.firstMatch(in: text, options: [], range: range) else { return nil }

        func group(_ index: Int) -> Int? {
            guard match.numberOfRanges > index,
                  let r = Range(match.range(at: index), in: text) else { return nil }
            return Int(text[r])
        }

        guard let month = group(1), let second = group(2) else { return nil }
        return DateParts(month: month, second: second, year: group(3))
    }

    private static func extractTitle(lines: [String], eventType: EventType) -> String {
        let informational = "\\b(MFG|BATCH|LOT|EXP|DUE|PAY|RENEW|RETURN)\\b"
        if let line = lines.first(where: { !matches($0.uppercased(), informational) }) {
            let cleaned = line.replacingOccurrences(of: "[^a-zA-Z0-9 ]", with: " ", options: .regularExpression)
                .trimmingCharacters(in: .whitespaces)
            if !cleaned.isEmpty { return toTitleCase(cleaned) }
        }
        return "\(eventType.label) memory"
    }

    private static func matches(_ text: String, _ pattern: String) -> Bool {
        text.range(of: pattern, options: [.regularExpression, .caseInsensitive]) != nil
    }

    private static func toTitleCase(_ value: String) -> String {
        value.lowercased()
            .split(separator: " ")
            .map { $0.prefix(1).uppercased() + $0.dropFirst() }
            .joined(separator: " ")
    }

    // MARK: - Date utilities

    private static func startOfDay(_ date: Date) -> Date { Calendar.current.startOfDay(for: date) }

    private static func isoString(from date: Date) -> String {
        let components = Calendar.current.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", components.year ?? 2000, components.month ?? 1, components.day ?? 1)
    }
}
