# CricSaga — Product & Development Decisions

**Tagline:** Har Run Ki Kahani  
**Document purpose:** Working source of truth for development with Antigravity  
**Status:** Initial strategic decisions locked; implementation details remain to be specified  
**Last updated:** 2026-10-20

---

## 1. Product vision

CricSaga is a native Android cricket scoring app intended to be a low-cost alternative to CricHeroes, initially serving a small number of grassroots cricket teams and expanding based on feedback.

The product should make it easy to score matches, preserve match history, view player/team statistics, and continue scoring even when internet connectivity is unavailable.

### Brand direction

- **Working name:** CricSaga
- **Tagline:** Har Run Ki Kahani
- **Audience:** Grassroots cricket players, scorers, and teams
- **Initial platform:** Android
- **Brand style:** Red with charcoal/black in dark mode; red and white in light mode
- **Name status:** Working name only. Trademark, domain, social handle, and Play Store availability still need to be checked before committing publicly.

## 2. Locked decisions

### Decision 1 — MVP scope: Balanced MVP

The first release should include:

- User accounts
- Player profiles
- Teams and team membership
- Match setup
- Ball-by-ball scoring
- Scorecards
- Basic player and match statistics
- Historical-data import using scorecard screenshots and assisted extraction
- Free and Premium membership

Avoid expanding the initial MVP with costly or complex features such as live streaming, AI video analysis, and advanced tournament systems unless a later product decision explicitly adds them.

### Decision 2 — Technical architecture

**Android client**
- Kotlin
- Jetpack Compose for UI
- Room/SQLite for local persistence
- WorkManager or an equivalent reliable mechanism for background synchronization
- A repository-based data layer separating UI, local storage, and remote API access

**Backend**
- Node.js API
- PostgreSQL database
- Firebase Authentication for identity and sign-in

**Architecture principles**
- The Android app is offline-first for match scoring.
- Save scoring changes locally before attempting remote synchronization.
- The backend is the authority for shared data, permissions, and subscription entitlements.
- Design synchronization to be retry-safe and idempotent, so retries do not duplicate runs, wickets, or other scoring events.
- Keep the scoring rules and calculations testable independently from the UI.
- Keep the system modular enough that hosting providers or individual services can be changed later.

### Decision 3 — Membership model

- **Free:** A free tier for users.
- **Premium:** ₹99 per month per individual user.
- Each person manages their own subscription.
- Team-wide subscriptions are out of scope for the initial launch and may be considered later.
- Exact Premium feature entitlements are **not yet decided**.
- Subscription entitlement must be verified server-side. Do not trust a client-side flag alone.
- Before implementing payments, confirm applicable Google Play Billing requirements and test purchase flows using Google's supported test environment.

### Decision 4 — Historical-data import

**Chosen approach:** Screenshot-based assisted import.

Proposed flow:
1. User selects or uploads a scorecard screenshot they are authorized to use.
2. OCR or another extraction method identifies available match and player data.
3. CricSaga presents the extracted information for review.
4. User corrects errors and confirms the import.
5. Validated data is saved and clearly identified as imported historical data where appropriate.

Requirements:
- Never assume OCR is perfectly accurate.
- Validate totals, overs, wickets, player names, and other fields where possible.
- Do not silently import unreviewed OCR output.
- Do not promise full automatic migration from CricHeroes until a supported, authorized export or integration path is confirmed.
- Spreadsheet import may be added later.

### Decision 5 — Authentication and security

- Firebase Authentication handles user identity.
- The Node.js backend verifies Firebase ID tokens.
- The backend enforces authorization for users, teams, matches, and privileged actions.
- PostgreSQL stores application data.
- Never embed server secrets or privileged credentials in the Android app.
- Use HTTPS in deployed environments.
- Provide database backups and test the recovery process.
- Protect team and match data with server-side permission checks.
- Validate all incoming API data.
- Log useful operational errors without unnecessarily exposing credentials, tokens, or sensitive user data.
- Offline match data must survive app restarts and recover safely after interrupted synchronization.

### Decision 6 — Development and launch strategy

**Chosen approach:** Build the full MVP before organized external user testing.

This does **not** mean postponing automated or developer testing until the end. Test each feature throughout development, then run a structured beta with real teams after the complete MVP is ready.

Proposed release process:
1. Define requirements, user flows, and acceptance criteria.
2. Build and test features in small increments.
3. Complete the agreed MVP.
4. Run internal end-to-end and failure-recovery testing.
5. Beta test with approximately 5–10 cricket teams.
6. Fix critical bugs and repeat affected tests.
7. Prepare the Google Play release after checking current publishing and testing requirements.

## 3. Core scoring and reliability requirements

Scoring correctness is a central product requirement.

- Match state and scoring events must be persisted locally.
- The scorer must be able to continue scoring without internet.
- Closing or restarting the app must not lose a saved match.
- Synchronization retries must not create duplicate events.
- Corrections and undo must update the scorecard and dependent statistics consistently.
- Handle legal deliveries, wides, no-balls, byes, leg-byes, wickets, over transitions, innings transitions, and match completion according to the cricket rules and supported match format.
- Scoring logic should have automated unit tests covering normal cases and edge cases.
- Use a clear event/history model for deliveries and corrections so that match state can be reconstructed or recalculated reliably.
- Define how simultaneous edits from multiple devices are handled before supporting multi-device scoring of the same match.

**Open product detail:** The exact supported match formats, rules, tie/super-over handling, retirement/hurt behavior, player substitutions, and correction semantics still need to be specified in the PRD.

## 4. Proposed MVP development sequence

1. **Project foundation**
   - Set up Android Studio, Kotlin, Jetpack Compose, Git, and a physical Android device.
   - Create the app shell, navigation, theme, and basic screen structure.

2. **Local-first Android foundation**
   - Establish UI, ViewModel/state, repository, and Room layers.
   - Create initial local data models and database migrations.

3. **Accounts, players, and teams**
   - Integrate Firebase Authentication.
   - Build profiles, team creation, membership, and permissions.

4. **Match setup and scoring engine**
   - Define supported match formats and scoring rules.
   - Implement ball-by-ball events, scorecard calculations, undo/correction, and match resume.
   - Add unit tests before adding backend synchronization.

5. **Backend and synchronization**
   - Define the API contract and PostgreSQL schema.
   - Implement token verification, authorization, match persistence, idempotent sync, retry handling, and conflict behavior.

6. **Statistics and historical import**
   - Add basic statistics.
   - Implement screenshot upload, extraction, review, validation, and confirmed import.

7. **Premium and release readiness**
   - Finalize Premium entitlements.
   - Integrate and test Google Play billing and server-side purchase verification.
   - Add error reporting, privacy disclosures, and release configuration as required.

8. **Complete-MVP testing and beta**
   - Run automated tests and end-to-end scenarios.
   - Test offline scoring, app restarts, failed uploads, retries, and recovery.
   - Invite 5–10 teams to use the completed MVP in real matches.
   - Fix critical issues before public launch.

## 5. Development environment and testing constraints

The developer is working solo on a Linux laptop with an Intel i3 4th-generation processor and 8 GB RAM, plus a physical Android phone. The developer is new to Kotlin and Android development.

Consequences:
- Prefer testing on the physical Android phone instead of running a heavy emulator.
- Keep the development workflow lightweight.
- Learn Kotlin and Jetpack Compose while building real CricSaga features.
- Use Git commits frequently and keep a remote repository backup.
- Start with a local backend for development, then deploy it to a suitable hosting provider for multi-device and real-world testing.
- Do not select a backend hosting provider until cost, free-tier limits, sleep behavior, backups, and deployment requirements have been checked.

Suggested local device connection check:

```bash
adb devices
```

Enable Developer options and USB debugging on the Android phone, connect it with a data-capable USB cable, and authorize the computer when prompted.

## 6. Developer workflow

For each feature:
1. Define the smallest useful behavior and acceptance criteria.
2. Implement a small change.
3. Build and run it on the phone.
4. Test normal cases and relevant edge cases.
5. Add or update automated tests.
6. Fix regressions.
7. Commit the working code to Git.

Even though the organized external beta is planned after the full MVP, internal testing must happen continuously.

## 7. Live testing plan

After the complete MVP is ready:

- Recruit approximately 5–10 teams for a controlled beta.
- Have scorers run real matches from setup through completion.
- Compare CricSaga scorecards with a verified scorebook or other trusted record.
- Test airplane mode, reconnection, app restart, failed requests, retry behavior, corrections, and match resumption.
- Check that only authorized team members can modify shared data.
- Collect reproducible bug reports and prioritize scoring/data-loss/security problems above cosmetic issues.
- Repeat tests after fixes.
- Check current Google Play testing-track and publishing requirements for the developer account before release.

## 8. Decisions still open

Do not invent these choices during implementation. Propose options and request approval where they materially affect product behavior or cost.

1. **Premium feature boundaries:** Which features are Free and which require Premium?
2. **Scoring specification:** Supported formats, rules, extras, wickets, corrections, undo, tie-breakers, and match states.
3. **Backend hosting:** Node.js and PostgreSQL deployment provider and budget.
4. **Database schema and API contract:** Final entities, relations, endpoints, and authorization rules.
5. **Synchronization/conflicts:** How to handle the same match edited on multiple devices.
6. **OCR implementation:** Extraction provider/library, cost limits, supported screenshot layouts, and fallback flow.
7. **Payment implementation:** Billing flow, entitlement lifecycle, cancellation/expiry behavior, and server-side verification.
8. **Design specification:** Screen list, navigation, typography, components, and accessibility.
9. **Operational requirements:** Error reporting, analytics, backups, retention, privacy policy, and account/data deletion flows.
10. **Brand verification:** Trademark, domain, social handles, and Google Play name availability.
11. **Launch criteria:** Required automated test coverage, beta acceptance criteria, and critical-bug thresholds.

## 9. Guidance for Antigravity

Use this document as the current source of truth for product decisions.

When working on CricSaga:
- Read this document before proposing architecture or writing code.
- Preserve the locked decisions unless the developer explicitly changes them.
- Do not generate the entire app in one unreviewed step.
- Work in small, verifiable milestones.
- Explain Kotlin/Android concepts when they are introduced because the developer is new to this stack.
- Prefer maintainable, beginner-readable code over unnecessary abstraction.
- Include build/run instructions and tests with each meaningful milestone.
- Never claim a build or test passed unless it was actually run and verified.
- Flag unresolved decisions rather than silently choosing major product behavior.
- Prioritize scoring accuracy, offline reliability, data integrity, and security over decorative features.

**First implementation milestone:** Set up a minimal Kotlin + Jetpack Compose Android project, connect the physical phone, run the starter app, and commit the working baseline to Git. Do not start implementing the full scoring engine until the app builds and runs reliably.
