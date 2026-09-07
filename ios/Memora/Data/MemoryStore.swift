import Foundation

/// Stores confirmed memories locally as JSON in UserDefaults.
enum MemoryStore {

    private static let key = "memora.memories.v1"

    static func load() -> [Memory] {
        guard let data = UserDefaults.standard.data(forKey: key) else { return [] }
        return (try? JSONDecoder().decode([Memory].self, from: data)) ?? []
    }

    static func save(_ memories: [Memory]) {
        guard let data = try? JSONEncoder().encode(memories) else { return }
        UserDefaults.standard.set(data, forKey: key)
    }
}
