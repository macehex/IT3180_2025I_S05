# JavaFX App Build 

## Quick Start

### Linux/Mac:
```bash
./build-app.sh
```

### Windows:
```cmd
build-app.bat
```

##  Available Build Options

### 1. Fat JAR 
- **What it does**: Creates a single executable JAR with all dependencies
- **Gradle task**: `./gradlew clean fatJarNoModule`
- **Output**: `build/libs/quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar` (~57MB)
- **Run with**: Click on the file build/libs/quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar or use:
- **Pros**: 
  -  Works everywhere with Java 21
  -  No module conflicts
  -  Easy to distribute
  -  Single file deployment
### 3. Standard Distribution
- **What it does**: Creates ZIP/TAR with separate JARs and scripts
- **Gradle task**: `./gradlew clean distZip distTar`
- **Output**: `build/distributions/`
- **Pros**: 
  -  Traditional Java deployment
  -  Separate dependency management

