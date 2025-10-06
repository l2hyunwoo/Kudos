# Kudos

Compose Multiplatform으로 구축된 최신 Todo 애플리케이션으로, 크로스 플랫폼 개발을 위한 최신 기술과 이후 대세가 될 수 있는 Best Practice를 활용합니다.

## 🚀 기술 스택

### 핵심 기술
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** - Android와 iOS를 위한 최신 선언형 UI 프레임워크
- **[Metro](https://github.com/ZacSweers/metro)** - Compose Multiplatform에서 사용 가능한 컴파일 타임 의존성 주입 라이브러리
- **[Soil](https://github.com/soil-kt/soil)** - [@tanstack/query](https://tanstack.com/query/latest)에서 영감을 받은 서버 상태 관리 라이브러리

### 백엔드 & API
- **[Supabase](https://supabase.com/)** - 데이터베이스와 엣지 함수를 위한 BaaS
- **[Ktor](https://ktor.io/)** - 비동기 HTTP 클라이언트
- **[Ktorfit](https://github.com/Foso/Ktorfit)** - KSP를 사용한 타입 안전 HTTP 클라이언트

### 아키텍처 & 라이브러리
- **Kotlin Multiplatform** - 플랫폼 간 비즈니스 로직 공유
- **Kotlinx Serialization** - 타입 안전 JSON 직렬화
- **Kotlinx Coroutines** - 비동기 프로그래밍
- **DataStore** - 타입 안전 데이터 저장소
- **Material 3** - 최신 Material Design 컴포넌트
- **Coil** - 이미지 로딩 라이브러리

## 🏗️ 프로젝트 구조

```
Kudos/
├── composeApp/           # 메인 애플리케이션 모듈
├── core/
│   ├── common/          # 공통 유틸리티 및 스코프 정의
│   ├── datastore/       # DataStore 설정 및 프로바이더
│   ├── design/          # 디자인 시스템 (색상, 타이포그래피, 테마)
│   ├── network/         # 네트워크 설정 및 Ktorfit 구성
│   └── soil/            # Soil 쿼리 설정 및 폴백 컴포넌트
├── data/
│   └── tasks/           # 작업 데이터 레이어 (API, 캐시, 리포지토리)
├── feature/
│   └── tasks/           # 작업 기능 UI
└── build-logic/         # Gradle 컨벤션 플러그인
```

## 📱 지원 플랫폼

- ✅ Android (API 24+)
- ✅ iOS (iOS 15+)

## 🔐 아키텍처 하이라이트

### Metro를 활용한 의존성 주입
Metro는 최소한의 런타임 오버헤드로 컴파일 타임 DI를 제공합니다:
```kotlin
@ContributesTo(DataScope::class)
interface NetworkGraph {
    @Provides
    @SingleIn(DataScope::class)
    fun provideKtorfit(httpClient: HttpClient): Ktorfit
}
```

### Soil을 활용한 서버 상태 관리
Soil은 자동 캐싱 및 리페칭으로 서버 상태를 관리합니다:
```kotlin
@ContributesBinding(AppScope::class)
class DefaultTasksQueryKey @Inject constructor(
    private val apiClient: TasksApiClient,
    private val dataStore: TasksCacheDataStore,
) : QueryKey<List<TasksResponse.CategoryWithTasks>>
```

## 📊 데이터베이스 스키마

### Categories 테이블
- 카테고리 ID, 접두사, 색상, 제목
- 작업을 논리적으로 그룹화

### Projects 테이블
- 프로젝트 ID, 카테고리 ID, 제목, 설명
- Epic/Story 역할 수행

### Tasks 테이블
- 작업 ID (자동 생성: PREFIX-NUMBER 형식)
- 상태, 우선순위, 마감일
- 계층 구조 지원 (부모-자식 태스크)
- 프로젝트 및 카테고리 연결

## 📄 라이센스

이 프로젝트는 MIT 라이센스로 배포됩니다. 자세한 내용은 LICENSE 파일을 참조하세요.

## 🤝 기여

기여를 환영합니다! Pull Request를 자유롭게 제출해 주세요.

## 📞 문의

질문이나 피드백은 GitHub 이슈로 남겨주세요.

---

Compose Multiplatform으로 ❤️를 담아 제작되었습니다
