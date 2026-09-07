# Memora Product Intent

## Core Intent

Memora helps people capture important information from the real world, understand what it means, turn it into a useful memory, and act on it at the right time without manually organizing everything.

> You capture the moment. Memora remembers what matters and reminds you when it is time to act.

Short motto:

> Capture it. Understand it. Remember it. Act on it.

## Product Philosophy

Memora is not a photo storage app, OCR archive, note-taking app, or generic reminder tool. Its purpose is to close the loop from real-world information to timely action.

Every feature should support at least one part of this loop:

```text
REAL WORLD
    -> CAPTURE
    -> UNDERSTAND
    -> IDENTIFY
    -> EXTRACT
    -> CONFIRM
    -> REMEMBER
    -> REMIND
    -> ACT
    -> COMPLETE
```

If a feature does not help users capture, understand, remember, remind, act, or complete something, it should be questioned before being added.

## 1. Capture

Principle: Do not make the user type it.

The user encounters something important in the real world and captures it instead of manually entering details.

```text
See something important
    -> Take a photo or select an image
    -> Memora receives it
```

Human effort should start with capture, not data entry.

## 2. Understand

Principle: Figure out what the user is looking at.

Memora should not merely save the photograph. It should examine the information inside it.

```text
Image
    -> OCR
    -> Text
    -> Normalize
    -> Understand context
```

Example:

```text
EXP 08/27
```

Memora should understand that this is not just text. It likely means something becomes actionable in August 2027.

## 3. Identify

Principle: Know what actually matters.

A captured image may contain many dates and pieces of information. Memora must determine which information is meaningful and actionable for the user.

Example:

```text
MFG 09/25
BATCH 84921
EXP 08/27
```

Expected interpretation:

```text
MFG   -> informational
BATCH -> informational
EXP   -> actionable
```

Memora should not remember everything. It should identify what is worth remembering.

## 4. Extract

Principle: Turn information into something usable.

Once Memora understands the capture, it should convert the result into structured information.

Example memory candidate:

```text
Title: Milk
Event: Expiration
Date: August 2027
Confidence: High
```

The raw image and OCR text are inputs. The product value is the structured memory candidate.

## 5. Confirm

Principle: The user stays in control.

Memora should never assume its interpretation is automatically correct. It should show the user what it understood and ask for confirmation.

The user must be able to:

- Accept the interpretation.
- Change the title.
- Change the event type.
- Change the date.
- Resolve ambiguity.

The user is the final authority, especially when information is ambiguous.

## 6. Remember

Principle: The user should not have to think about it again.

Once confirmed, Memora stores the structured information as a memory.

```text
Captured information
    -> Confirmed information
    -> Memory
```

Memora should store the useful knowledge, not just the photograph or a pile of OCR text.

## 7. Remind

Principle: Tell the user when it matters.

A memory is only useful if it returns to the user at the right time.

Example:

```text
Expiry: September 20

September 19 -> Reminder
September 20 -> Due today
September 21 -> Overdue
```

Memora should not merely remind the user that something exists. It should remind them when they need to act.

## 8. Act

Principle: Turn memory into action.

The reminder is not the final product. Action is.

Example:

```text
Expiration
    -> Reminder
    -> User sees it
    -> User uses, returns, renews, pays, or replaces it
    -> Complete
```

Memora should help the user close the loop from memory to action.

## 9. Complete

Principle: Once handled, stop bothering the user.

When the user resolves the situation, the memory should move out of the active reminder flow.

```text
Overdue or upcoming
    -> Completed
    -> Future reminders stop
```

The system should understand that completed memories no longer require attention.

## Product Requirements Implied by the Intent

Memora should support:

- Image capture or image selection as the primary input path.
- OCR and text normalization.
- Context understanding for dates, labels, categories, and action signals.
- Actionable versus informational field detection.
- Structured memory candidate generation.
- User confirmation and correction before final storage.
- Memory storage focused on useful extracted knowledge.
- Reminder scheduling based on actionable dates.
- Status states such as upcoming, due today, overdue, and completed.
- Completion handling that stops future reminders.

## Product Boundaries

Memora should avoid becoming:

- A generic photo gallery.
- A passive OCR archive.
- A manual note-taking app.
- A reminder app that requires users to type and organize everything themselves.
- A feature collection that does not serve the capture-to-action loop.

## Decision Rule

Before adding a feature, ask:

Does this help the user capture something important, understand it, identify what matters, extract a useful memory, confirm it, remember it, get reminded at the right time, act on it, or complete it?

If the answer is no, the feature does not belong in Memora yet.