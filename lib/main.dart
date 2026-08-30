import 'package:flutter/material.dart';
import 'package:get_storage/get_storage.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await GetStorage.init();
  runApp(const DszSaveChatApp());
}

class DszSaveChatApp extends StatelessWidget {
  const DszSaveChatApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DSZ Save Chat',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        primaryColor: const Color(0xFF00E676),
        scaffoldBackgroundColor: const Color(0xFF0F172A),
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('DSZ Save Chat'),
        backgroundColor: const Color(0xFF1E293B),
      ),
      body: const Center(
        child: Text(
          'DSZ Save Chat - Anti-Delete Vault Active',
          style: TextStyle(fontSize: 18, color: Colors.white70),
        ),
      ),
    );
  }
}
