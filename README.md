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
- **Coil** - Image loading library

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
│   └── tasks/           # Task data layer (API, cache, repository)
├── feature/
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

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Contact

For questions or feedback, please open an issue on GitHub.

---

Built with ❤️ using Compose Multiplatform
