# lockmyapps Android — early prototype

This is a native Kotlin / Android Studio starter project for a daily app-limit blocker.

## Current prototype features
- Lists launchable apps installed on the device.
- Lets the user select apps and configure daily limits in minutes.
- Saves configuration locally.
- Includes setup shortcuts for Android Usage Access and Accessibility settings.
- Detects opening selected apps through an explicitly enabled Accessibility Service.
- Shows a lock screen when queried daily foreground usage meets the configured limit.

## Important limitations
This is an early prototype, not a production-ready or tamper-proof blocker. Android OEM behavior and usage-stat aggregation vary. Accessibility events can be delayed or stopped, and users can potentially bypass enforcement by disabling accessibility, revoking permissions, force-stopping/uninstalling the app, or using device settings/safe mode. The prototype does not yet have reboot recovery, robust foreground-usage accounting, device-admin / managed-device hardening, automated tests, or a Play Store policy review.

The app asks the user to explicitly enable Accessibility and Usage Access. Do not hide this behavior or misrepresent it. Accessibility usage must comply with Google Play policies if distributed.

## Build
1. Install Android Studio (current stable) and a JDK supported by that Android Studio release.
2. Open this folder as an existing project.
3. Allow Gradle sync to complete.
4. Run on an Android device (an emulator may not accurately reproduce app blocking).
5. Open lockmyapps, tap **Grant usage access**, find lockmyapps in system settings and enable usage access.
6. Return to lockmyapps, tap **Enable lockmyapps blocking service**, then explicitly enable the service in Accessibility settings.
7. Select apps, enter daily limits, and tap **Save limits**.

## Next engineering steps
1. Test usage totals against Android Digital Wellbeing across Android versions and OEMs.
2. Add reboot/time-change handling and persist lock state.
3. Replace usage-stat total polling with a tested accounting strategy and correct handling of foreground sessions.
4. Improve lock-screen task handling and test Home/Recents/app switching.
5. Consider managed-device / device-owner enforcement only for devices the user owns and deliberately enrolls.
6. Add tests, privacy disclosures, accessibility and Play policy review before distribution.
