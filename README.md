# Glaminator

Glaminator is an Android application designed to replace the GLAM service at Chung-Ang University (CAU). It provides a platform for students on campus to share tips, information, and stories with each other.

## Features

The current feature plan includes:
*   A blog system allowing users to create posts with text and photos.
*   A simple commenting system for users to engage with posts.

## Technical Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Backend:** Firebase Realtime Database
*   **Authentication:** Custom user / password authentication with Md5 hashing

### Notice on security

The project is intended for educational purposes only, and thus do not use firebase authentification by design.
Md5 password hashing is not secure enough for production applications and should be replaced in production environments.

## Project Structure

The project is organized into the following packages:

*   `ui`: Contains all the Composable functions that define the application's user interface. It's further divided into `screens`, `components`, and `theme`.
*   `viewmodel`: Holds the ViewModels responsible for managing UI-related data and business logic.
*   `repository`: Manages data operations, abstracting the data source (Firebase) from the rest of the application.
*   `model`: Defines the data classes that represent the application's data structures (e.g., `User`, `Post`).
*   `services`: Contains services for interacting with external systems, such as Firebase.
*   `utils`: Includes utility functions and helper classes used across the application.

### Creating a New Feature

Here's a quick guide to adding a new feature:

**1. Create a new Model and Repository:**

*   Define a new data class in the `model` package (e.g., `Post.kt`).
*   Create a corresponding repository in the `repository` package (e.g., `PostRepository.kt`). This repository will handle all data operations related to the new model, such as fetching, creating, and updating data in Firebase.

**2. Create a new UI Page:**

*   Create a new Composable function in the `ui/screens` package (e.g., `FeedScreen.kt`). This will be the main Composable for your new page.

**3. Create Custom Components:**

*   If your new page requires reusable UI elements, create them as Composable functions in the `ui/components` package.

## Team

*   [Nicolas Rodriguez Amaris]()
*   [Lieve Peerenboom]()
*   [Camilo Limones]()
*   [Mael RABOT](https://github.com/Mael-RABOT)

## License

This project is licensed under the terms of the [GPL v3 license](LICENSE).
