# KnullCI – Lightweight CI/CD Platform

A modern, self-hosted CI/CD platform built with Spring Boot and Thymeleaf. Trigger builds from GitHub webhooks, execute build scripts, and monitor progress in real-time with live status and log updates.

## Features

- **GitHub Integration**: Webhook-triggered builds from push events
- **Real-time Monitoring**: SSE (Server-Sent Events) with polling fallback for live status & logs
- **Build Management**: 
  - View builds by job or all builds
  - Track build progress with status badges and step durations
  - Monitor full execution logs with auto-scroll
- **Job Configuration**: Define build steps in `.knull.yml` within your repository
- **Secure Credentials**: Encrypted storage for GitHub tokens and username/password auth
- **Modern UI**: Responsive Tailwind CSS design with side navigation

## Architecture

```
KnullCI
├── Spring Boot Backend
│   ├── Web Controller (builds, jobs, credentials, settings)
│   ├── Command Handlers (ExecuteBuild, CreateCredential)
│   ├── Build Executor (process orchestration)
│   └── JSON-based Storage (no database)
├── Thymeleaf Templates
│   ├── Build Detail (Overview, Steps, Logs, Pipeline)
│   ├── Login Page
│   ├── Job & Credential Management
│   └── Settings
└── Frontend (JavaScript)
    ├── SSE client for real-time updates
    ├── Polling fallback (1s interval)
    └── Live log auto-scroll
```

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **macOS / Linux** (tested on macOS)
- **Git**
- **GitHub Account** (for webhooks)

### Installation

#### Option A: Run with Java (Traditional)

1. **Clone the repository**
   ```bash
   git clone https://github.com/deepakraj5/knull-ci-cd.git
   cd knull-ci-cd
   ```

2. **Build the project**
   ```bash
   mvn -q -DskipTests package
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The server starts at `http://localhost:8080`

#### Option B: Native Executable (No Java Required)

1. **Install GraalVM**
   ```bash
   brew install --cask graalvm-jdk@21
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   ```

2. **Clone and build native executable**
   ```bash
   git clone https://github.com/deepakraj5/knull-ci-cd.git
   cd knull-ci-cd
   ./build-native.sh
   ```

3. **Run the native executable**
   ```bash
   ./target/knull
   ```

   The server starts at `http://localhost:8080`

**Benefits of native build**: 10x faster startup (~0.1s), 50% less memory, no Java runtime required.

See [NATIVE_BUILD.md](NATIVE_BUILD.md) for detailed native build instructions.

### First Login

- **Default Credentials**: `username: knull` / `password: knull`
- Change these in `DataSeeder.java` or via the database after first run
- After login, you'll be redirected to the builds dashboard

## Usage

### 1. Create a Job

1. Navigate to **Jobs** → **Create Job**
2. Fill in:
   - **Job Name**: e.g., "Motiv Server"
   - **Repository**: e.g., `deepakraj5/motiv-server`
   - **Branch**: default branch to watch
   - **Build Script**: path to `.knull.yml` in the repo (e.g., `.knull.yml` or `build/knull.yml`)
   - **Credentials**: select a stored credential for authentication

3. Click **Create Job**

### 2. Store Credentials

1. Navigate to **Credentials** → **Create Credential**
2. Choose credential type:
   - **GitHub Token**: For public/private repo access (recommended)
   - **Username/Password**: For git clone with credentials
3. Credentials are AES-256 encrypted and stored securely
4. Click **Create Credential**

### 3. Define `.knull.yml` Build Script

In your GitHub repository, create a `.knull.yml` file:

```yaml
name: Motiv Server Build
steps:
  - name: Lint Code
    run:
      tool: npm
      args: ["run", "lint"]
  
  - name: Run Tests
    run:
      tool: npm
      args: ["test"]
  
  - name: Build
    run:
      tool: npm
      args: ["run", "build"]
```

**Supported Tools**: `git`, `npm`, `mvn`, `docker`, `kubectl`

### 4. Trigger a Build

- **Option A**: Push to a monitored branch → GitHub webhook automatically triggers the build
- **Option B**: Click **Trigger Build** from the Jobs page
- Build starts asynchronously; navigate to the build detail page to monitor

### 5. Monitor Build Progress

#### Overview Tab
- Repository info (owner, branch, commit SHA/message)
- Build timeline (triggered by, started at, completed at, total duration)

#### Build Steps Tab
- Real-time step status badges (Pending → In Progress → Success/Failure)
- Step output (stdout/stderr)
- Error messages on failure
- Step durations

#### Logs Tab
- **Live-updating** complete build log
- Auto-scrolls to bottom while build is running
- Shows progress as each step executes

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=knull
spring.thymeleaf.cache=false
spring.web.resources.cache.period=0

github.api.base-url=https://api.github.com

# AES-256 encryption key for credentials (change in production!)
knull.encryption.secret-key=knull-default-secret-key-change-me-12345
```

### Environment Variables

Set `knull.encryption.secret-key` via environment variable for production:
```bash
export KNULL_ENCRYPTION_SECRET_KEY="your-secure-32-char-key-here"
mvn spring-boot:run
```

## Storage

KnullCI uses **JSON file storage** (no database):

- **Builds**: `storage/builds/1.json`, `storage/builds/2.json`, etc.
- **Jobs**: `storage/jobs/1.json`, etc.
- **Users**: `storage/users/1.json`
- **Credentials**: Encrypted and stored in JSON

To reset:
```bash
rm -rf storage/
```

## Real-time Updates

The UI auto-updates build status, step badges, and logs via:

1. **Server-Sent Events (SSE)** endpoint: `/builds/{id}/events`
   - Streams build object every 1 second while in progress
   - Auto-completes on SUCCESS/FAILURE

2. **Polling Fallback**: `/builds/{id}/status`
   - 1-second intervals if SSE unavailable
   - Automatically stops when build completes

Both methods fetch fresh state from storage each poll, ensuring the UI always reflects the current build progress.

## API Endpoints

### Builds
- `GET /builds` – List all builds
- `GET /builds/{id}/overview` – Build overview page
- `GET /builds/{id}/steps` – Build steps page
- `GET /builds/{id}/logs` – Build logs page
- `GET /builds/{id}/status` – JSON build status (for polling)
- `GET /builds/{id}/events` – SSE stream

### Jobs
- `GET /jobs` – List all jobs
- `POST /jobs/create` – Create a new job
- `GET /jobs/{id}` – View job details

### Credentials
- `GET /credentials` – List credentials
- `POST /credentials/create` – Store encrypted credential

### Settings
- `GET /settings` – Manage settings

### Webhooks
- `POST /api/v1/webhook/github` – GitHub push webhook (no auth required)

## Security

- ✅ **CSRF Protection**: Enabled (except GitHub webhook endpoint)
- ✅ **Credential Encryption**: AES-256 for tokens and passwords
- ✅ **Authenticated Pages**: All except `/login`, `/css/**`, `/api/v1/webhook/github`
- ✅ **Form Login**: Username/password with optional "remember me"

### Important
- Change `knull.encryption.secret-key` before deploying to production
- Use HTTPS in production
- Store credentials securely (use environment variables)
- Regularly rotate GitHub tokens

## Troubleshooting

### Build not starting
- Check job configuration: correct repository, branch, and build script path
- Verify credential is set and has proper access
- Check logs: `mvn spring-boot:run` output for errors

### Webhook not triggering
- Verify GitHub webhook is configured in repository settings
- Webhook URL: `https://your-domain/api/v1/webhook/github`
- Check GitHub webhook delivery logs for errors

### Logs not updating during build
- Ensure browser supports EventSource (SSE)
- Check browser console for JavaScript errors
- Polling should still update every 1 second as fallback

### Storage directory missing
- `storage/builds/` is created automatically on first build save
- If missing, builds may fail; verify file write permissions

## Development

### Project Structure
```
src/main/java/org/knullci/knull/
├── web/
│   ├── controller/      # HTTP endpoints
│   └── dto/             # Web DTOs
├── application/
│   ├── command/         # Command objects
│   ├── handler/         # Command handlers
│   ├── service/         # Business logic
│   └── interfaces/      # Contracts
├── domain/
│   ├── model/           # Domain entities
│   ├── enums/           # Build status, step status
│   └── repository/      # Repository interfaces
├── infrastructure/
│   ├── service/         # External integrations (GitHub, process runner)
│   ├── config/          # Configuration beans
│   └── security/        # Spring Security config
└── persistence/
    ├── repository/      # JSON file storage
    ├── entity/          # Persistence entities
    └── mapper/          # Entity mappers
```

### Build & Test
```bash
mvn clean package
mvn test
mvn spring-boot:run
```

## Roadmap

- [ ] Docker support for build containers
- [ ] Database backend (PostgreSQL, MySQL)
- [ ] Build artifact storage
- [ ] Slack/email notifications
- [ ] Build caching
- [ ] Multi-user teams
- [ ] Build history & analytics
- [ ] Webhook signing verification

## License

MIT

## Contributing

Contributions welcome! Please open an issue or submit a pull request.

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review the logs: `mvn spring-boot:run`
3. Open an issue on GitHub

---

**Built with ❤️ by [deepakraj5](https://github.com/deepakraj5)**
