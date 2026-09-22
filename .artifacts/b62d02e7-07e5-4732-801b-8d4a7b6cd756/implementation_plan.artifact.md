# Setup Clean Architecture and Firestore

This plan sets up a Clean Architecture directory structure (Data, Domain, Presentation) and configures Firebase Firestore so it can be easily injected and used throughout the project.

## User Review Required

> [!NOTE]
> For Dependency Injection (DI), this plan uses a **Manual DI** approach (`AppModule` singleton) to provide Firestore and Repositories. This is lightweight and doesn't require modifying Gradle files or adding Hilt/Dagger.

## Proposed Changes

### 1. Domain Layer
Contains the core business logic, models, and interfaces. It has no dependencies on the Android framework or Firestore.
#### [NEW] [School.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/domain/model/School.kt)
- A simple data class representing a domain model.
#### [NEW] [SchoolRepository.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/domain/repository/SchoolRepository.kt)
- Interface defining data operations (e.g., `getSchools()`, `addSchool()`).

---

### 2. Data Layer
Implements the domain repository interfaces and interacts with Firestore.
#### [NEW] [SchoolRepositoryImpl.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/data/repository/SchoolRepositoryImpl.kt)
- Implementation of `SchoolRepository` using `FirebaseFirestore`.

---

### 3. Dependency Injection (DI)
Provides instances of Firebase and Repositories to the rest of the app.
#### [NEW] [AppModule.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/di/AppModule.kt)
- A singleton object that initializes `Firebase.firestore` and provides `SchoolRepository`. This ensures you can access Firestore easily throughout the app.

---

### 4. Presentation Layer
Contains the UI (Jetpack Compose) and ViewModels.
#### [NEW] [SchoolViewModel.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/presentation/viewmodel/SchoolViewModel.kt)
- Uses `SchoolRepository` to fetch data and expose UI state.
#### [NEW] [SchoolViewModelFactory.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/presentation/viewmodel/SchoolViewModelFactory.kt)
- Factory to inject the repository into the ViewModel.
#### [NEW] [SchoolScreen.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/presentation/screen/SchoolScreen.kt)
- A sample Compose screen displaying the data.

---

### 5. App Entry Point
#### [MODIFY] [MainActivity.kt](file:///C:/Users/ik/AndroidStudioProjects/SchoolDataCollector/app/src/main/java/com/example/schooldatacollector/MainActivity.kt)
- Update to use `SchoolScreen` and pass the ViewModel using our `AppModule`.

## Verification Plan

### Manual Verification
- Build and run the app. It should compile successfully and show an empty list (or fetched data if you add documents to your Firestore "schools" collection).
- The project structure will be cleanly separated into `data`, `domain`, `presentation`, and `di`.