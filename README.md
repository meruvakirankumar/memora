# Memora

Memora is a mobile-first Android and iOS app for turning real-world information into actionable memories.

Core loop:

```text
Capture -> Understand -> Identify -> Extract -> Confirm -> Remember -> Remind -> Act -> Complete
```

## Current Implementation

This repository currently contains an Expo React Native TypeScript app with the first usable Memora flow:

- Capture a new photo or select an existing image.
- Review recognized text in an OCR-ready field.
- Extract a structured memory candidate from labels such as `EXP 08/27`.
- Confirm or edit the title, event type, and action date.
- Store confirmed memories locally on the device.
- Schedule a local reminder for actionable future dates.
- Track upcoming, due today, overdue, and completed memory states.
- Mark a memory complete to stop future reminders.

OCR is represented by an editable recognized-text field in this first slice. The app is structured so a native OCR service can replace that field later without changing the rest of the confirmation and reminder loop.

## Run Locally

Install dependencies:

```bash
npm install
```

Start Expo:

```bash
npm start
```

Run on Android:

```bash
npm run android
```

Run on iOS:

```bash
npm run ios
```

## Product Intent

See [INTENT.md](INTENT.md) for the full product philosophy and decision rule.