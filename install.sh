#!/bin/bash
# Knull CI/CD Easy Install Script with systemd
# Usage: curl -fsSL https://raw.githubusercontent.com/knullci/knull/main/install.sh | bash
# With custom port: curl -fsSL ... | bash -s -- --port 9090

set -e

REPO="knullci/knull"
BINARY_NAME="knull"
INSTALL_DIR="/opt/knull"
DATA_DIR="/var/lib/knull"
CONFIG_DIR="/etc/knull"
SERVICE_USER="knull"
DEFAULT_PORT=8080
IS_UPGRADE=false
BACKUP_CONFIG=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse arguments
PORT=$DEFAULT_PORT
SKIP_SERVICE=false
VERSION=""
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --port=*)
            PORT="${1#*=}"
            shift
            ;;
        --version)
            VERSION="$2"
            shift 2
            ;;
        --version=*)
            VERSION="${1#*=}"
            shift
            ;;
        --no-service)
            SKIP_SERVICE=true
            shift
            ;;
        *)
            shift
            ;;
    esac
done

echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║     Knull CI/CD Installer                    ║${NC}"
echo -e "${GREEN}║     Self-hosted CI/CD Pipeline Server        ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
echo ""

# Check if running as root or with sudo
if [ "$EUID" -ne 0 ]; then
    echo -e "${YELLOW}Note: This script needs sudo access to install system services${NC}"
    SUDO="sudo"
else
    SUDO=""
fi

# Detect OS and Architecture
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)

case "$ARCH" in
    x86_64)
        ARCH="x64"
        ;;
    aarch64|arm64)
        ARCH="arm64"
        ;;
    *)
        echo -e "${RED}Error: Unsupported architecture: $ARCH${NC}"
        exit 1
        ;;
esac

case "$OS" in
    linux)
        PLATFORM="linux-${ARCH}"
        ;;
    darwin)
        PLATFORM="darwin-${ARCH}"
        echo -e "${YELLOW}Note: systemd is not available on macOS. Skipping service setup.${NC}"
        SKIP_SERVICE=true
        ;;
    *)
        echo -e "${RED}Error: Unsupported OS: $OS${NC}"
        exit 1
        ;;
esac

echo -e "Platform: ${BLUE}${PLATFORM}${NC}"
echo -e "Port: ${BLUE}${PORT}${NC}"

# Check for existing installation (upgrade detection)
if [ -f "${CONFIG_DIR}/knull.conf" ]; then
    IS_UPGRADE=true
    echo -e "${YELLOW}Existing installation detected - running in upgrade mode${NC}"
    echo -e "${GREEN}Your existing configuration will be preserved${NC}"
fi

# Get version (use provided or fetch latest)
echo ""
if [ -n "$VERSION" ]; then
    # Remove 'v' prefix if provided
    VERSION="${VERSION#v}"
    echo -e "Using specified version: ${BLUE}v${VERSION}${NC}"
else
    echo "Fetching latest version..."
    VERSION=$(curl -s "https://api.github.com/repos/${REPO}/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v?([^"]+)".*/\1/')
    
    if [ -z "$VERSION" ]; then
        # Fallback: try to get the first release (including prereleases)
        echo -e "${YELLOW}No stable release found, checking for prereleases...${NC}"
        VERSION=$(curl -s "https://api.github.com/repos/${REPO}/releases" | grep '"tag_name":' | head -1 | sed -E 's/.*"v?([^"]+)".*/\1/')
    fi
    
    if [ -z "$VERSION" ]; then
        echo -e "${RED}Error: No releases found. Please specify a version with --version${NC}"
        exit 1
    fi
    
    echo -e "Version: ${BLUE}v${VERSION}${NC}"
fi

# Download bundled distribution (includes JRE)
BUNDLED_URL="https://github.com/${REPO}/releases/download/v${VERSION}/${BINARY_NAME}-${VERSION}-${PLATFORM}-bundled.tar.gz"
echo ""
echo "Downloading Knull CI (bundled with JRE)..."

TMP_DIR=$(mktemp -d)

download_bundled() {
    if curl -fsSL -o "${TMP_DIR}/knull-bundled.tar.gz" "$BUNDLED_URL" 2>/dev/null; then
        return 0
    fi
    return 1
}

download_jar() {
    JAR_URL="https://github.com/${REPO}/releases/download/v${VERSION}/${BINARY_NAME}-${VERSION}.jar"
    if curl -fsSL -o "${TMP_DIR}/knull.jar" "$JAR_URL" 2>/dev/null; then
        return 0
    fi
    return 1
}

INSTALL_TYPE=""

if download_bundled; then
    echo -e "${GREEN}Downloaded bundled distribution${NC}"
    INSTALL_TYPE="bundled"
else
    echo -e "${YELLOW}Bundled distribution not available, trying JAR...${NC}"
    if download_jar; then
        # Check if Java is installed
        if ! command -v java &> /dev/null; then
            echo -e "${YELLOW}Java not found. Installing OpenJDK 21...${NC}"
            if command -v apt-get &> /dev/null; then
                $SUDO apt-get update -qq
                $SUDO apt-get install -y -qq openjdk-21-jre-headless
            elif command -v yum &> /dev/null; then
                $SUDO yum install -y java-21-openjdk-headless
            elif command -v dnf &> /dev/null; then
                $SUDO dnf install -y java-21-openjdk-headless
            else
                echo -e "${RED}Error: Cannot install Java. Please install Java 21+ manually.${NC}"
                rm -rf "$TMP_DIR"
                exit 1
            fi
        fi
        echo -e "${GREEN}Downloaded JAR version${NC}"
        INSTALL_TYPE="jar"
    else
        echo -e "${RED}Error: Download failed${NC}"
        rm -rf "$TMP_DIR"
        exit 1
    fi
fi

# Create directories
$SUDO mkdir -p "$INSTALL_DIR"
$SUDO mkdir -p "$DATA_DIR"
$SUDO mkdir -p "$CONFIG_DIR"

# Backup existing configuration before upgrade
if [ "$IS_UPGRADE" = true ]; then
    echo "Backing up existing configuration..."
    BACKUP_CONFIG="/tmp/knull.conf.backup.$$"
    $SUDO cp "${CONFIG_DIR}/knull.conf" "$BACKUP_CONFIG"
    
    # Stop the service before upgrading
    if [ "$SKIP_SERVICE" = false ] && command -v systemctl &> /dev/null; then
        echo "Stopping Knull service for upgrade..."
        $SUDO systemctl stop knull 2>/dev/null || true
    fi
fi

# Install based on type
if [ "$INSTALL_TYPE" = "bundled" ]; then
    echo "Installing bundled distribution..."
    # Extract to temp location first
    $SUDO tar -xzf "${TMP_DIR}/knull-bundled.tar.gz" -C "${TMP_DIR}"
    EXTRACTED_DIR=$(tar -tzf "${TMP_DIR}/knull-bundled.tar.gz" | head -1 | cut -f1 -d"/")
    
    # Move contents to install directory
    $SUDO rm -rf "${INSTALL_DIR}"
    $SUDO mv "${TMP_DIR}/${EXTRACTED_DIR}" "${INSTALL_DIR}"
    
    # Create symlink
    $SUDO ln -sf "${INSTALL_DIR}/bin/knull" /usr/local/bin/knull
    
    EXEC_START="${INSTALL_DIR}/bin/knull --server.port=\${SERVER_PORT}"
    
elif [ "$INSTALL_TYPE" = "jar" ]; then
    echo "Installing JAR distribution..."
    $SUDO mv "${TMP_DIR}/knull.jar" "${INSTALL_DIR}/knull.jar"
    
    # Create launcher script
    $SUDO tee "${INSTALL_DIR}/bin/knull" > /dev/null << 'LAUNCHER'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KNULL_HOME="$(dirname "$SCRIPT_DIR")"
exec java -Xmx512m -jar "$KNULL_HOME/knull.jar" "$@"
LAUNCHER
    $SUDO mkdir -p "${INSTALL_DIR}/bin"
    $SUDO chmod +x "${INSTALL_DIR}/bin/knull"
    $SUDO ln -sf "${INSTALL_DIR}/bin/knull" /usr/local/bin/knull
    
    EXEC_START="/usr/bin/java -Xmx512m -jar ${INSTALL_DIR}/knull.jar --server.port=\${SERVER_PORT}"
fi

# Handle configuration
if [ "$IS_UPGRADE" = true ] && [ -n "$BACKUP_CONFIG" ] && [ -f "$BACKUP_CONFIG" ]; then
    echo "Restoring existing configuration..."
    $SUDO cp "$BACKUP_CONFIG" "${CONFIG_DIR}/knull.conf"
    rm -f "$BACKUP_CONFIG"
    echo -e "${GREEN}Configuration preserved from previous installation${NC}"
else
    # Create new config file (fresh install only)
    echo "Creating configuration..."
    $SUDO tee "$CONFIG_DIR/knull.conf" > /dev/null << EOF
# Knull CI/CD Configuration
# Generated by install script

# Server port
SERVER_PORT=${PORT}

# Data directory
DATA_DIR=${DATA_DIR}

# Workspace base path (shared with Necrosword executor)
# Using /var/lib for shared access between Knull and Necrosword
KNULL_WORKSPACE_BASE_PATH=/var/lib/knull-workspace
EOF
fi

# Create service user
if ! id "$SERVICE_USER" &>/dev/null; then
    $SUDO useradd -r -s /bin/false -d "$DATA_DIR" "$SERVICE_USER" 2>/dev/null || true
fi

# Set permissions
$SUDO chown -R "$SERVICE_USER:$SERVICE_USER" "$DATA_DIR"
$SUDO chown -R "$SERVICE_USER:$SERVICE_USER" "$INSTALL_DIR"
$SUDO chmod -R 755 "$INSTALL_DIR"

# Setup systemd service
if [ "$SKIP_SERVICE" = false ]; then
    echo ""
    echo "Setting up systemd service..."
    
    $SUDO tee /etc/systemd/system/knull.service > /dev/null << EOF
[Unit]
Description=Knull CI/CD Server
Documentation=https://github.com/knullci/knull
After=network.target

[Service]
Type=simple
User=${SERVICE_USER}
Group=${SERVICE_USER}
EnvironmentFile=${CONFIG_DIR}/knull.conf
ExecStart=${EXEC_START}
Restart=on-failure
RestartSec=10
WorkingDirectory=${DATA_DIR}

# Security settings
NoNewPrivileges=true
PrivateTmp=true

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=knull

[Install]
WantedBy=multi-user.target
EOF

    $SUDO systemctl daemon-reload
    $SUDO systemctl enable knull
    $SUDO systemctl start knull
    
    sleep 2
    
    if $SUDO systemctl is-active --quiet knull; then
        echo -e "${GREEN}Knull CI started successfully!${NC}"
    else
        echo -e "${YELLOW}Warning: Service may not have started correctly${NC}"
        echo -e "Check logs with: ${BLUE}sudo journalctl -u knull -f${NC}"
    fi
fi

# Cleanup
rm -rf "$TMP_DIR"

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
if [ "$IS_UPGRADE" = true ]; then
echo -e "${GREEN}║     Upgrade Complete!                        ║${NC}"
else
echo -e "${GREEN}║     Installation Complete!                   ║${NC}"
fi
echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
echo ""
echo -e "Access Knull CI at: ${BLUE}http://localhost:${PORT}${NC}"
echo ""
echo "Useful commands:"
echo -e "  ${BLUE}sudo systemctl status knull${NC}    - Check status"
echo -e "  ${BLUE}sudo systemctl restart knull${NC}   - Restart"
echo -e "  ${BLUE}sudo systemctl stop knull${NC}      - Stop"
echo -e "  ${BLUE}sudo journalctl -u knull -f${NC}    - View logs"
echo ""
echo -e "Configuration: ${BLUE}${CONFIG_DIR}/knull.conf${NC}"
echo ""
echo "To change the port, edit ${CONFIG_DIR}/knull.conf and restart:"
echo -e "  ${BLUE}sudo nano ${CONFIG_DIR}/knull.conf${NC}"
echo -e "  ${BLUE}sudo systemctl restart knull${NC}"
