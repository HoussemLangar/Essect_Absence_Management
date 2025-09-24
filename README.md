# 📱 Gestion Absence Essect

Une **application Android** développée en **Kotlin** permettant la gestion des absences au sein d’un établissement.  
Elle intègre Firebase pour l’authentification et la base de données, ainsi que Cloudinary pour la gestion des fichiers.

---

## 🚀 Fonctionnalités

- ✅ Authentification via Firebase (administration, agent & enseignants)  
- 📊 Gestion et suivi des absences  
- 👨‍🏫 Interface enseignant : visualisation des absences  
- 🎓 Interface agent : ajout des absences/ consultation des emplois
- 🎓 Interface administartion : ajout/consultation des emplois, ajout/consultation des absences, ajout/consultation des enseignants, ajout/consultation des classes
- ☁️ Intégration **Firebase** (auth, base de données, cloud)  
- 📷 Stockage et gestion des images avec **Cloudinary**  

---

## 🛠️ Technologies utilisées

- [Kotlin](https://kotlinlang.org/) – Langage principal  
- [Android Studio](https://developer.android.com/studio) – IDE  
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) – Build system  
- [Firebase](https://firebase.google.com/) – Authentification & base de données  
- [Cloudinary](https://cloudinary.com/) – Gestion des médias  

---

## 📂 Structure du projet

```
GestionAbsenceEssect/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/projet/gestionabsence/
│   │   │   ├── api/            # Config Cloudinary
│   │   │   ├── controller/     # Logique de contrôle
│   │   │   ├── model/          # Modèles (Absence, Classe, Enseignant…)
│   │   │   └── ui/             # Activités & Fragments
│   │   └── res/                # Ressources (layouts, drawables…)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## ⚙️ Installation & Exécution

### 1. Cloner le repo
```bash
git clone https://github.com/username/GestionAbsenceEssect.git
cd GestionAbsenceEssect
```

### 2. Ouvrir dans Android Studio
- File → Open → Sélectionner le dossier du projet  

### 3. Configurer Firebase
- Placer ton fichier `google-services.json` dans `app/` (il est déjà inclus pour ce projet)  
- Vérifier que le projet est bien lié à Firebase  

### 4. Lancer l’application
- Sélectionner un émulateur ou un smartphone Android connecté  
- Cliquer sur ▶ **Run**  

---

## 🔐 Configuration requise

- **Java 17** ou plus  
- **Android Studio Giraffe ou plus récent**  
- Connexion Internet (Firebase & Cloudinary)  

---

## 📄 Licence

Ce projet est sous licence MIT – libre à toi de le modifier et l’utiliser.  

---

## 👤 Auteur

Développé par **Houssem LANGAR**  
📧 Email : houssemlangar3@gmail.com  
