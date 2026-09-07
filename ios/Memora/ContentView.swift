import PhotosUI
import SwiftUI
import UIKit

struct ContentView: View {
    @StateObject private var viewModel = MemoraViewModel()
    @State private var showCamera = false
    @State private var showLibrary = false

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                hero
                capturePanel
                understandPanel
                confirmPanel
                remindPanel
                if !viewModel.completedMemories.isEmpty {
                    completedPanel
                }
            }
            .padding(20)
        }
        .background(Theme.background.ignoresSafeArea())
        .sheet(isPresented: $showCamera) {
            ImagePicker(sourceType: .camera) { viewModel.onImageSelected($0) }
        }
        .sheet(isPresented: $showLibrary) {
            PhotoPicker { viewModel.onImageSelected($0) }
        }
    }

    // MARK: - Sections

    private var hero: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("MEMORA").font(.caption).bold().foregroundColor(Theme.gold)
            Text("Capture it. Understand it. Act on it.")
                .font(.system(size: 30, weight: .black))
                .foregroundColor(Color(hex: 0xFFF8E8))
            Text("Turn real-world labels, receipts, renewals, and dates into confirmed memories with timely reminders.")
                .font(.system(size: 15))
                .foregroundColor(Color(hex: 0xD8E8DE))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(Theme.hero)
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }

    private var capturePanel: some View {
        Panel(title: "Capture", step: "1/4") {
            if let image = viewModel.image {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(maxWidth: .infinity)
                    .aspectRatio(4.0 / 3.0, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
            } else {
                VStack(spacing: 8) {
                    Text("No image captured").font(.system(size: 17, weight: .black)).foregroundColor(Theme.darkGreen)
                    Text("Use the camera or choose a product label, bill, or document.")
                        .font(.system(size: 14)).foregroundColor(Theme.muted).multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .aspectRatio(4.0 / 3.0, contentMode: .fit)
                .background(Color(hex: 0xEAF3EE))
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            HStack(spacing: 10) {
                PrimaryButton(title: "Take Photo") { showCamera = true }
                PrimaryButton(title: "Select Image", background: Theme.darkGreen) { showLibrary = true }
            }
        }
    }

    private var understandPanel: some View {
        Panel(title: "Understand", step: "2/4") {
            FieldLabel("Recognized text")
            TextEditor(text: $viewModel.sourceText)
                .frame(minHeight: 110)
                .padding(8)
                .background(Theme.field)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            PrimaryButton(title: "Analyze Memory", fullWidth: true) { viewModel.analyze() }
        }
    }

    private var confirmPanel: some View {
        Panel(title: "Confirm", step: "3/4") {
            FieldLabel("Title")
            TextField("Title", text: $viewModel.candidate.title)
                .textFieldStyle(.plain)
                .padding(14)
                .background(Theme.field)
                .clipShape(RoundedRectangle(cornerRadius: 14))

            FieldLabel("Event")
            HStack(spacing: 8) {
                ForEach(EventType.allCases) { type in
                    let selected = viewModel.candidate.eventType == type
                    Text(type.label)
                        .font(.system(size: 11, weight: .black))
                        .foregroundColor(selected ? Color(hex: 0xFFFDF7) : Color(hex: 0x40504A))
                        .padding(.horizontal, 10).padding(.vertical, 8)
                        .background(selected ? Theme.darkGreen : Color(hex: 0xECE3D0))
                        .clipShape(Capsule())
                        .onTapGesture { viewModel.candidate.eventType = type }
                }
            }

            FieldLabel("Action date (YYYY-MM-DD)")
            TextField("YYYY-MM-DD", text: Binding(
                get: { viewModel.candidate.dateISO },
                set: { viewModel.updateDate($0) }
            ))
            .textFieldStyle(.plain)
            .padding(14)
            .background(Theme.field)
            .clipShape(RoundedRectangle(cornerRadius: 14))

            if viewModel.dateError {
                Text("Use YYYY-MM-DD so Memora can remind you on time.")
                    .font(.system(size: 12)).foregroundColor(Theme.accent)
            }

            HStack(spacing: 10) {
                Text("Confidence: \(viewModel.candidate.confidence.label)")
                    .font(.system(size: 13, weight: .black)).foregroundColor(Theme.darkGreen)
                Text("Status: \(viewModel.candidateStatus.label)")
                    .font(.system(size: 13, weight: .black)).foregroundColor(Theme.darkGreen)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .background(Color(hex: 0xEAF3EE))
            .clipShape(RoundedRectangle(cornerRadius: 14))

            PrimaryButton(title: "Confirm Memory", fullWidth: true) { viewModel.confirm() }
        }
    }

    private var remindPanel: some View {
        Panel(title: "Remind & Act", step: "4/4") {
            if viewModel.activeMemories.isEmpty {
                Text("Confirmed memories will appear here when they need attention.")
                    .font(.system(size: 15)).foregroundColor(Theme.muted)
            } else {
                HStack(spacing: 8) {
                    SummaryTile(label: "Overdue", count: viewModel.summary.overdue)
                    SummaryTile(label: "Due Today", count: viewModel.summary.dueToday)
                    SummaryTile(label: "Upcoming", count: viewModel.summary.upcoming)
                }
                ForEach(viewModel.activeMemories) { memory in
                    let status = MemoryStatusResolver.status(for: memory)
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(memory.title).font(.system(size: 17, weight: .black)).foregroundColor(Color(hex: 0x15201D))
                            Spacer()
                            StatusBadge(status: status)
                        }
                        Text("\(memory.eventType.label) on \(MemoryStatusResolver.formatDate(memory.dateISO))")
                            .font(.system(size: 14, weight: .bold)).foregroundColor(Color(hex: 0x40504A))
                        PrimaryButton(title: "Mark Complete", background: Theme.darkGreen) { viewModel.complete(memory.id) }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(14)
                    .background(Theme.field)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
            }
        }
    }

    private var completedPanel: some View {
        Panel(title: "Completed", step: "\(viewModel.completedMemories.count)") {
            ForEach(viewModel.completedMemories) { memory in
                VStack(alignment: .leading, spacing: 10) {
                    Text(memory.title)
                        .font(.system(size: 16, weight: .black))
                        .strikethrough()
                        .foregroundColor(Color(hex: 0x3C4A44))
                    Text("\(memory.eventType.label) on \(MemoryStatusResolver.formatDate(memory.dateISO))")
                        .font(.system(size: 14)).foregroundColor(Color(hex: 0x40504A))
                    Text("Delete")
                        .font(.system(size: 13, weight: .black)).foregroundColor(Color(hex: 0xB23D28))
                        .onTapGesture { viewModel.delete(memory.id) }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(14)
                .background(Color(hex: 0xEEF4EA))
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }
}

// MARK: - Reusable views

private struct Panel<Content: View>: View {
    let title: String
    let step: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(title).font(.system(size: 20, weight: .black)).foregroundColor(Color(hex: 0x16201D))
                Spacer()
                Text(step).font(.system(size: 12, weight: .black)).foregroundColor(Color(hex: 0x36564F))
            }
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Theme.panel)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Theme.panelBorder, lineWidth: 1))
    }
}

private struct PrimaryButton: View {
    let title: String
    var background: Color = Theme.accent
    var fullWidth: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: .black))
                .foregroundColor(Color(hex: 0xFFFDF7))
                .frame(maxWidth: fullWidth ? .infinity : nil)
                .padding(.horizontal, 16).padding(.vertical, 15)
        }
        .frame(maxWidth: fullWidth ? .infinity : nil)
        .background(background)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct SummaryTile: View {
    let label: String
    let count: Int

    var body: some View {
        VStack(spacing: 2) {
            Text("\(count)").font(.system(size: 24, weight: .black)).foregroundColor(Color(hex: 0x15201D))
            Text(label).font(.system(size: 12, weight: .bold)).foregroundColor(Theme.muted)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color(hex: 0xEAF3EE))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct StatusBadge: View {
    let status: MemoryStatus

    private var color: Color {
        switch status {
        case .upcoming: return Color(hex: 0xD9E9F5)
        case .dueToday: return Theme.gold
        case .overdue: return Color(hex: 0xF2B7A5)
        case .completed: return Color(hex: 0xCDE5C8)
        }
    }

    var body: some View {
        Text(status.label)
            .font(.system(size: 11, weight: .black))
            .foregroundColor(Color(hex: 0x15201D))
            .padding(.horizontal, 10).padding(.vertical, 6)
            .background(color)
            .clipShape(Capsule())
    }
}

private struct FieldLabel: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.system(size: 12, weight: .black)).foregroundColor(Color(hex: 0x4F5C57))
    }
}

// MARK: - Theme

private enum Theme {
    static let background = Color(hex: 0xF6F2E8)
    static let hero = Color(hex: 0x0E2A2B)
    static let accent = Color(hex: 0xD84F2A)
    static let darkGreen = Color(hex: 0x17312D)
    static let gold = Color(hex: 0xF3BE4E)
    static let panel = Color(hex: 0xFFFDF7)
    static let panelBorder = Color(hex: 0xE2D8C4)
    static let field = Color(hex: 0xF8F0DD)
    static let muted = Color(hex: 0x557068)
}

private extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}

// MARK: - Native image pickers

private struct ImagePicker: UIViewControllerRepresentable {
    let sourceType: UIImagePickerController.SourceType
    let onPicked: (UIImage?) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let controller = UIImagePickerController()
        controller.sourceType = UIImagePickerController.isSourceTypeAvailable(sourceType) ? sourceType : .photoLibrary
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker
        init(_ parent: ImagePicker) { self.parent = parent }

        func imagePickerController(
            _ picker: UIImagePickerController,
            didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
        ) {
            parent.onPicked(info[.originalImage] as? UIImage)
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}

private struct PhotoPicker: UIViewControllerRepresentable {
    let onPicked: (UIImage?) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> PHPickerViewController {
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = 1
        let controller = PHPickerViewController(configuration: config)
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: PhotoPicker
        init(_ parent: PhotoPicker) { self.parent = parent }

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            parent.dismiss()
            guard let provider = results.first?.itemProvider, provider.canLoadObject(ofClass: UIImage.self) else {
                parent.onPicked(nil)
                return
            }
            provider.loadObject(ofClass: UIImage.self) { object, _ in
                DispatchQueue.main.async { self.parent.onPicked(object as? UIImage) }
            }
        }
    }
}
