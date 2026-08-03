# Contributing to SwiftLink Enterprise

Thank you for your interest in contributing to **SwiftLink Enterprise**! We welcome contributions to improve backend performance, frontend UI/UX, security, monitoring, and system documentation.

---

## 🛠️ Development Setup

### Backend (Java 21 / Spring Boot 3.2)
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run tests:
   ```bash
   mvn clean test
   ```
3. Start local development server:
   ```bash
   mvn spring-boot:run
   ```

### Frontend (React 18 / Vite / TypeScript)
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start Vite development server:
   ```bash
   npm run dev
   ```

---

## 📋 Pull Request Process

1. **Fork & Branch**: Create a feature branch off `main` using a descriptive name:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Code Standards**:
   - Follow standard Java coding conventions and Lombok patterns.
   - Maintain unit test coverage for new backend services or filters.
   - Keep React components modular and typed using TypeScript interfaces.
3. **Commit Messages**: Write clear, concise commit messages summarizing your changes.
4. **Submit PR**: Open a Pull Request targeting `main` with a detailed description of your changes and test results.

---

## 🐞 Reporting Issues

If you encounter bugs, security issues, or performance bottlenecks:
- Open a GitHub Issue with clear steps to reproduce.
- Include environment details (Java version, OS, browser, Docker version).
