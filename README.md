# KnullCI – The Open Source CI/CD Platform

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green.svg)
![GraalVM](https://img.shields.io/badge/GraalVM-Native-orange)

> **A modern, lightweight, and completely open-source CI/CD platform built for performance and simplicity.**

KnullCI is designed to provide developers with a robust, self-hosted alternative to heavy CI systems. Built with **Java 21** and **Spring Boot**, it offers lightning-fast build executions, real-time monitoring, and a beautiful UI powered by **Thymeleaf** and **Tailwind CSS**.

---

## 🚀 Why KnullCI?

- **Open Source at Heart**: Fully open code, zero vendor lock-in, and community-driven.
- **Blazing Fast**: Supports **native compilation** with GraalVM for <0.1s startup times.
- **Modern Stack**: Leveraging the latest in Java ecosystem—Spring Boot 4, gRPC, and Virtual Threads (Project Loom ready).
- **Secure by Default**: AES-256 encryption for all credentials and tokens.
- **Beautiful UX**: detailed build logs, live streaming updates, and a clean, dark-mode friendly interface.

## ✨ Key Features

- **GitHub Integration**: Seamless webhook integration triggers builds on push.
- **Pipeline as Code**: Define builds in a simple `.knull.yml` file within your repo.
- **Real-Time Visibility**: Live log streaming via SSE (Server-Sent Events).
- **Native Efficiency**: Run as a standalone native binary for minimal resource footprint.
- **Artifact Management** (Coming Soon): Store and retrieve build artifacts.

---

## 🛠️ Technology Stack

We believe in using the best tools for the job to ensure stability and speed.

- **Core**: Java 21, Spring Boot 4.0.1
- **Communication**: gRPC (v1.77.0) for internal services
- **Frontend**: Thymeleaf, Tailwind CSS, Vanilla JS
- **Build Tools**: Maven, GraalVM
- **Security**: Spring Security, AES-256

---

## ⚡ Getting Started

### Quick Install (Recommended)

One-line install for Ubuntu/Debian with automatic systemd service setup:

```bash
curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | sudo bash
```

This will:
- Download the latest native binary
- Install to `/usr/local/bin/knull`
- Create systemd service
- Start Knull CI on port 8080
- Enable auto-start on boot

#### Custom Port

```bash
curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | sudo bash -s -- --port 9090
```

#### Specific Version (Including Beta/RC)

```bash
curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | sudo bash -s -- --version 0.0.1-beta
```

#### Install Without Service (Manual Control)

```bash
curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | sudo bash -s -- --no-service
```

#### All Options Combined

```bash
curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | sudo bash -s -- --version 1.0.0 --port 9090
```

### Service Management

```bash
# Check status
sudo systemctl status knull

# Stop/Start/Restart
sudo systemctl stop knull
sudo systemctl start knull
sudo systemctl restart knull

# View logs
sudo journalctl -u knull -f

# Change port after installation
sudo nano /etc/knull/knull.conf   # Edit SERVER_PORT=9090
sudo systemctl restart knull
```

### Manual Installation

#### Prerequisites

- **Java 21+** (for JAR version)
- **Maven 3.8+** (for building from source)
- **Git**

#### From Source

```bash
git clone https://github.com/knullci/knull.git
cd knull
mvn clean package -DskipTests
java -jar target/knull-*.jar --server.port=8080
```

#### Native Binary (GraalVM)

```bash
# Download from releases
curl -L -o knull https://github.com/knullci/knull/releases/latest/download/knull-linux-amd64
chmod +x knull
sudo mv knull /usr/local/bin/

# Run
knull --server.port=8080
```

Visit `http://localhost:8080` to see KnullCI in action.

---

## 📖 Usage Guide

1.  **Login**: Default credentials are `knull` / `knull`.
2.  **Add a Credential**: Go to **Credentials**, add your GitHub Token (encrypted securely).
3.  **Create a Job**: Link a GitHub repository and point to your `.knull.yml`.
4.  **Trigger**: Push code or click "Trigger Build".

### Example `.knull.yml`

```yaml
name: Production Build
steps:
  - name: Install dependencies
    run:
      tool: npm
      args: ["install"]
  - name: Run Tests
    run:
      tool: npm
      args: ["test"]
```

---

## 🤝 Contributing

We love contributions! KnullCI is an open project and we welcome help to make it better.

1.  **Fork** the repository.
2.  **Create** a feature branch (`git checkout -b feature/amazing-feature`).
3.  **Commit** your changes.
4.  **Push** to the branch.
5.  **Open** a Pull Request.

Please read our [CONTRIBUTING.md](CONTRIBUTING.md) (coming soon) for details on our code of conduct and development process.

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Built with ❤️ for the Open Source Community
</p>
