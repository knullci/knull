# GraalVM Native Image Build Instructions

## Prerequisites

### 1. Install GraalVM
```bash
# macOS - Install GraalVM JDK 21
brew install --cask graalvm-jdk@21

# Or download manually from:
# https://www.graalvm.org/downloads/
```

### 2. Set JAVA_HOME
```bash
# Add to ~/.zshrc or ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH

# Reload shell
source ~/.zshrc
```

### 3. Verify Installation
```bash
java -version
# Should show: GraalVM...

native-image --version
# Should show: native-image version...
```

## Building Native Executable

### Quick Build (Recommended)
```bash
./build-native.sh
```

### Manual Build
```bash
# Clean and build
mvn clean

# Build native executable
mvn -Pnative -DskipTests package
```

Build time: **3-10 minutes** (first build is slower)

## Running the Native Executable

```bash
# Run directly
./target/knull

# Or with custom port
./target/knull --server.port=9090
```

Access at: `http://localhost:8080`

## Output

**Executable location**: `target/knull`  
**Expected size**: 80-150 MB (includes embedded runtime)  
**Startup time**: ~0.1 seconds (vs ~3 seconds for JVM)  
**Memory usage**: ~50% less than JVM

## Distribution

### Single Binary
```bash
# Copy executable to any macOS machine
cp target/knull ~/Desktop/knull-ci

# Run without Java installed
./knull-ci
```

### With Storage Directory
```bash
# Create distribution package
mkdir knull-ci-dist
cp target/knull knull-ci-dist/
cp -r storage knull-ci-dist/

# Zip for distribution
zip -r knull-ci-macos.zip knull-ci-dist/
```

## Platform-Specific Builds

GraalVM creates **platform-specific** binaries:
- Build on macOS → macOS executable
- Build on Linux → Linux executable  
- Build on Windows → Windows .exe

For cross-platform support, build on each target OS or use Docker multi-arch builds.

## Troubleshooting

### Build Fails with Reflection Errors
Spring Boot uses heavy reflection. If you encounter errors:

1. Run the app normally and enable the tracing agent:
```bash
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -jar target/knull-0.0.1-SNAPSHOT.jar
```

2. Exercise all features (create job, trigger build, view logs)

3. Stop the app (Ctrl+C)

4. Rebuild native image

### Out of Memory During Build
Increase heap size:
```bash
export MAVEN_OPTS="-Xmx8g"
mvn -Pnative package
```

### Missing Resources (Templates, CSS)
Resources are already configured in `resource-config.json`. If templates don't load:
- Verify files exist in `src/main/resources/templates/`
- Check console for resource loading errors

### Slow Startup
First run may be slower due to:
- Creating storage directories
- Initializing security
- Loading templates

Subsequent runs should be <0.2 seconds.

## Benefits vs Regular JAR

| Metric | JAR (JVM) | Native Image |
|--------|-----------|--------------|
| Startup | ~3 sec | ~0.1 sec |
| Memory | ~200 MB | ~100 MB |
| Package | 60 MB + JRE | 120 MB (standalone) |
| Runtime | Java required | No Java needed |

## Production Deployment

```bash
# Build optimized production binary
mvn -Pnative -DskipTests clean package

# Create systemd service (Linux)
sudo tee /etc/systemd/system/knull.service > /dev/null <<EOF
[Unit]
Description=KnullCI
After=network.target

[Service]
Type=simple
User=knull
WorkingDirectory=/opt/knull
ExecStart=/opt/knull/knull
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start
sudo systemctl enable knull
sudo systemctl start knull
```

## Notes

- Native image is **platform-specific** (cannot run macOS binary on Linux)
- Some reflection-heavy libraries may not work (already configured for KnullCI)
- Debugging requires `--debug-attach` flag during build
- AOT compilation = no runtime optimizations (JIT), but predictable performance
