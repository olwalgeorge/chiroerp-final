# ChiroERP Developer Setup Guide

**Quick Start**: Get from zero to running code in **< 30 minutes**.

> Current infra baseline for scaffold/pre-implementation stage: see `docs/MVP-INFRA-PREIMPLEMENTATION.md`.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Setup](#quick-setup)
- [Manual Setup](#manual-setup)
- [Verify Installation](#verify-installation)
- [Next Steps](#next-steps)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Tool | Version | Download | Purpose |
|------|---------|----------|---------|
| **Java** | 21 (LTS) | [Eclipse Adoptium](https://adoptium.net/) | Runtime & compilation |
| **Docker Desktop** | Latest | [Docker](https://www.docker.com/products/docker-desktop) | Local services (databases, Kafka, Redis) |
| **Git** | Latest | [Git](https://git-scm.com/downloads) | Version control |

### Recommended Software

| Tool | Purpose |
|------|---------|
| **IntelliJ IDEA** (Ultimate) | Java/Kotlin IDE with excellent Quarkus support |
| **VS Code** | Lightweight editor with Kotlin extensions |
| **Postman** | API testing |
| **pgAdmin** | Database management (included in Docker stack) |

### System Requirements

- **OS**: Windows 10/11, macOS 11+, or Linux (Ubuntu 20.04+)
- **RAM**: 16 GB minimum, 32 GB recommended
- **Disk**: 50 GB free space
- **CPU**: 4 cores minimum, 8 cores recommended

---

## Quick Setup

### Option 1: Automated Setup Script (Recommended)

**Windows** (PowerShell):
```powershell
# Clone the repository
git clone https://github.com/your-org/chiroerp.git
cd chiroerp

# Run setup script
.\scripts\dev-setup.ps1
```

**What the script does**:
1. ✅ Verifies Java 21 installation
2. ✅ Starts Docker services (PostgreSQL, Kafka, Redis, etc.)
3. ✅ Creates `.env` configuration file
4. ✅ Builds all modules
5. ✅ Runs tests
6. ✅ Generates IDE project files
7. ✅ Prints service URLs and next steps

**Time**: ~10-15 minutes (depending on download speeds)

### Option 2: Quick Commands

If you prefer manual control:

```bash
# 1. Clone repository
git clone https://github.com/your-org/chiroerp.git
cd chiroerp

# 2. Copy environment file
cp .env.example .env

# 3. Start Docker services
docker-compose up -d

# 4. Build project
./gradlew buildAll

# 5. Run tests
./gradlew test
```

---

## Manual Setup

### Step 1: Install Java 21

#### Windows
1. Download [Eclipse Adoptium 21 (LTS)](https://adoptium.net/)
2. Run installer: `OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.msi`
3. Verify installation:
   ```powershell
   java -version
   # Expected: openjdk version "21.0.5" 2024-10-15 LTS
   ```

#### macOS
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

#### Linux (Ubuntu/Debian)
```bash
wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_linux_hotspot_21.0.5_11.tar.gz
sudo tar -xzf OpenJDK21U-jdk_x64_linux_hotspot_21.0.5_11.tar.gz -C /opt
sudo update-alternatives --install /usr/bin/java java /opt/jdk-21.0.5+11/bin/java 1
java -version
```

### Step 2: Install Docker Desktop

#### Windows
1. Download [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. Run installer: `Docker Desktop Installer.exe`
3. Enable WSL 2 backend (recommended)
4. Start Docker Desktop
5. Verify:
   ```powershell
   docker --version
   docker ps  # Should show empty list, not error
   ```

#### macOS
```bash
brew install --cask docker
open /Applications/Docker.app
# Wait for Docker Desktop to start
docker --version
```

#### Linux
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker
docker --version
```

### Step 3: Clone Repository

```bash
git clone https://github.com/your-org/chiroerp.git
cd chiroerp
```

### Step 4: Configure Environment

```bash
# Copy template
cp .env.example .env

# Edit .env if needed (optional for local dev)
# Default values work out-of-the-box
```

**Key configuration values** (`.env`):
```bash
# App PostgreSQL (schema-partitioned local MVP)
POSTGRES_APP_DB=chiroerp
POSTGRES_APP_USER=chiroerp_admin
POSTGRES_APP_PASSWORD=dev_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PASSWORD=dev_password

# Logging
LOG_LEVEL=DEBUG
```

### Step 5: Start Docker Services

```bash
docker-compose up -d
```

**Services started** (12 total):
- **2 PostgreSQL instances** (`postgres-app` for schema-partitioned modules, `postgres-temporal` for Temporal)
- **Redpanda** (Kafka API-compatible event streaming with built-in Schema Registry)
- **Redis** (Caching)
- **Temporal** (Workflow engine)
- **Jaeger** (Distributed tracing)
- **Prometheus** (Metrics)
- **Grafana** (Dashboards)
- **Redpanda Console** (Kafka/Schema Registry management)
- **Redis Commander** (Redis management)
- **pgAdmin** (Database management)

**Wait for services to be healthy**:
```bash
docker-compose ps
# All services should show "healthy" status after ~30 seconds
```

### Step 6: Build Project

```bash
./gradlew buildAll
```

**What this does**:
- Compiles all modules
- Runs ktlint (code style checks)
- Validates architecture rules
- Runs unit tests
- Generates test coverage reports

**Time**: ~5-10 minutes (first build downloads dependencies)

### Step 7: Run Tests

```bash
./gradlew test
```

**View test reports**:
- HTML reports: `<module>/build/reports/tests/test/index.html`
- Coverage reports: `<module>/build/reports/jacoco/test/html/index.html`

### Step 8: Generate IDE Files

#### IntelliJ IDEA
```bash
./gradlew idea
```

Then:
1. Open IntelliJ IDEA
2. **File** → **Open** → Select `chiroerp` directory
3. Wait for Gradle indexing to complete
4. **Run** → **Edit Configurations** → Add Quarkus configurations

#### VS Code
```bash
code .
```

Install recommended extensions:
- Kotlin Language (fwcd.kotlin)
- Gradle for Java (vscjava.vscode-gradle)
- Quarkus (redhat.vscode-quarkus)

---

## Verify Installation

### Check Services

```bash
# All services healthy?
docker-compose ps

# Check logs
docker-compose logs -f

# Test database connection
docker exec -it chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp -c "SELECT version();"
```

### Run Sample Module

```bash
# Start tenancy module in dev mode
./gradlew :bounded-contexts:tenancy-identity:tenancy-core:quarkusDev

# Start identity module in dev mode (separate terminal)
./gradlew :bounded-contexts:tenancy-identity:identity-core:quarkusDev
```

**Expected output**:
```
Listening for transport dt_socket at address: 5005
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2026-02-03 10:00:00,000 INFO  [io.quarkus] (Quarkus Main Thread) tenancy-core 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.31.1) started in 2.345s. Listening on: http://localhost:8071

2026-02-03 10:00:00,001 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
```

**Test endpoints**:
```bash
# Quarkus Dev UI
http://localhost:8071/q/dev

# Health check
curl http://localhost:8071/q/health

# Metrics
curl http://localhost:8071/q/metrics
```

---

## Next Steps

### 1. Create Your First Module

```bash
.\scripts\scaffold-module.ps1 `
    -ModuleName "my-domain" `
    -PackageName "mydomain" `
    -DatabaseName "mydomain" `
    -Port 8090
```

This creates a complete module structure:
- ✅ `build.gradle.kts` (Quarkus dependencies)
- ✅ `application.yml` (Database, Kafka, Redis config)
- ✅ Sample domain entity
- ✅ Sample REST API
- ✅ Database migration (Flyway)
- ✅ Unit/integration tests
- ✅ README.md

### 2. Explore the Codebase

**Read these docs**:
1. [**ARCHITECTURE_GUIDE.md**](ARCHITECTURE_GUIDE.md) - System overview, bounded contexts
2. [**MODULE_DEVELOPMENT.md**](MODULE_DEVELOPMENT.md) - How to add features
3. [**API_STANDARDS.md**](API_STANDARDS.md) - REST conventions
4. [**TESTING_GUIDE.md**](TESTING_GUIDE.md) - Testing strategies

**Key directories**:
```
chiroerp/
├── finance-domain/          # General Ledger, AP/AR
├── sales-distribution/      # Orders, invoicing
├── inventory-management/    # Stock, warehousing
├── platform-shared/         # Common utilities
├── docs/                    # Architecture decisions (ADRs)
├── scripts/                 # Automation scripts
└── docker-compose.yml       # Local services
```

### 3. Run Your First Build

```bash
# Build all modules
./gradlew buildAll

# Run specific module
./gradlew :finance-domain:quarkusDev

# Run tests
./gradlew test

# Check code coverage
./gradlew jacocoTestReport
open finance-domain/build/reports/jacoco/test/html/index.html
```

### 4. Make Your First Commit

```bash
# Create feature branch
git checkout -b feature/my-first-feature

# Make changes
# ...

# Commit
git add .
git commit -m "feat(finance): add GL account validation"

# Push
git push origin feature/my-first-feature
```

**Commit message convention**:
```
<type>(<scope>): <subject>

Types: feat, fix, docs, style, refactor, test, chore
Scopes: finance, sales, inventory, platform, etc.
```

### 5. Open Pull Request

1. Push your branch
2. Open PR on GitHub
3. CI/CD runs automatically:
   - ✅ Build all modules
   - ✅ Run unit tests
   - ✅ Run integration tests
   - ✅ Validate architecture rules
   - ✅ Security scan (OWASP Dependency Check, Trivy)
   - ✅ Code coverage report

---

## Troubleshooting

### Common Issues

#### 1. "Java version mismatch"

**Error**:
```
ERROR: JAVA_HOME is set to an invalid directory
```

**Fix**:
```bash
# Windows
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

#### 2. "Docker daemon not running"

**Error**:
```
Cannot connect to the Docker daemon
```

**Fix**:
1. Start Docker Desktop
2. Wait for "Docker Desktop is running" notification
3. Retry: `docker ps`

#### 3. "Port already in use"

**Error**:
```
Bind for 0.0.0.0:5432 failed: port is already allocated
```

**Fix**:
```bash
# Find process using port
netstat -ano | findstr :5432  # Windows
lsof -i :5432                  # macOS/Linux

# Stop conflicting service
docker-compose down

# Or change port in docker-compose.yml
```

#### 4. "Gradle build fails"

**Error**:
```
Could not resolve all dependencies
```

**Fix**:
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Retry
./gradlew buildAll --refresh-dependencies
```

#### 5. "Out of memory"

**Error**:
```
java.lang.OutOfMemoryError: Java heap space
```

**Fix**:
```bash
# Increase Gradle memory (gradle.properties)
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m

# Increase Docker memory (Docker Desktop → Settings → Resources)
# Set to at least 8 GB
```

#### 6. "Database connection refused"

**Error**:
```
Connection to localhost:5432 refused
```

**Fix**:
```bash
# Check services running
docker-compose ps

# Restart services
docker-compose restart postgres-app

# Check logs
docker-compose logs postgres-app
```

#### 7. "Kafka not available"

**Error**:
```
org.apache.kafka.common.errors.TimeoutException
```

**Fix**:
```bash
# Check Redpanda health
docker-compose ps redpanda

# Restart Redpanda
docker-compose restart redpanda

# Wait 30 seconds for startup
sleep 30
```

### Getting Help

1. **Check logs**:
   ```bash
   docker-compose logs -f <service-name>
   ```

2. **Check health**:
   ```bash
   docker-compose ps
   curl http://localhost:8081/q/health
   ```

3. **Reset everything**:
   ```bash
   docker-compose down -v  # ⚠️ Deletes all data
   docker-compose up -d
   ./gradlew clean buildAll
   ```

4. **Ask for help**:
   - **Slack**: #chiroerp-dev
   - **Email**: dev-team@chiroerp.local
   - **GitHub Issues**: https://github.com/your-org/chiroerp/issues

---

## Service URLs (Local)

| Service | URL | Credentials |
|---------|-----|-------------|
| **Redpanda Console** | http://localhost:8090 | - |
| **Redpanda Kafka API** | localhost:19092 (external), localhost:9092 (docker) | - |
| **Schema Registry** | http://localhost:18081 | - |
| **Redis Commander** | http://localhost:8091 | - |
| **Temporal UI** | http://localhost:8092 | - |
| **pgAdmin** | http://localhost:8093 | admin@chiroerp.local / admin |
| **Jaeger UI** | http://localhost:16686 | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Finance GL (planned)** | http://localhost:8081 | - |
| **Finance AR (planned)** | http://localhost:8082 | - |
| **Finance AP (planned)** | http://localhost:8083 | - |
| **Finance Assets (planned)** | http://localhost:8084 | - |
| **Finance Tax (planned)** | http://localhost:8085 | - |
| **Tenancy Core (planned)** | http://localhost:8071 | - |
| **Identity Core (planned)** | http://localhost:8072 | - |

---

## Useful Commands

### Docker

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart postgres-app

# Remove everything (including volumes)
docker-compose down -v

# Check service health
docker-compose ps
```

### Gradle

```bash
# Build all modules
./gradlew buildAll

# Build specific module
./gradlew :finance-domain:build

# Run tests
./gradlew test

# Run specific module in dev mode
./gradlew :finance-domain:quarkusDev

# Clean build
./gradlew clean

# Refresh dependencies
./gradlew --refresh-dependencies
```

### Database

```bash
# Connect to database
docker exec -it chiroerp-postgres-app psql -U chiroerp_admin -d chiroerp

# Run migration
./gradlew :finance-domain:flywayMigrate

# Check migration status
./gradlew :finance-domain:flywayInfo

# Rollback migration
./gradlew :finance-domain:flywayUndo
```

### Git

```bash
# Create feature branch
git checkout -b feature/my-feature

# Commit changes
git add .
git commit -m "feat(finance): add feature"

# Push branch
git push origin feature/my-feature

# Pull latest changes
git pull origin develop

# Rebase on develop
git rebase develop
```

---

## Development Workflow

1. **Pull latest code**: `git pull origin develop`
2. **Create feature branch**: `git checkout -b feature/my-feature`
3. **Start services**: `docker-compose up -d`
4. **Run module in dev mode**: `./gradlew :module:quarkusDev`
5. **Make changes** (hot reload enabled)
6. **Write tests** (TDD recommended)
7. **Run tests**: `./gradlew test`
8. **Commit**: `git commit -m "feat(scope): description"`
9. **Push**: `git push origin feature/my-feature`
10. **Open PR** on GitHub
11. **Wait for CI/CD** (build, tests, security scan)
12. **Request code review**
13. **Merge** after approval

---

**Next**: Read [**ARCHITECTURE_GUIDE.md**](ARCHITECTURE_GUIDE.md) to understand the system design.

**Questions?** Ask in **#chiroerp-dev** on Slack.

**Happy coding!** 🚀
