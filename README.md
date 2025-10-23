🏨 Booking Training Spring Boot

A Spring Boot training project integrated with SonarCloud, JaCoCo, OWASP Dependency Check, and Spotless to ensure code quality, maintainability, and security.

🚀 Overview

This project simulates a simple Booking Management System, built using:

Spring Boot 3 (Java 21)

Maven for dependency and build management

SonarCloud for static code analysis

JaCoCo for test coverage reports

OWASP Dependency Check for vulnerability scanning

Spotless for automatic code formatting and style enforcement

🧱 Project Structure
src/
 ├── main/java/...        # main application source code
 ├── main/resources/      # configuration files (application.yml, templates, etc.)
 ├── test/java/...        # unit and integration tests
 └── pom.xml              # Maven configuration

⚙️ Tech Stack
Component	Description
☕ Java 21	Primary language
🌱 Spring Boot 3	Application framework
🧩 Maven	Build & dependency management
🧪 JaCoCo	Test coverage reports
🔒 OWASP Dependency Check	Security vulnerability analysis
🧽 Spotless	Code formatter and linter
☁️ SonarCloud	Code quality and static analysis
🐙 GitHub Actions	Continuous Integration (CI/CD) pipeline
▶️ How to Run Locally

1️⃣ Clone the repository

git clone https://github.com/costelmarianmereuta/booking-training-spring-boot.git
cd booking-training-spring-boot


2️⃣ Build and test

mvn clean verify


3️⃣ Run the application

mvn spring-boot:run


4️⃣ Run SonarCloud analysis manually

Only works if you have a valid SONAR_TOKEN:

mvn clean verify jacoco:report org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988:sonar \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=costelmarianmereuta \
  -Dsonar.projectKey=costelmarianmereuta_booking-training-spring-boot \
  -Dsonar.branch.name=main

🧹 Code Formatting — Spotless

Spotless ensures consistent code style across the project.

Command	Description
mvn spotless:check	Verifies formatting (used in CI)
mvn spotless:apply	Automatically fixes formatting locally

💡 The CI pipeline runs spotless:check.
If your code is not properly formatted, the build will fail.

🧪 Code Coverage (JaCoCo)

After running tests (mvn verify), open the coverage report:

target/site/jacoco/index.html


You’ll see class-level and method-level coverage metrics.

🔒 Dependency Security — OWASP

Detects known vulnerabilities (CVEs) in your dependencies.

Run locally:

mvn org.owasp:dependency-check-maven:check


The generated report can be found at:

target/dependency-check-report.html

☁️ SonarCloud Integration

SonarCloud provides advanced static analysis for:

🐛 Bugs and code smells

⚠️ Vulnerabilities

🔁 Code duplication

📊 Test coverage

📊 View dashboard:
SonarCloud Project Dashboard

🤖 CI/CD — GitHub Actions

The CI pipeline (defined in .github/workflows/ci.yml) automatically runs on every push or pull request.

Steps executed:

Checkout repository

Setup Java 21 (Temurin)

Run build & tests → mvn clean verify

Generate JaCoCo coverage report

Check code format with Spotless

Perform SonarCloud Quality Analysis

Run OWASP vulnerability scan

If any step fails ❌ → the build and pull request are blocked until fixed.

🛡️ Quality Gate (SonarCloud)

The project enforces a Quality Gate policy:

No new bugs or vulnerabilities

Minimum 80% test coverage on new code

No critical code smells or duplicated blocks

If the quality gate fails, the pipeline fails too.

🧠 Developer Tips

Run mvn spotless:apply before committing code.

Always run tests locally (mvn test) before pushing.

Use your personal SonarCloud token for local analysis.

Check the JaCoCo and OWASP reports before merging PRs.

📈 Badges (example placeholders)

You can add these badges at the top of the README:

![Build](https://github.com/costelmarianmereuta/booking-training-spring-boot/actions/workflows/ci.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=costelmarianmereuta_booking-training-spring-boot&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=costelmarianmereuta_booking-training-spring-boot)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=costelmarianmereuta_booking-training-spring-boot&metric=coverage)](https://sonarcloud.io/summary/new_code?id=costelmarianmereuta_booking-training-spring-boot)

📜 License

This project is distributed under the MIT License.
Feel free to use, modify, and share with proper attribution.

👨‍💻 Author

Costel Marian Mereuta
📧 costelmarianmereuta@gmail.com
