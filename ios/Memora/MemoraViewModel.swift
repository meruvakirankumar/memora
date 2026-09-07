import SwiftUI

private let sampleOCR = "MILK\nMFG 09/25\nBATCH 84921\nEXP 08/27"

@MainActor
final class MemoraViewModel: ObservableObject {

    @Published private(set) var memories: [Memory] = MemoryStore.load()
    @Published var sourceText: String = sampleOCR
    @Published var candidate: MemoryCandidate = MemoryExtractor.extract(sampleOCR)
    @Published var image: UIImage?
    @Published var dateError = false

    var activeMemories: [Memory] {
        memories
            .filter { MemoryStatusResolver.status(for: $0) != .completed }
            .sorted { lhs, rhs in
                let lu = MemoryStatusResolver.status(for: lhs).urgency
                let ru = MemoryStatusResolver.status(for: rhs).urgency
                return lu == ru ? lhs.dateISO < rhs.dateISO : lu < ru
            }
    }

    var completedMemories: [Memory] {
        memories.filter { MemoryStatusResolver.status(for: $0) == .completed }
    }

    var summary: (overdue: Int, dueToday: Int, upcoming: Int) {
        var overdue = 0, dueToday = 0, upcoming = 0
        for memory in activeMemories {
            switch MemoryStatusResolver.status(for: memory) {
            case .overdue: overdue += 1
            case .dueToday: dueToday += 1
            case .upcoming: upcoming += 1
            case .completed: break
            }
        }
        return (overdue, dueToday, upcoming)
    }

    var candidateStatus: MemoryStatus {
        MemoryStatusResolver.isValidISODate(candidate.dateISO)
            ? MemoryStatusResolver.status(forDate: candidate.dateISO)
            : .upcoming
    }

    func onImageSelected(_ image: UIImage?) {
        self.image = image
        candidate = MemoryExtractor.extract(sourceText)
    }

    func analyze() {
        candidate = MemoryExtractor.extract(sourceText)
    }

    func updateDate(_ value: String) {
        candidate.dateISO = value
        dateError = false
    }

    func confirm() {
        guard MemoryStatusResolver.isValidISODate(candidate.dateISO) else {
            dateError = true
            return
        }

        let notificationId = ReminderScheduler.schedule(candidate)
        let memory = Memory(
            id: String(Int(Date().timeIntervalSince1970 * 1000)),
            title: candidate.title,
            eventType: candidate.eventType,
            dateISO: candidate.dateISO,
            confidence: candidate.confidence,
            sourceText: candidate.sourceText,
            createdAtISO: ISO8601DateFormatter().string(from: Date()),
            completedAtISO: nil,
            notificationId: notificationId
        )
        memories.insert(memory, at: 0)
        persist()
        resetCapture()
    }

    func complete(_ id: String) {
        guard let index = memories.firstIndex(where: { $0.id == id }) else { return }
        if let notificationId = memories[index].notificationId {
            ReminderScheduler.cancel(notificationId)
        }
        memories[index].completedAtISO = ISO8601DateFormatter().string(from: Date())
        persist()
    }

    func delete(_ id: String) {
        guard let memory = memories.first(where: { $0.id == id }) else { return }
        if let notificationId = memory.notificationId {
            ReminderScheduler.cancel(notificationId)
        }
        memories.removeAll { $0.id == id }
        persist()
    }

    private func resetCapture() {
        sourceText = sampleOCR
        candidate = MemoryExtractor.extract(sampleOCR)
        image = nil
    }

    private func persist() {
        MemoryStore.save(memories)
    }
}
