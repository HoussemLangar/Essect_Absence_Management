# 📱 Gestion Absence Essect

An **Android application** developed in **Kotlin** for managing student absences within an institution.  
It integrates Firebase for authentication and database management, as well as Cloudinary for file handling.  

---

## 🚀 Features

- ✅ Authentication via Firebase (administration, agent & teachers)  
- 📊 Absence management and tracking  
- 👨‍🏫 Teacher interface: view absences  
- 🎓 Agent interface: add absences / view schedules  
- 🏫 Administration interface: add/view schedules, add/view absences, add/view teachers, add/view classes  
- ☁️ **Firebase** integration (auth, database, cloud)  
- 📷 Image storage & management with **Cloudinary**  

---

## 🛠️ Technologies Used

- [Kotlin](https://kotlinlang.org/) – Main language  
- [Android Studio](https://developer.android.com/studio) – IDE  
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) – Build system  
- [Firebase](https://firebase.google.com/) – Authentication & database  
- [Cloudinary](https://cloudinary.com/) – Media management  

---

## 📂 Project Structure

```
GestionAbsenceEssect/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/projet/gestionabsence/
│   │   │   ├── api/            # Cloudinary config
│   │   │   ├── controller/     # Business logic
│   │   │   ├── model/          # Models (Absence, Class, Teacher…)
│   │   │   └── ui/             # Activities & Fragments
│   │   └── res/                # Resources (layouts, drawables…)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## ⚙️ Installation & Setup

### 1. Clone the repo
```bash
git clone https://github.com/username/GestionAbsenceEssect.git
cd GestionAbsenceEssect
```

### 2. Open in Android Studio
- File → Open → Select the project folder  

### 3. Configure Firebase
- Place your `google-services.json` file inside the `app/` folder (already included in this project)  
- Ensure the project is linked to Firebase  

### 4. Run the app
- Select an emulator or a connected Android device  
- Click ▶ **Run**  

---

## 🔐 Requirements

- **Java 17** or higher  
- **Android Studio Giraffe or newer**  
- Internet connection (Firebase & Cloudinary)  

---

## 📄 License

This project is licensed under the MIT License – feel free to modify and use it.  

---

## 👤 Author

Developed by **Houssem LANGAR**  
📧 Email: houssemlangar3@gmail.com  
