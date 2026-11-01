# Exam HQ Alarms (Android)

This folder is reserved for the native Android companion app to the Exam HQ November 2026 planner.

## Source project

Use the Android Studio project archive generated in the ChatGPT conversation: `Exam-HQ-Alarms-Android-Project.zip`. Extract the contents so that this folder contains `settings.gradle`, `build.gradle`, `gradle.properties`, and the `app/` directory.

The app reads its planner data from `app/src/main/assets/planner.json`. Keep that data unchanged when updating the app.

## Build

Open this folder in Android Studio, allow Gradle sync, connect an Android phone, and run the app. The app needs notification and exact-alarm permissions. Full-screen alarm behavior is subject to Android version and device settings.

GitHub stores the source; it does not host or execute Android alarms. A native Android build is required to install the app. Local task completion is stored on the phone and does not sync through GitHub.

## Important

The November 2026 planner defines study blocks rather than a separate start time for every task. The app distributes tasks evenly within each applicable block in original order. Exam-morning revision is assigned to 6:00–6:30 AM because the planner does not specify an exact morning time.
