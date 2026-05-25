FaceMuse 🧖‍♀️✨
FaceMuse is a new-age android app that assists you with your AI skincare assistant. With real-time skin analysis and personalized routines, as well as progress tracking via a clean, fragment-based UI, it harnesses the power of the Google Gemini API to help ensure your complexion is always on point.

🚀 Features
Real-time Skin Analysis: A photograph followed by near-instant feedback from the Gemini Pro Vision AI model on skin issues.
Phone Authentication: Simple and secure Login flow with firebase Phone Auth.
History: Section for skin analysis history and tracking changes.
Great Recommendations: Product and routine suggestions curated by AI
Modern Architecture: Using Single-Activity, Multi-Fragment to improve performance.

🛠️ Tech Stack
Language: Kotlin (100%)
AI Engine: Google Gemini API
Backend: Firebase (Auth + Analytics + Database)
Data Persistence: Room, DataStore and SharedPreferences
Image Processing: CameraX

📂 Project Structure
app/src/main/java/com/example/facemuse/
├── fragments/       # Home, History, and Profile logic
├── activities/      # Main entry point and Auth flow
├── repository/      # Gemini and Firebase data handling
└── models/          # Data classes for AI responses and messages
🔐 Setup & Security
This project uses a local. properties file for managing API keys.
Get Started with a Gemini API Key from Google AI Studio
Add GEMINI_API_KEY=your_key_here to your local. properties.
This includes sensitive files for projects like google-services. json to prevent security leaks.
