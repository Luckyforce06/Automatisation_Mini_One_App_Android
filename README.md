# appmini - MINI Car Automation

Une application Android / Flutter conçue pour automatiser les tâches d'infodivertissement et d'affichage lors de la connexion Bluetooth à un véhicule MINI.

## 🚀 Fonctionnalités

* **Détection Bluetooth automatique :** L'application écoute les événements de connexion/déconnexion Bluetooth de la voiture via un `BroadcastReceiver` en arrière-plan.
* **Gestion du mode conduite :**
  * Activation de la rotation automatique de l'écran.
  * Activation du mode Ne Pas Déranger (DND).
  * Ouverture des réglages réseau/5G si nécessaire.
* **Séquence média & navigation :**
  * Lancement automatique de Deezer et démarrage de la lecture audio.
  * Lancement automatique de Waze et maintien au premier plan par-dessus les autres applications.
* **Maintien de Waze au premier plan :** 
  * Écoute de l'état du téléphone (`TelephonyManager`) pour réafficher Waze automatiquement lorsqu'un appel sortant ou entrant est initié.
* **Gestion du volume sonore :**
  * Module d'accessibilité (`WazeAccessibilityService`) dédié pour l'atténuation du volume audio (ducking) selon l'activité de guidage.

---

## 🛠️ Technologies utilisées

* **Framework :** Flutter / Dart
* **Native Code :** Kotlin (Android Foreground Service, BroadcastReceiver, AccessibilityService)
* **Target OS :** Android (Optimisé pour Google Pixel / Android 14+)

---

## ⚙️ Configuration & Installation

### Prérequis
* Flutter SDK (Version 3.x ou supérieure)
* Android Studio / VS Code avec extensions Flutter & Dart
* Un appareil Android avec le mode développeur et le débogage USB activés

### Installation

1. **Cloner le projet :**
   ```bash
   git clone [[https://github.com/votre-utilisateur/appmini.git](https://github.com/votre-utilisateur/appmini.git)](https://github.com/Luckyforce06/Automatisation_Mini_One_App_Android)
   cd appmini
