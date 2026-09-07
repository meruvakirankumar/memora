import AsyncStorage from '@react-native-async-storage/async-storage';
import * as ImagePicker from 'expo-image-picker';
import * as Notifications from 'expo-notifications';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Image,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StatusBar as RNStatusBar,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

type EventType = 'EXPIRATION' | 'PAYMENT' | 'RENEWAL' | 'RETURN' | 'GENERAL';
type MemoryStatus = 'upcoming' | 'dueToday' | 'overdue' | 'completed';

type Memory = {
  id: string;
  title: string;
  eventType: EventType;
  dateISO: string;
  confidence: 'High' | 'Medium' | 'Low';
  sourceText: string;
  imageUri?: string;
  createdAt: string;
  completedAt?: string;
  notificationId?: string;
};

type MemoryCandidate = Omit<Memory, 'id' | 'createdAt' | 'completedAt' | 'notificationId'>;

const MEMORIES_KEY = 'memora.memories.v1';
const SAMPLE_OCR = 'MILK\nMFG 09/25\nBATCH 84921\nEXP 08/27';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldPlaySound: true,
    shouldSetBadge: false,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

export default function App() {
  const [imageUri, setImageUri] = useState<string>();
  const [sourceText, setSourceText] = useState(SAMPLE_OCR);
  const [candidate, setCandidate] = useState<MemoryCandidate>(() => extractCandidate(SAMPLE_OCR));
  const [memories, setMemories] = useState<Memory[]>([]);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    loadMemories();
    registerNotificationChannel();
  }, []);

  const activeMemories = useMemo(
    () =>
      memories
        .filter((memory) => getMemoryStatus(memory) !== 'completed')
        .sort((first, second) => {
          const urgencyDelta = statusUrgency(getMemoryStatus(first)) - statusUrgency(getMemoryStatus(second));
          return urgencyDelta !== 0 ? urgencyDelta : first.dateISO.localeCompare(second.dateISO);
        }),
    [memories],
  );

  const summary = useMemo(() => {
    return activeMemories.reduce(
      (totals, memory) => {
        totals[getMemoryStatus(memory)] += 1;
        return totals;
      },
      { overdue: 0, dueToday: 0, upcoming: 0, completed: 0 } as Record<MemoryStatus, number>,
    );
  }, [activeMemories]);

  const completedMemories = useMemo(
    () => memories.filter((memory) => getMemoryStatus(memory) === 'completed'),
    [memories],
  );

  async function loadMemories() {
    const stored = await AsyncStorage.getItem(MEMORIES_KEY);
    if (!stored) {
      return;
    }

    setMemories(JSON.parse(stored));
  }

  async function persistMemories(nextMemories: Memory[]) {
    setMemories(nextMemories);
    await AsyncStorage.setItem(MEMORIES_KEY, JSON.stringify(nextMemories));
  }

  async function captureImage() {
    const permission = await ImagePicker.requestCameraPermissionsAsync();

    if (!permission.granted) {
      Alert.alert('Camera permission needed', 'Memora needs camera access to capture real-world information.');
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.85,
    });

    if (!result.canceled) {
      setImageUri(result.assets[0].uri);
      setCandidate(extractCandidate(sourceText, result.assets[0].uri));
    }
  }

  async function selectImage() {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (!permission.granted) {
      Alert.alert('Photo permission needed', 'Memora needs photo access so you can select an existing image.');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.85,
    });

    if (!result.canceled) {
      setImageUri(result.assets[0].uri);
      setCandidate(extractCandidate(sourceText, result.assets[0].uri));
    }
  }

  function analyzeText() {
    setCandidate(extractCandidate(sourceText, imageUri));
  }

  async function confirmMemory() {
    if (!isValidISODate(candidate.dateISO)) {
      Alert.alert('Check the date', 'Use YYYY-MM-DD so Memora can remind you at the right time.');
      return;
    }

    setIsSaving(true);

    try {
      const notificationId = await scheduleReminder(candidate);
      const memory: Memory = {
        ...candidate,
        id: `${Date.now()}`,
        createdAt: new Date().toISOString(),
        notificationId,
      };

      await persistMemories([memory, ...memories]);
      setCandidate(extractCandidate(SAMPLE_OCR));
      setSourceText(SAMPLE_OCR);
      setImageUri(undefined);
    } finally {
      setIsSaving(false);
    }
  }

  async function completeMemory(memoryId: string) {
    const target = memories.find((memory) => memory.id === memoryId);

    if (target?.notificationId) {
      await Notifications.cancelScheduledNotificationAsync(target.notificationId);
    }

    await persistMemories(
      memories.map((memory) =>
        memory.id === memoryId
          ? {
              ...memory,
              completedAt: new Date().toISOString(),
            }
          : memory,
      ),
    );
  }

  async function deleteMemory(memoryId: string) {
    const target = memories.find((memory) => memory.id === memoryId);

    if (target?.notificationId) {
      await Notifications.cancelScheduledNotificationAsync(target.notificationId);
    }

    await persistMemories(memories.filter((memory) => memory.id !== memoryId));
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.keyboardView}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={styles.hero}>
            <Text style={styles.eyebrow}>Memora</Text>
            <Text style={styles.title}>Capture it. Understand it. Act on it.</Text>
            <Text style={styles.subtitle}>
              Turn real-world labels, receipts, renewals, and dates into confirmed memories with timely reminders.
            </Text>
          </View>

          <View style={styles.panel}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Capture</Text>
              <Text style={styles.sectionStep}>1/4</Text>
            </View>

            {imageUri ? (
              <Image source={{ uri: imageUri }} style={styles.previewImage} />
            ) : (
              <View style={styles.emptyPreview}>
                <Text style={styles.emptyPreviewTitle}>No image captured</Text>
                <Text style={styles.emptyPreviewText}>Use the camera or choose a product label, bill, document, or reminder-worthy image.</Text>
              </View>
            )}

            <View style={styles.actionRow}>
              <Pressable style={styles.primaryButton} onPress={captureImage}>
                <Text style={styles.primaryButtonText}>Take Photo</Text>
              </Pressable>
              <Pressable style={styles.secondaryButton} onPress={selectImage}>
                <Text style={styles.secondaryButtonText}>Select Image</Text>
              </Pressable>
            </View>
          </View>

          <View style={styles.panel}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Understand</Text>
              <Text style={styles.sectionStep}>2/4</Text>
            </View>

            <Text style={styles.fieldLabel}>Recognized text</Text>
            <TextInput
              multiline
              onChangeText={setSourceText}
              placeholder="OCR text appears here"
              style={styles.sourceInput}
              textAlignVertical="top"
              value={sourceText}
            />

            <Pressable style={styles.primaryButton} onPress={analyzeText}>
              <Text style={styles.primaryButtonText}>Analyze Memory</Text>
            </Pressable>
          </View>

          <View style={styles.panel}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Confirm</Text>
              <Text style={styles.sectionStep}>3/4</Text>
            </View>

            <Text style={styles.fieldLabel}>Title</Text>
            <TextInput
              onChangeText={(title) => setCandidate((current) => ({ ...current, title }))}
              style={styles.input}
              value={candidate.title}
            />

            <Text style={styles.fieldLabel}>Event</Text>
            <View style={styles.eventGrid}>
              {(['EXPIRATION', 'PAYMENT', 'RENEWAL', 'RETURN', 'GENERAL'] as EventType[]).map((eventType) => (
                <Pressable
                  key={eventType}
                  onPress={() => setCandidate((current) => ({ ...current, eventType }))}
                  style={[styles.eventChip, candidate.eventType === eventType && styles.eventChipSelected]}
                >
                  <Text style={[styles.eventChipText, candidate.eventType === eventType && styles.eventChipTextSelected]}>
                    {toTitleCase(eventType)}
                  </Text>
                </Pressable>
              ))}
            </View>

            <Text style={styles.fieldLabel}>Action date</Text>
            <TextInput
              onChangeText={(dateText) => setCandidate((current) => ({ ...current, dateISO: normalizeDateInput(dateText) }))}
              placeholder="YYYY-MM-DD"
              style={styles.input}
              value={candidate.dateISO}
            />

            <View style={styles.confirmSummary}>
              <Text style={styles.summaryLabel}>Confidence</Text>
              <Text style={styles.summaryValue}>{candidate.confidence}</Text>
              <Text style={styles.summaryLabel}>Status</Text>
              <Text style={styles.summaryValue}>{formatStatus(getCandidateStatus(candidate))}</Text>
            </View>

            <Pressable disabled={isSaving} style={[styles.primaryButton, isSaving && styles.disabledButton]} onPress={confirmMemory}>
              <Text style={styles.primaryButtonText}>{isSaving ? 'Remembering...' : 'Confirm Memory'}</Text>
            </Pressable>
          </View>

          <View style={styles.panel}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Remind & Act</Text>
              <Text style={styles.sectionStep}>4/4</Text>
            </View>

            {activeMemories.length === 0 ? (
              <Text style={styles.emptyListText}>Confirmed memories will appear here when they need attention.</Text>
            ) : (
              <>
                <View style={styles.summaryStrip}>
                  <View style={styles.summaryTile}>
                    <Text style={styles.summaryCount}>{summary.overdue}</Text>
                    <Text style={styles.summaryTileLabel}>Overdue</Text>
                  </View>
                  <View style={styles.summaryTile}>
                    <Text style={styles.summaryCount}>{summary.dueToday}</Text>
                    <Text style={styles.summaryTileLabel}>Due Today</Text>
                  </View>
                  <View style={styles.summaryTile}>
                    <Text style={styles.summaryCount}>{summary.upcoming}</Text>
                    <Text style={styles.summaryTileLabel}>Upcoming</Text>
                  </View>
                </View>

                {activeMemories.map((memory) => {
                  const status = getMemoryStatus(memory);

                  return (
                    <View key={memory.id} style={styles.memoryCard}>
                      <View style={styles.memoryCardHeader}>
                        <Text style={styles.memoryTitle}>{memory.title}</Text>
                        <View style={[styles.statusBadge, styles[status]]}>
                          <Text style={styles.statusText}>{formatStatus(status)}</Text>
                        </View>
                      </View>
                      <Text style={styles.memoryMeta}>{toTitleCase(memory.eventType)} on {formatDate(memory.dateISO)}</Text>
                      <Text style={styles.memorySource} numberOfLines={2}>{memory.sourceText}</Text>
                      <Pressable style={styles.completeButton} onPress={() => completeMemory(memory.id)}>
                        <Text style={styles.completeButtonText}>Mark Complete</Text>
                      </Pressable>
                    </View>
                  );
                })}
              </>
            )}
          </View>

          {completedMemories.length > 0 && (
            <View style={styles.panel}>
              <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle}>Completed</Text>
                <Text style={styles.sectionStep}>{completedMemories.length}</Text>
              </View>

              {completedMemories.map((memory) => (
                <View key={memory.id} style={styles.completedCard}>
                  <View style={styles.memoryCardHeader}>
                    <Text style={styles.completedTitle}>{memory.title}</Text>
                    <View style={[styles.statusBadge, styles.completed]}>
                      <Text style={styles.statusText}>Completed</Text>
                    </View>
                  </View>
                  <Text style={styles.memoryMeta}>{toTitleCase(memory.eventType)} on {formatDate(memory.dateISO)}</Text>
                  <Pressable style={styles.deleteButton} onPress={() => deleteMemory(memory.id)}>
                    <Text style={styles.deleteButtonText}>Delete</Text>
                  </Pressable>
                </View>
              ))}
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

async function registerNotificationChannel() {
  if (Platform.OS !== 'android') {
    return;
  }

  await Notifications.setNotificationChannelAsync('memora-reminders', {
    name: 'Memora Reminders',
    importance: Notifications.AndroidImportance.HIGH,
  });
}

async function scheduleReminder(candidate: MemoryCandidate) {
  const permission = await Notifications.requestPermissionsAsync();

  if (!permission.granted) {
    return undefined;
  }

  const actionDate = parseISODate(candidate.dateISO);
  const reminderDate = new Date(actionDate);
  reminderDate.setDate(reminderDate.getDate() - 1);
  reminderDate.setHours(9, 0, 0, 0);

  if (reminderDate.getTime() <= Date.now()) {
    return undefined;
  }

  return Notifications.scheduleNotificationAsync({
    content: {
      title: `${candidate.title} needs attention`,
      body: `${toTitleCase(candidate.eventType)} is coming up on ${formatDate(candidate.dateISO)}.`,
    },
    trigger: {
      type: Notifications.SchedulableTriggerInputTypes.DATE,
      date: reminderDate,
      channelId: 'memora-reminders',
    },
  });
}

function extractCandidate(text: string, imageUri?: string): MemoryCandidate {
  const normalizedText = text.trim();
  const lines = normalizedText.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
  const eventType = detectEventType(normalizedText);
  const dateISO = extractActionDate(normalizedText);
  const title = extractTitle(lines, eventType);

  return {
    title,
    eventType,
    dateISO,
    confidence: eventType !== 'GENERAL' && dateISO ? 'High' : dateISO ? 'Medium' : 'Low',
    sourceText: normalizedText,
    imageUri,
  };
}

function detectEventType(text: string): EventType {
  const upperText = text.toUpperCase();

  if (/\bEXP|EXPIR|BEST BY|USE BY\b/.test(upperText)) {
    return 'EXPIRATION';
  }

  if (/\bDUE|PAY|BILL|INVOICE\b/.test(upperText)) {
    return 'PAYMENT';
  }

  if (/\bRENEW|SUBSCRIPTION|POLICY\b/.test(upperText)) {
    return 'RENEWAL';
  }

  if (/\bRETURN|EXCHANGE\b/.test(upperText)) {
    return 'RETURN';
  }

  return 'GENERAL';
}

function extractActionDate(text: string) {
  const labeledDate = text.match(/(?:EXP|EXPIRES|EXPIRY|BEST BY|USE BY|DUE|PAY BY|RENEW(?:AL)?|RETURN BY)\D*(\d{1,2})[\/-](\d{1,2})(?:[\/-](\d{2,4}))?/i);

  if (labeledDate) {
    return datePartsToISO(labeledDate[1], labeledDate[2], labeledDate[3]);
  }

  const looseDate = text.match(/\b(\d{1,2})[\/-](\d{1,2})(?:[\/-](\d{2,4}))?\b/);

  if (looseDate) {
    return datePartsToISO(looseDate[1], looseDate[2], looseDate[3]);
  }

  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return toISODate(tomorrow);
}

function datePartsToISO(firstPart: string, secondPart: string, yearPart?: string) {
  const month = Number(firstPart);
  const secondNumber = Number(secondPart);
  const currentYear = new Date().getFullYear();
  const currentShortYear = currentYear % 100;
  const isLikelyMonthYear = !yearPart && secondNumber >= currentShortYear;
  const year = yearPart ? normalizeYear(Number(yearPart)) : isLikelyMonthYear ? normalizeYear(secondNumber) : currentYear;
  const day = yearPart || !isLikelyMonthYear ? secondNumber : lastDayOfMonth(year, month);
  const date = new Date(year, month - 1, day);

  if (!yearPart && !isLikelyMonthYear && startOfDay(date).getTime() < startOfDay(new Date()).getTime()) {
    date.setFullYear(date.getFullYear() + 1);
  }

  return toISODate(date);
}

function normalizeYear(year: number) {
  if (year < 100) {
    return 2000 + year;
  }

  return year;
}

function lastDayOfMonth(year: number, month: number) {
  return new Date(year, month, 0).getDate();
}

function extractTitle(lines: string[], eventType: EventType) {
  const candidateLine = lines.find((line) => !/\b(MFG|BATCH|LOT|EXP|DUE|PAY|RENEW|RETURN)\b/i.test(line));

  if (candidateLine) {
    return toTitleCase(candidateLine.replace(/[^a-z0-9 ]/gi, ' ').trim());
  }

  return `${toTitleCase(eventType)} memory`;
}

function getCandidateStatus(candidate: MemoryCandidate): MemoryStatus {
  return getStatus(candidate.dateISO);
}

function getMemoryStatus(memory: Memory): MemoryStatus {
  if (memory.completedAt) {
    return 'completed';
  }

  return getStatus(memory.dateISO);
}

function getStatus(dateISO: string): MemoryStatus {
  const today = startOfDay(new Date());
  const date = startOfDay(parseISODate(dateISO));

  if (date.getTime() < today.getTime()) {
    return 'overdue';
  }

  if (date.getTime() === today.getTime()) {
    return 'dueToday';
  }

  return 'upcoming';
}

function statusUrgency(status: MemoryStatus) {
  return { overdue: 0, dueToday: 1, upcoming: 2, completed: 3 }[status];
}

function parseISODate(dateISO: string) {
  const [year, month, day] = dateISO.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function isValidISODate(dateISO: string) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateISO)) {
    return false;
  }

  const parsedDate = parseISODate(dateISO);
  return toISODate(parsedDate) === dateISO;
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function toISODate(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function normalizeDateInput(dateText: string) {
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateText)) {
    return dateText;
  }

  return extractActionDate(dateText);
}

function formatDate(dateISO: string) {
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const date = parseISODate(dateISO);
  return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
}

function formatStatus(status: MemoryStatus) {
  if (status === 'dueToday') {
    return 'Due Today';
  }

  return toTitleCase(status);
}

function toTitleCase(value: string) {
  return value
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase());
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F6F2E8',
    paddingTop: Platform.OS === 'android' ? RNStatusBar.currentHeight ?? 24 : 0,
  },
  keyboardView: {
    flex: 1,
  },
  content: {
    gap: 18,
    padding: 20,
    paddingBottom: 36,
  },
  hero: {
    backgroundColor: '#0E2A2B',
    borderRadius: 24,
    padding: 24,
  },
  eyebrow: {
    color: '#F3BE4E',
    fontSize: 13,
    fontWeight: '800',
    letterSpacing: 0,
    marginBottom: 12,
    textTransform: 'uppercase',
  },
  title: {
    color: '#FFF8E8',
    fontSize: 32,
    fontWeight: '900',
    letterSpacing: 0,
    lineHeight: 36,
  },
  subtitle: {
    color: '#D8E8DE',
    fontSize: 16,
    lineHeight: 22,
    marginTop: 14,
  },
  panel: {
    backgroundColor: '#FFFDF7',
    borderColor: '#E2D8C4',
    borderRadius: 18,
    borderWidth: 1,
    gap: 14,
    padding: 16,
  },
  sectionHeader: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  sectionTitle: {
    color: '#16201D',
    fontSize: 21,
    fontWeight: '900',
  },
  sectionStep: {
    backgroundColor: '#EAF3EE',
    borderRadius: 999,
    color: '#36564F',
    fontSize: 12,
    fontWeight: '800',
    overflow: 'hidden',
    paddingHorizontal: 10,
    paddingVertical: 5,
  },
  previewImage: {
    aspectRatio: 4 / 3,
    borderRadius: 14,
    width: '100%',
  },
  emptyPreview: {
    alignItems: 'center',
    aspectRatio: 4 / 3,
    backgroundColor: '#EAF3EE',
    borderColor: '#C3D7CD',
    borderRadius: 14,
    borderStyle: 'dashed',
    borderWidth: 1,
    justifyContent: 'center',
    padding: 18,
  },
  emptyPreviewTitle: {
    color: '#17312D',
    fontSize: 18,
    fontWeight: '900',
    marginBottom: 8,
  },
  emptyPreviewText: {
    color: '#557068',
    fontSize: 14,
    lineHeight: 20,
    textAlign: 'center',
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
  },
  primaryButton: {
    alignItems: 'center',
    backgroundColor: '#D84F2A',
    borderRadius: 14,
    flex: 1,
    minHeight: 52,
    justifyContent: 'center',
    paddingHorizontal: 16,
  },
  primaryButtonText: {
    color: '#FFFDF7',
    fontSize: 15,
    fontWeight: '900',
  },
  secondaryButton: {
    alignItems: 'center',
    backgroundColor: '#17312D',
    borderRadius: 14,
    flex: 1,
    minHeight: 52,
    justifyContent: 'center',
    paddingHorizontal: 16,
  },
  secondaryButtonText: {
    color: '#FFFDF7',
    fontSize: 15,
    fontWeight: '900',
  },
  fieldLabel: {
    color: '#4F5C57',
    fontSize: 13,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  sourceInput: {
    backgroundColor: '#F8F0DD',
    borderColor: '#E6D7B8',
    borderRadius: 14,
    borderWidth: 1,
    color: '#15201D',
    fontSize: 16,
    lineHeight: 22,
    minHeight: 118,
    padding: 14,
  },
  input: {
    backgroundColor: '#F8F0DD',
    borderColor: '#E6D7B8',
    borderRadius: 14,
    borderWidth: 1,
    color: '#15201D',
    fontSize: 16,
    minHeight: 50,
    paddingHorizontal: 14,
  },
  eventGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  eventChip: {
    backgroundColor: '#ECE3D0',
    borderRadius: 999,
    paddingHorizontal: 13,
    paddingVertical: 9,
  },
  eventChipSelected: {
    backgroundColor: '#17312D',
  },
  eventChipText: {
    color: '#40504A',
    fontSize: 13,
    fontWeight: '800',
  },
  eventChipTextSelected: {
    color: '#FFFDF7',
  },
  confirmSummary: {
    backgroundColor: '#EAF3EE',
    borderRadius: 14,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    padding: 12,
  },
  summaryLabel: {
    color: '#557068',
    fontSize: 13,
    fontWeight: '800',
  },
  summaryValue: {
    color: '#17312D',
    fontSize: 13,
    fontWeight: '900',
    marginRight: 10,
  },
  disabledButton: {
    opacity: 0.55,
  },
  emptyListText: {
    color: '#557068',
    fontSize: 15,
    lineHeight: 22,
  },
  summaryStrip: {
    flexDirection: 'row',
    gap: 8,
  },
  summaryTile: {
    alignItems: 'center',
    backgroundColor: '#EAF3EE',
    borderRadius: 14,
    flex: 1,
    paddingVertical: 12,
  },
  summaryCount: {
    color: '#15201D',
    fontSize: 24,
    fontWeight: '900',
  },
  summaryTileLabel: {
    color: '#557068',
    fontSize: 12,
    fontWeight: '800',
    marginTop: 2,
  },
  memoryCard: {
    backgroundColor: '#F8F0DD',
    borderRadius: 14,
    gap: 10,
    padding: 14,
  },
  memoryCardHeader: {
    alignItems: 'flex-start',
    flexDirection: 'row',
    gap: 10,
    justifyContent: 'space-between',
  },
  memoryTitle: {
    color: '#15201D',
    flex: 1,
    fontSize: 18,
    fontWeight: '900',
  },
  statusBadge: {
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  upcoming: {
    backgroundColor: '#D9E9F5',
  },
  dueToday: {
    backgroundColor: '#F3BE4E',
  },
  overdue: {
    backgroundColor: '#F2B7A5',
  },
  completed: {
    backgroundColor: '#CDE5C8',
  },
  statusText: {
    color: '#15201D',
    fontSize: 12,
    fontWeight: '900',
  },
  memoryMeta: {
    color: '#40504A',
    fontSize: 14,
    fontWeight: '800',
  },
  memorySource: {
    color: '#5F6E68',
    fontSize: 13,
    lineHeight: 18,
  },
  completeButton: {
    alignItems: 'center',
    alignSelf: 'flex-start',
    backgroundColor: '#17312D',
    borderRadius: 999,
    minHeight: 42,
    justifyContent: 'center',
    paddingHorizontal: 16,
  },
  completeButtonText: {
    color: '#FFFDF7',
    fontSize: 13,
    fontWeight: '900',
  },
  completedCard: {
    backgroundColor: '#EEF4EA',
    borderRadius: 14,
    gap: 10,
    opacity: 0.9,
    padding: 14,
  },
  completedTitle: {
    color: '#3C4A44',
    flex: 1,
    fontSize: 17,
    fontWeight: '900',
    textDecorationLine: 'line-through',
  },
  deleteButton: {
    alignItems: 'center',
    alignSelf: 'flex-start',
    borderColor: '#C0463040',
    borderRadius: 999,
    borderWidth: 1,
    minHeight: 40,
    justifyContent: 'center',
    paddingHorizontal: 16,
  },
  deleteButtonText: {
    color: '#B23D28',
    fontSize: 13,
    fontWeight: '900',
  },
});
