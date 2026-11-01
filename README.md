# Kudos

A modern Todo Application built with Compose Multiplatform, showcasing the latest technologies and best practices for cross-platform development.

## 🚀 Tech Stack

### Core Technologies
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** - Modern declarative UI framework for Android and iOS
- **[Metro](https://github.com/ZacSweers/metro)** - Compile-time dependency injection library for Compose Multiplatform
- **[Soil](https://github.com/soil-kt/soil)** - Server state management library inspired by [@tanstack/query](https://tanstack.com/query/latest)

### Backend & API
- **[Supabase](https://supabase.com/)** - Backend-as-a-Service for database and edge functions
- **[Ktor](https://ktor.io/)** - Asynchronous HTTP client for API communication
- **[Ktorfit](https://github.com/Foso/Ktorfit)** - Type-safe HTTP client using KSP

### Architecture & Libraries
- **Kotlin Multiplatform** - Share business logic across platforms
- **Kotlinx Serialization** - Type-safe JSON serialization
- **Kotlinx Coroutines** - Asynchronous programming
- **DataStore** - Type-safe data storage
- **Material 3** - Modern Material Design components
- **Navigation Compose** - Type-safe navigation
- **Coil** - Image loading library with network support
- **[Rin](https://github.com/takahirom/Rin)** - State retention library with `rememberRetained` for ViewModel-like functionality

## 🏗️ Project Structure

```
Kudos/
├── composeApp/           # Main application module
├── core/
│   ├── common/          # Common utilities and scope definitions
│   ├── datastore/       # DataStore configuration and providers
│   ├── design/          # Design system (colors, typography, theme)
│   ├── network/         # Network configuration and Ktorfit setup
│   └── soil/            # Soil query setup and fallback components
├── data/
│   ├── categories/      # Category data layer (API, cache, repository)
│   └── tasks/           # Task data layer (API, cache, repository)
├── feature/
│   ├── category/        # Category management UI
│   └── tasks/           # Task feature UI
└── build-logic/         # Gradle convention plugins
```

## 📱 Supported Platforms

- ✅ Android (API 24+)
- ✅ iOS (iOS 15+)

## 🔐 Architecture Highlights

### Dependency Injection with Metro
Metro provides compile-time DI with minimal runtime overhead:
```kotlin
@ContributesTo(DataScope::class)
interface NetworkGraph {
    @Provides
    @SingleIn(DataScope::class)
    fun provideKtorfit(httpClient: HttpClient): Ktorfit
}
```

### Server State with Soil
Soil manages server state with automatic caching and refetching:
```kotlin
@ContributesBinding(AppScope::class)
class DefaultTasksQueryKey @Inject constructor(
    private val apiClient: TasksApiClient,
    private val dataStore: TasksCacheDataStore,
) : QueryKey<List<TasksResponse.CategoryWithTasks>>
```

## Android builds with Bazel

Android APKs and JVM unit tests use Bazel; iOS continues to use Gradle. Install Bazelisk, JDK 21 or newer, and Android SDK `platforms;android-37.0` plus `build-tools;37.0.0` and `ndk;27.1.12297006`. Bazelisk reads the pinned version from `.bazelversion`.

Set the SDK location in the ignored `user.bazelrc`:

```text
common --repo_env=ANDROID_HOME=/path/to/Android/sdk
common --repo_env=ANDROID_NDK_HOME=/path/to/Android/sdk/ndk/27.1.12297006
```

```sh
bazel build //androidApp:app
bazel test //feature/tasks:all
bazel build //androidApp:app_release
adb install -r bazel-bin/androidApp/app.apk
```

APKs target arm64 by default. For an x86_64 emulator, add `--android_platforms=//tools:android_x86_64` to the build command. Both APK targets use the Bazel development signing key. Release enables R8 and resource shrinking; it is not a store-signed release. Optional `SUPABASE_URL` and `SUPABASE_ANON_KEY` values come from `local.properties`, with empty defaults. Never commit that file or generated configuration.

On macOS with Xcode 27, if host C compilation reports `SDKSettings.json` as an undeclared absolute include, add `common --repo_env=BAZEL_CONLYOPTS=-isystem:/path/to/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk` to `user.bazelrc`. Use the SDK root named in the error, including its unversioned symlink.

Library versions remain in `gradle/libs.versions.toml`. To update the Android artifact checksums and dependency graph after a dependency change:

```sh
./gradlew -I tools/export_android_dependencies.gradle :androidApp:exportBazelDependencies --no-configuration-cache
python3 tools/check_versions.py
```

The export uses Gradle's release runtime variants. Bazel downloads those exact files and preserves their dependency edges without resolving their POMs again. Compiler plugins and test tools use a separate Maven lock; after changing their declarations run `REPIN=1 bazel run @maven_tools//:pin`. Ordinary Bazel builds do not invoke Gradle.

For iOS, continue to use `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` or Xcode's existing Gradle build phase. The CI lint and Compose resource parity checks also continue to use Gradle.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Contact

For questions or feedback, please open an issue on GitHub.

---

Built with ❤️ using Compose Multiplatform
