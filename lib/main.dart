import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'services/car_automation_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 1. Demander les permissions Bluetooth (Android 12+)
  await [Permission.bluetoothConnect, Permission.bluetoothScan].request();

  // 2. Demander la permission de superposition pour permettre le lancement de Waze depuis l'arrière-plan
  if (await Permission.systemAlertWindow.isDenied) {
    await Permission.systemAlertWindow.request();
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final carService = CarAutomationService();

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Mini Auto Drive')),
        body: Center(
          child: ElevatedButton(
            onPressed: () async {
              await carService.startCarSequence();
            },
            child: const Text('Tester la séquence manuellement'),
          ),
        ),
      ),
    );
  }
}
