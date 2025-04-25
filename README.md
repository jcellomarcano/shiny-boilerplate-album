# Album Viewer Android App

* [Album Viewer Android App](#album-viewer-android-app)

* [What is it?](#what-is-it)

* [How to Run it?](#how-to-run-it)

* [Key Decisions & Justifications](#key-decisions--justifications)

  * [1. Architecture: MVVM + Clean Architecture](#1-architecture-mvvm--clean-architecture)

  * [2. Data Persistence: Room Database](#2-data-persistence-room-database)

  * [3. Networking: Ktor Client](#3-networking-ktor-client)

  * [4. UI Toolkit: Jetpack Compose](#4-ui-toolkit-jetpack-compose)

  * [5. Dependency Injection: Hilt](#5-dependency-injection-hilt)

  * [6. Pagination: Paging 3 Library](#6-pagination-paging-3-library)

  * [7. Image Loading: Coil](#7-image-loading-coil)

  * [8. Data Flow & Modeling](#8-data-flow--modeling)

  * [9. UI Structure: Three Screens](#9-ui-structure-three-screens)

  * [`10. Gradle Build Management: libs.versions.toml`](#10-gradle-build-management-libsversionstoml)

  * [11. Handling Configuration Changes](#11-handling-configuration-changes)

* [Testing Strategy](#testing-strategy)

* [Conclusion](#conclusion)


## What is it?

This is a native Android application developed as part of a technical test. Its primary function is to display a list of albums and their associated items (images with titles) fetched from a remote JSON endpoint: `https://static.leboncoin.fr/img/shared/technical-test.json`.

![ezgif-767f0d944061da](https://github.com/user-attachments/assets/1d121718-221c-4837-8a77-4883c02c6923)


A key requirement was to implement an offline persistence system, ensuring data remains accessible even if the network is unavailable or the app is restarted. The app also handles a specific constraint for image loading, requiring a `User-Agent` header for image requests.

This project was approached as a professional development task, focusing on robust architecture, modern libraries, testability, and clear justifications for technical choices.

## How to Run it?

1. **Clone the Repository:** Obtain the source code from the provided Git repository.

2. **Open in Android Studio:** Open the project using a recent version of Android Studio (e.g., Koala, Iguana or later recommended) Canary for preference.
3. Have in mind the following **dependencies**:
```
agp = "8.11.0-alpha05"
kotlin = "2.1.20"
coreKtx = "1.16.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.10.1"
composeBom = "2025.04.01"
ktor = "3.1.2"
room = "2.7.1"
ksp = "2.1.20-2.0.0" # Before the '-' Need to match with kotlin version
hilt = "2.56.2"
pagingCommonAndroid = "3.3.6"
coil = "3.1.0"
pagingComposeAndroid = "3.3.6"
hiltNavigationCompose = "1.2.0"
navigationCompose = "2.8.9"
junitKtx = "1.2.1"
coroutinesTest = "1.10.2"
pagingTestingAndroid = "3.3.6"
hamcrest = "3.0"
androidxTestCore = "1.6.1"
runner = "1.6.2"
mockk = "1.14.0"
turbine = "1.2.0"
```

4. **Build:** Allow Gradle to sync and download dependencies. Build the project using `Build > Make Project` or by running the app directly.

5. **Run:** Deploy the application to an Android emulator or a physical device (minimum SDK 24).

## Key Decisions & Justifications

This section details the reasoning behind the architectural patterns, libraries, and implementation choices made during development.

### 1. Architecture: MVVM + Clean Architecture

* **Choice:** I adopted a combination of the Model-View-ViewModel (MVVM) pattern for the presentation layer and principles from Clean Architecture (separating concerns into Data, Domain, and Presentation layers).

* **Justification:**

  * **MVVM:** Provides excellent separation between the UI (View - Composables) and the business logic/state management (ViewModel). It leverages Android Jetpack components like `ViewModel` and `StateFlow` for lifecycle awareness and reactive UI updates, naturally handling configuration changes.

  * **Clean Architecture:** Enforces a strong separation of concerns, making the codebase more modular, testable, and maintainable.

    * **Presentation Layer:** Contains UI (Compose) and ViewModels. Knows about the Domain layer.

    * **Domain Layer:** Contains core business logic (Use Cases) and entity/model definitions (plain Kotlin classes). It defines repository interfaces but has no knowledge of specific data sources or frameworks (like Room or Ktor). This makes the core logic independent and reusable.

    * **Data Layer:** Contains Repository implementations, local data sources (Room DAO), remote data sources (Ktor ApiClient), and data mapping logic (DTOs, Entities). It handles the implementation details of data retrieval and storage.

  * **Benefits:** This layered approach enhances testability (each layer can be tested independently), improves code organization, and makes it easier to swap out implementations (e.g., change the network client or database) without affecting other layers significantly.

### 2. Data Persistence: Room Database

* **Choice:** I selected the Room Persistence Library, part of Android Jetpack.

* **Justification:**

  * **Requirement:** The test required offline data availability. A local database is the standard solution for structured data persistence on Android.

  * **Why Room:** Room provides an abstraction layer over SQLite, significantly reducing boilerplate code for database interactions. It offers compile-time verification of SQL queries, preventing runtime errors. Its seamless integration with Kotlin Coroutines and Flow (`PagingSource`, `Flow<List<T>>`) makes asynchronous data handling straightforward and fits perfectly with the reactive nature of the MVVM pattern. It also simplifies database migrations.

  * **Entities:** Room Entities (`AlbumEntity`, `ItemEntity`) define the database schema, representing the structure of the stored data.

### 3. Networking: Ktor Client

* **Choice:** I used Ktor Client for handling network requests to fetch the initial JSON data.

* **Justification:**

  * **Kotlin-First:** Ktor is a modern, Kotlin-first networking library developed by JetBrains. It integrates naturally with Kotlin features, especially Coroutines for asynchronous operations.

  * **Flexibility & Simplicity:** Ktor offers a clean and relatively simple API for making HTTP requests. Its plugin system (e.g., for `ContentNegotiation` with `kotlinx.serialization`, `UserAgent`) allows easy configuration.

  * **Serialization:** Used `kotlinx.serialization` for parsing the JSON response into Data Transfer Objects (DTOs - `AlbumResponseDTO`), ensuring type safety and easy integration with Ktor.

### 4. UI Toolkit: Jetpack Compose

* **Choice:** The entire UI was built using Jetpack Compose, Android's modern declarative UI toolkit.

* **Justification:**

  * **Modern Android Development:** Compose simplifies and accelerates UI development compared to the traditional View system.

  * **Declarative Approach:** Allows describing the UI state, and Compose handles the rendering and updates automatically when the state changes. This works exceptionally well with the MVVM pattern and reactive data streams (`StateFlow`, `PagingData`).

  * **Kotlin Integration:** Being Kotlin-based, it integrates seamlessly with the rest of the codebase.

  * **Reusability:** Encourages the creation of small, reusable UI components (like `AlbumGridItem`, `ItemGridItem`, `PaginatedLazyVerticalGrid`).

### 5. Dependency Injection: Hilt

* **Choice:** Hilt was used for managing dependency injection throughout the application.

* **Justification:**

  * **Maintainability & Testability:** DI frameworks like Hilt decouple classes from the responsibility of creating their own dependencies. This makes classes easier to manage, test (by providing mock dependencies), and reuse.

  * **Android Integration:** Hilt is specifically designed for Android, integrating smoothly with Android components like Activities, Fragments, ViewModels (`@HiltViewModel`), and the Application class (`@HiltAndroidApp`).

  * **Standardization:** Reduces boilerplate code associated with manual dependency injection or other DI frameworks like Dagger 2 (which Hilt builds upon). It provides standard annotations (`@Module`, `@Provides`, `@Binds`, `@Singleton`, etc.) for defining and scoping dependencies.

### 6. Pagination: Paging 3 Library

* **Choice:** The Paging 3 library from Android Jetpack was implemented for loading the lists of albums and items.

* **Justification:**

  * **Performance & Scalability:** Loading potentially large datasets (like the 5000 items in the JSON) all at once is inefficient and can lead to poor performance or `OutOfMemoryError`s. Paging 3 loads data in smaller chunks (pages) as needed, improving memory usage and app responsiveness.

  * **Integration:** It integrates well with Room (returning `PagingSource` directly from DAOs), Coroutines (`Flow<PagingData>`), and Jetpack Compose (`collectAsLazyPagingItems`).

  * **UI Handling:** Provides components and states (`LoadState`) to easily implement common pagination UI patterns like loading indicators and error/retry messages within lists (`PaginatedLazyVerticalGrid`).

### 7. Image Loading: Coil

* **Choice:** Coil (Coroutine Image Loader) was used for loading and displaying images from URLs.

* **Justification:**

  * **Kotlin & Coroutines:** Coil is a Kotlin-first library that leverages coroutines for efficient background loading and caching.

  * **Compose Integration:** Provides excellent Jetpack Compose integration (`rememberAsyncImagePainter`, `AsyncImage`).

  * **Performance:** Offers memory and disk caching, image sampling, and optimizations.

  * **Request Customization:** Easily allowed adding the required `User-Agent` header to image requests via its `ImageRequest.Builder`.

### 8. Data Flow & Modeling

* **Choice:** A clear data flow was established using distinct model types for different layers:

  * **`DTOs (AlbumResponseDTO):`** Represent the raw structure of the network response. Used only in the network client part of the Data layer.

  * **``Entities (AlbumEntity, ItemEntity):``** Represent the database table structure. Used by Room and the DAO within the Data layer.

  * **```Domain Models (Album, Item, AlbumDetails):```** Plain Kotlin classes representing the core business concepts. Used in the Domain layer (Use Cases, Repository Interface) and passed to the Presentation layer (ViewModel, UI).

  * **Repository Pattern:** An interface (`IAlbumRepository`) was defined in the Domain layer, and its implementation (`AlbumRepository`) resides in the Data layer. The repository acts as the single source of truth, coordinating between the local (Room) and remote (Ktor) data sources and performing the necessary data mapping.

  * **Use Cases:** Simple classes in the Domain layer that encapsulate specific actions (e.g., `GetAlbumPagingDataUseCase`, `RefreshAlbumsUseCase`), depending on the Repository interface. ViewModels depend on these Use Cases.

* **Justification:** This separation ensures:

  * **Decoupling:** Each layer works with models suited to its purpose, preventing network or database details from leaking into the Domain or UI.

  * **Clarity:** Makes the flow of data explicit and easier to follow.

  * **Testability:** Allows mocking repository interfaces or use cases for testing ViewModels and testing the repository's mapping logic independently.

### 9. UI Structure: Three Screens

* **Choice:** The UI was divided into three distinct screens managed by Jetpack Compose Navigation:

  1. `AlbumListScreen`: Displays a paginated grid of all albums.

  2. `ItemListScreen`: Displays a paginated grid of items belonging to a selected album.

  3. `ItemDetailScreen`: Displays the full image and title of a selected item.

* **Justification:** This structure provides a logical user flow. Starting with an overview (albums), drilling down into specifics (items within an album), and finally viewing the detail of a single item is a common and intuitive navigation pattern. Separating these into distinct screens with dedicated ViewModels keeps the UI logic focused and manageable.

### 10. Gradle Build Management: `libs.versions.toml`

* **Choice:** Dependencies and versions were managed using the `libs.versions.toml` file (Version Catalog).

* **Justification:** This is the modern, recommended approach for managing dependencies in Gradle. It centralizes version definitions, provides type-safe accessors in `build.gradle.kts` files, improves readability, and makes updating dependencies easier and less error-prone compared to defining them directly in build scripts.

### 11. Handling Configuration Changes

* **Approach:** Configuration changes (like screen rotation) are handled robustly using standard Jetpack practices:

  * **ViewModels:** UI-related state is held in `ViewModel`s, which survive configuration changes.

  * **StateFlow:** State is exposed from ViewModels using `StateFlow`, which retains the latest state for new subscribers (like the UI recomposing after rotation).

  * **`Paging 3 (cachedIn)`**: The `cachedIn(viewModelScope)` operator ensures that loaded `PagingData` is cached within the `ViewModelScope`, preventing data from being re-fetched unnecessarily on configuration changes.

  * **Hilt Scopes:** Using appropriate Hilt scopes (like `@Singleton` for repositories/data sources, `@HiltViewModel` for ViewModels) ensures dependencies have the correct lifecycle.

  * **`SavedStateHandle`**: Used in `ItemListViewModel` and `ItemDetailViewModel` to safely receive and retain navigation arguments (like `albumId` or `itemId`) across process death and configuration changes.

## Testing Strategy

Testing was a key consideration, following the layered architecture:

* **`Unit Tests (/test):`**

  * **ViewModels:** Tested logic, state transitions, and interaction with mocked Use Cases using JUnit4, MockK, `kotlinx-coroutines-test`, and Turbine.

  * **Use Cases:** Tested delegation to mocked Repository interfaces using JUnit4 and MockK.

  * **Repository:** Tested data mapping, Pager setup, and interaction with mocked DAO/ApiClient using JUnit4 and MockK.

* **`Instrumented Tests (/androidTest):`**

  * **`DAO (AlbumDaoTest):`** Tested database interactions (inserts, queries, paging sources, transactions) using an in-memory Room database, JUnit4, and AndroidX Test libraries.

  * **UI (Screens/Composables):** Basic structure set up using Jetpack Compose Test framework (`createAndroidComposeRule`, Hilt test rules) to verify UI rendering for different states and basic navigation interactions. Testing `LazyPagingItems` thoroughly requires more advanced setup.

This multi-layered testing approach ensures different parts of the application are verified correctly – logic via unit tests, database interactions via DAO tests, and UI rendering/interaction via UI tests.

## Conclusion

This project aimed to deliver a robust, performant, and maintainable Android application adhering to modern development practices and the specific requirements of the technical test. The chosen architecture (MVVM + Clean) and libraries (Compose, Room, Paging 3, Ktor, Hilt, Coil) provide a solid foundation for scalability and testability. Justification for each major decision was based on meeting requirements, leveraging Jetpack standards, and ensuring code quality suitable for a professional project.
