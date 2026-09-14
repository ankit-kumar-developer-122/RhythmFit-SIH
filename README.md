Team - Bharat Innovates

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

RhythmFit — "Fitness that adapts to your day, not the other way around."

Feature breakdown
* Core: Rhythm Engine — takes mood check-in + calendar state + context tags (heat/festival/exam/travel) → decides today's ask and adjusts the "consistency score" instead of resetting a streak to zero.
* Touchpoint 1 — Adaptive log: numbers (steps, workout minutes) or free-text/preset tags ("felt tired," "wedding today"). Missed days soften the score, they don't zero it.
* Touchpoint 2 — Micro-break nudges: 2–5 min prompts slotted into timetable/calendar gaps, with opt-in classroom/team leaderboards.
* Touchpoint 3 — Mood-linked movement: one-tap mood check → engine suggests a matched breathing/stretch micro-routine. Keep this strictly wellness-framed, never diagnostic — and it's good product practice (and scores well on responsibility) to add a quiet "talk to someone" resource link if someone logs sustained low mood for days, without making it a core feature.
  MVP vs. stretch (scope this honestly — feasibility is a judged criterion)
Build for the hackathon (MVP)	Roadmap / V2 (mention, don't demo)
Flexible log screen (numbers + tags), local-first	Google Calendar sync (auto-detect free slots)
Soft "consistency score" instead of streaks	Weather API + festival calendar for auto-context
Manual timetable grid → local notification nudges	On-device pose estimation (1–2 exercises, MediaPipe)
5-point mood check-in → rule-based routine suggestion	Opt-in classroom/team leaderboards
Simple dashboard (activity + mood over time)	Vernacular voice UI, wearable import
  Comparison table (for your Innovation & Uniqueness slide)
Capability	RhythmFit	Fit India App	Generic AI-coach/pose apps	Google Fit / Strava
Accepts qualitative entries, not just numbers	Yes	No	No	No
Soft/no-punish streaks	Yes	No	No	No
Adjusts to heat/festival/exam context	Yes	No	No	No
Micro-breaks tied to your actual timetable	Yes	No	No	Generic reminders only
Mood-linked movement suggestion	Yes	No	No	Limited/generic
Offline-first, low-end-phone friendly	Yes	Partial	Varies	Yes
  Tech stack 
* Frontend: Flutter — one codebase, runs fine on low-end Android, good offline plugins (sqflite/Hive for local DB, flutter_local_notifications for nudges).
* Local-first layer: SQLite on-device is the source of truth; background sync when online — this is your offline-first story.
* Backend: Node.js + Express; MongoDB (not MySQL) because your log entries are genuinely heterogeneous — numbers, tags, free text — and a document store fits that better than a rigid schema.
* Auth/push: Firebase Auth + FCM — fastest path to working notifications in a hackathon window.
* Rhythm Engine v1: a rules engine in the Node backend (mood + context → suggestion); pitch it as evolving into a lightweight classifier (logistic regression/decision tree) trained on usage data — that's your "AI" growth story without needing it live for demo.
* Stretch ML: TensorFlow Lite + MediaPipe Pose, on-device — keep it optional so a slow demo phone can't sink your prototype.
* Deploy for demo: Render/Railway free tier + GitHub Actions


Feasibility slide — risks worth naming yourself before judges do
Risk	Mitigation
No hard streaks → users disengage	Opt-in social accountability instead of punishment
Manual timetable entry is friction	One-time setup, editable, no calendar API dependency for MVP
Mood data is sensitive	Store locally by default, explicit consent for any cloud sync, no third-party sharing
On-device ML on cheap phones	Ship it as V2, never a core dependency
