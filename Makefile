# Knull CI/CD Makefile

# Application variables
APP_NAME = knull
JAR_FILE = target/$(APP_NAME)-0.0.1-SNAPSHOT.jar
PID_FILE = .knull.pid
LOG_FILE = knull.log

# Java options
JAVA_OPTS ?= -Xms256m -Xmx512m

# Maven wrapper
MVN = ./mvnw

# Main targets
.PHONY: all build clean test run run-bg stop status logs help package dev

all: clean test package

## build: Compile the application (without packaging)
build:
	@echo "Building $(APP_NAME)..."
	$(MVN) compile

## package: Create production JAR (fat jar with all dependencies)
package:
	@echo "Packaging $(APP_NAME) for production..."
	$(MVN) package -DskipTests
	@echo "Production JAR created: $(JAR_FILE)"

## clean: Clean build artifacts
clean:
	@echo "Cleaning..."
	$(MVN) clean

## test: Run tests
test:
	@echo "Running tests..."
	$(MVN) test

## run: Run the application (foreground)
run: package
	@echo "Running $(APP_NAME)..."
	java $(JAVA_OPTS) -jar $(JAR_FILE)

## dev: Run in development mode with hot reload
dev:
	@echo "Running $(APP_NAME) in development mode..."
	$(MVN) spring-boot:run

## run-bg: Run the application in the background
run-bg: package
	@echo "Starting $(APP_NAME) in background..."
	@if [ -f $(PID_FILE) ]; then \
		PID=$$(cat $(PID_FILE)); \
		if kill -0 $$PID 2>/dev/null; then \
			echo "$(APP_NAME) is already running (PID: $$PID)"; \
			exit 1; \
		fi \
	fi
	@nohup java $(JAVA_OPTS) -jar $(JAR_FILE) > $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@sleep 2
	@if kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "$(APP_NAME) started with PID $$(cat $(PID_FILE))"; \
		echo "Logs are being written to $(LOG_FILE)"; \
	else \
		echo "Failed to start $(APP_NAME). Check $(LOG_FILE) for details."; \
		rm -f $(PID_FILE); \
		exit 1; \
	fi

## stop: Stop the background process
stop:
	@if [ -f $(PID_FILE) ]; then \
		PID=$$(cat $(PID_FILE)); \
		if kill -0 $$PID 2>/dev/null; then \
			echo "Stopping $(APP_NAME) (PID: $$PID)..."; \
			kill $$PID; \
			sleep 2; \
			if kill -0 $$PID 2>/dev/null; then \
				echo "Process didn't stop gracefully, forcing..."; \
				kill -9 $$PID 2>/dev/null || true; \
			fi; \
			rm -f $(PID_FILE); \
			echo "$(APP_NAME) stopped."; \
		else \
			echo "Process $$PID not running."; \
			rm -f $(PID_FILE); \
		fi \
	else \
		echo "No PID file found. $(APP_NAME) may not be running."; \
		echo "Looking for running Java processes..."; \
		pgrep -f "$(JAR_FILE)" || echo "No process found."; \
	fi

## restart: Restart the application
restart: stop run-bg

## status: Check if the application is running
status:
	@if [ -f $(PID_FILE) ]; then \
		PID=$$(cat $(PID_FILE)); \
		if kill -0 $$PID 2>/dev/null; then \
			echo "$(APP_NAME) is running (PID: $$PID)"; \
			echo "Uptime: $$(ps -o etime= -p $$PID 2>/dev/null || echo 'unknown')"; \
		else \
			echo "$(APP_NAME) is not running (stale PID file)"; \
		fi \
	else \
		echo "$(APP_NAME) is not running"; \
	fi

## logs: Tail the log file
logs:
	@if [ -f $(LOG_FILE) ]; then \
		tail -f $(LOG_FILE); \
	else \
		echo "No log file found."; \
	fi

## logs-all: View all logs
logs-all:
	@if [ -f $(LOG_FILE) ]; then \
		less $(LOG_FILE); \
	else \
		echo "No log file found."; \
	fi

## deps: Download dependencies
deps:
	@echo "Downloading dependencies..."
	$(MVN) dependency:resolve

## native: Build native image using GraalVM
native:
	@echo "Building native image..."
	$(MVN) -Pnative package -DskipTests

## docker: Build Docker image
docker:
	@echo "Building Docker image..."
	docker build -t $(APP_NAME):latest .

## help: Show this help
help:
	@echo "Knull CI/CD - Makefile Commands"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^## ' $(MAKEFILE_LIST) | sed -E 's/## /  /'
	@echo ""
	@echo "Environment Variables:"
	@echo "  JAVA_OPTS    JVM options (default: -Xms256m -Xmx512m)"
	@echo ""
	@echo "Examples:"
	@echo "  make package          # Build production JAR"
	@echo "  make run-bg           # Start in background"
	@echo "  make logs             # View live logs"
	@echo "  make stop             # Stop the application"
	@echo "  make restart          # Restart the application"
