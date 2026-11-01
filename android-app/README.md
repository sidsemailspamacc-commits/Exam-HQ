# Exam HQ Alarms — Android

Native Android companion app for the November 2026 Exam HQ planner. The planner website in the repository root is left unchanged.

## Build an APK with GitHub Actions

1. Open the repository's **Actions** tab.
2. Select **Build Exam HQ Alarms APK**.
3. Open the latest successful run.
4. Under **Artifacts**, download **Exam-HQ-Alarms-debug-apk**.
5. Extract the downloaded artifact and install the APK on your Android phone.

The workflow also runs automatically when files inside `android-app/` are changed.

## Build locally

Open this `android-app` folder in Android Studio, allow Gradle sync, connect an Android phone, and run the app. Android Studio will fetch the Android Gradle Plugin and SDK components.

## App behavior

- Uses the original 17-day, 86-task November 2026 planner data in `app/src/main/assets/planner.json`.
- Each task is assigned an alarm time within its study block, in source order.
- A five-minute pre-alarm notification is scheduled for each task.
- Exact Android alarm scheduling, looping alarm audio, full-screen alarm screen, snooze choices (5/10/15 minutes), dismiss, and test alarm.
- Task completion is stored locally on the phone. No account, backend or internet permission is required.
- Upcoming alarms are restored after reboot or app update.

## Permissions and limitations

On first launch, grant notifications and exact-alarm access. On Android 14+, allow full-screen alarm notifications if the phone provides that setting. Android and device-maker policies can affect full-screen presentation, background execution, DND and alarm volume.

GitHub hosts the source and builds the APK; it does not run alarms. Install the generated APK on the phone for native alarms. Local progress is not synced between devices.

## Time assignment

The planner specifies study blocks, not a separate start time for each task. Tasks are divided evenly within each applicable block in original planner order. The planner's exam-morning quick-revision label does not specify a clock time, so those tasks are provisionally assigned to 6:00–6:30 AM.
