import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';

class CarAutomationService {
  static const platform = MethodChannel('com.example.car_automation/system');

  Future<void> startCarSequence() async {
    // 1. Rotation automatique
    try {
      await setAutoRotation(true);
    } catch (e) {
      print("Erreur rotation: $e");
    }

    // 2. Mode Ne Pas Déranger
    try {
      await setDoNotDisturb(true);
    } catch (e) {
      print("Erreur DND: $e");
    }

    // 3. Ouvrir Waze au premier plan
    final wazeUri = Uri.parse('waze://');
    if (await canLaunchUrl(wazeUri)) {
      await launchUrl(wazeUri, mode: LaunchMode.externalApplication);
    }

    // 4. Attendre 3 secondes que Waze soit prêt
    await Future.delayed(const Duration(seconds: 3));

    // 5. Déclencher la lecture audio globale au niveau du système Android
    try {
      await platform.invokeMethod('playAudio');
    } catch (e) {
      print("Erreur lecture audio: $e");
    }
  }

  Future<void> setDoNotDisturb(bool enabled) async {
    try {
      await platform.invokeMethod('setDoNotDisturb', {'enabled': enabled});
    } on PlatformException catch (e) {
      print("Erreur DND: ${e.message}");
    }
  }

  Future<void> setAutoRotation(bool enabled) async {
    try {
      await platform.invokeMethod('setAutoRotation', {'enabled': enabled});
    } on PlatformException catch (e) {
      print("Erreur rotation: ${e.message}");
    }
  }

  // Séquence exécutée à la déconnexion de la voiture
  Future<void> stopCarSequence() async {
    // 1. Mettre en pause la musique
    try {
      await platform.invokeMethod('playAudio'); // Ou pauseAudio si tu préfères
    } catch (e) {
      print("Erreur pause musique: $e");
    }

    // 2. Désactiver le mode Ne Pas Déranger
    try {
      await setDoNotDisturb(false);
    } catch (e) {
      print("Erreur DND: $e");
    }

    // 3. Désactiver la rotation automatique (ou remettre selon tes préférences)
    try {
      await setAutoRotation(false);
    } catch (e) {
      print("Erreur rotation: $e");
    }
  }
}
