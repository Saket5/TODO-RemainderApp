# ReminderApp

A simple yet powerful reminder and to-do list application for Android. It allows users to create, manage, and receive notifications for their reminders, ensuring they never miss an important task.

## Features

- **Create, Edit, & Delete Reminders**: A full-featured CRUD interface for managing your reminders.
- **Flexible Scheduling**: Schedule reminders for a specific date and time.
- **Repeating Reminders**: Configure reminders to repeat hourly, daily, or at a custom minute-based interval.
- **Reliable Notifications**: Uses `AlarmManager` to deliver timely and accurate notifications, even when the app is closed or the device is idle.
- **Heads-Up Notifications**: Important reminders appear as heads-up notifications, even when the app is open.
- **Combined Task List**: Displays a unified list of personal reminders (stored locally) and tasks fetched from a remote API.
- **Modern UI**: A clean, intuitive user interface built with Google's Material Design components.
- **Accessibility**: Includes support for screen readers like TalkBack with descriptive content labels.

## Screenshots

*(This is a great place to add screenshots of your app!)*

| Home Screen | Add/Edit Screen |
| :---: | :---: |
| ![WhatsApp Image 2025-11-04 at 16 14 50_939f0601](https://github.com/user-attachments/assets/ca7b84bb-17fe-413a-a1bb-d8792b32a304)
** | *![WhatsApp Image 2025-11-04 at 16 14 50_4d8ccdad](https://github.com/user-attachments/assets/bf99cea1-b99c-431e-8938-b82c3cc92f6d) 
* |![WhatsApp Image 2025-11-04 at 16 14 50_b7111214](https://github.com/user-attachments/assets/45d302f8-2796-4d84-b204-acd66cb20ba6)


## Tech Stack & Architecture

- **Language**: 100% [Kotlin](https://kotlinlang.org/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI**: Android Views with [Material Components](https://material.io/develop/android) and ViewBinding.
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for managing background threads.
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local, persistent storage of reminders.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) for making API calls to a remote service.
- **Scheduling**: `AlarmManager` for precise and reliable alarm scheduling.
- **Dependency Injection**: Manual injection via ViewModelFactories.

## How To Build

1.  **Clone the repository**:
    ```sh
    git clone <your-repository-url>
    ```
2.  **Open in Android Studio**.
3.  Let Gradle download and sync the project dependencies.
4.  **Build and run** the app on an Android emulator or a physical device.
