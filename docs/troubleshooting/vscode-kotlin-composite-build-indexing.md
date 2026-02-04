# VS Code Kotlin Language Server - Composite Build Indexing Issue

**Issue Date**: 2026-02-04
**Status**: ✅ RESOLVED
**Affected Files**: `build-logic/**/*.kt` (Gradle composite build modules)
**Symptom**: "Unresolved reference" errors in IDE despite successful compilation

---

## Problem Description

### Symptoms
VS Code's Kotlin Language Server shows **false "Unresolved reference" errors** for Gradle API classes and methods in the `build-logic` composite build module:

- ❌ `compilerOptions` - shows as unresolved
- ❌ `jvmTarget` - shows as unresolved
- ❌ `useJUnitPlatform` - shows as unresolved
- ❌ `testLogging`, `jvmArgs`, `doLast` - all show as unresolved

### Key Characteristics
1. **Code compiles successfully** - `gradlew :build-logic:compileKotlin` ✅ BUILD SUCCESSFUL
2. **Tests pass** - All tests run without issues
3. **Only affects IDE display** - Red squiggles in editor, but functionality works
4. **Specific to composite builds** - Main project files don't have this issue
5. **Gradle daemon uses correct JDK** - JDK 21 confirmed

### Root Cause
VS Code's Java/Kotlin Language Server **does not automatically index composite builds** (like `build-logic`) that are included via `includeBuild()` in `settings.gradle.kts`. The language server analyzes these modules without the Gradle API classpath, causing unresolved reference warnings.

---

## Solution

### ✅ Working Solution: Suppress IDE Warnings with `@Suppress` Annotation

Add `@Suppress("UNRESOLVED_REFERENCE")` to affected Kotlin files in the `build-logic` module:

```kotlin
/**
 * ChiroERP Kotlin Conventions Plugin
 *
 * NOTE: IDE may show "Unresolved reference" errors for Gradle API (compilerOptions, jvmTarget, etc.)
 *       This is a VS Code indexing issue with composite builds. Code compiles perfectly.
 *       Ignore red squiggles - they are false positives. BUILD SUCCESSFUL proves correctness.
 */
@Suppress("UNRESOLVED_REFERENCE")
class KotlinConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // Gradle API methods work correctly despite IDE warnings
        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {  // ✅ Compiles fine, may show red in IDE
                jvmTarget.set(JvmTarget.JVM_21)
                // ...
            }
        }
    }
}
```

**Result**: ✅ IDE warnings suppressed, no more red squiggles

---

## Failed Approaches (For Reference)

### ❌ Approach 1: Reload Window
**Command**: `Ctrl+Shift+P` → "Reload Window"
**Result**: Failed - Language server doesn't re-index composite builds on reload

### ❌ Approach 2: Clean Java Language Server Workspace
**Command**: `java.clean.workspace`
**Result**: Failed - Cleared cache but didn't trigger Gradle import for `build-logic`

### ❌ Approach 3: Gradle Refresh Dependencies
**Command**: `gradlew --refresh-dependencies`
**Result**: Failed - Regenerates classpath but IDE doesn't pick it up

### ❌ Approach 4: Explicit VS Code Settings
**Attempted**:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.import.gradle.enabled": true,
  "java.import.gradle.wrapper.enabled": true,
  "java.import.gradle.projects": [
    "${workspaceFolder}",
    "${workspaceFolder}/build-logic"
  ],
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.9.10-hotspot"
    }
  ],
  "java.jdt.ls.java.home": "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.9.10-hotspot"
}
```
**Result**: Failed - Settings correct but language server still doesn't index `build-logic`

### ❌ Approach 5: Touch settings.gradle.kts
**Command**: Touched file to trigger auto-reimport
**Result**: Failed - No re-indexing triggered

### ❌ Approach 6: Restart Kotlin Language Server
**Attempted**: Searched for Kotlin-specific restart commands
**Result**: Failed - Basic `mathiasfrohlich.kotlin` extension doesn't have LSP restart command

---

## Technical Details

### Project Structure
```
chiroerp/
├── build-logic/                 ← Composite build (precompiled script plugins)
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── chiroerp/buildlogic/
│           ├── KotlinConventionsPlugin.kt    ← ❌ Shows IDE errors
│           ├── QuarkusConventionsPlugin.kt
│           └── NativeImageConventionsPlugin.kt
├── finance-domain/              ← Main project module
│   ├── build.gradle.kts
│   └── src/main/kotlin/...      ← ✅ No IDE errors
├── settings.gradle.kts          ← includeBuild("build-logic")
└── build.gradle.kts
```

### Gradle Configuration
**settings.gradle.kts**:
```kotlin
rootProject.name = "chiroerp"

// Composite build - causes IDE indexing issues
includeBuild("build-logic")

include("finance-domain")
// ... other modules
```

**build-logic/build.gradle.kts**:
```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    // Gradle API available at compile time ✅
}
```

### VS Code Extensions
- **Red Hat Java**: `redhat.java` v1.52.0 ✅ Installed
- **Kotlin**: `mathiasfrohlich.kotlin` ⚠️ Syntax only (no LSP)
- **Gradle for Java**: Installed via Java extension pack

---

## Why This Happens

1. **Composite builds are separate Gradle projects** - `build-logic` has its own `build.gradle.kts`
2. **VS Code Java extension imports main project only** - Doesn't automatically detect `includeBuild()`
3. **Kotlin Language Server analyzes files without Gradle context** - No access to Gradle API classpath
4. **Manual import required** - User must explicitly tell VS Code to import composite build
5. **`@Suppress` bypasses the check** - Tells language server to ignore the errors

---

## Verification

### Confirm Issue is Cosmetic Only
```powershell
# Build the build-logic project
gradlew :build-logic:compileKotlin
# ✅ BUILD SUCCESSFUL in 7s

# Verify Gradle daemon is using JDK 21
gradlew --version
# Gradle 8.x
# Kotlin: 2.2.0
# JVM: 21.0.5 (Eclipse Adoptium 21.0.5+11)

# Run full project build
gradlew build
# ✅ BUILD SUCCESSFUL in 5m 16s
# ✅ All 86 tests PASSED
```

### Confirm `@Suppress` Works
After adding `@Suppress("UNRESOLVED_REFERENCE")`:
- ✅ Red squiggles disappear in IDE
- ✅ Code still compiles successfully
- ✅ No runtime impact
- ✅ Tests still pass

---

## Long-Term Solution (Future Work)

### Option 1: Use IntelliJ IDEA Instead
IntelliJ IDEA has **native Gradle composite build support** and handles `includeBuild()` correctly. The IDE automatically indexes all included builds.

**Trade-off**: Requires switching from VS Code to IntelliJ

### Option 2: Multi-Root Workspace in VS Code
Create a `.code-workspace` file that explicitly includes both roots:

```json
{
  "folders": [
    {
      "name": "ChiroERP (Main)",
      "path": "."
    },
    {
      "name": "Build Logic",
      "path": "build-logic"
    }
  ],
  "settings": {
    "java.configuration.updateBuildConfiguration": "automatic"
  }
}
```

**Trade-off**: Complicates workspace setup, may cause duplicate indexing

### Option 3: Wait for VS Code Java Extension Update
Track this issue in VS Code Java extension repository and wait for native composite build support.

**Trade-off**: Unknown timeline, not guaranteed

---

## Recommendation

**For now**: ✅ Use `@Suppress("UNRESOLVED_REFERENCE")` annotation
**Reason**:
- Simple one-line fix
- No impact on functionality
- Clearly documents the issue
- Works immediately

**For future**: Monitor VS Code Java extension updates for native composite build support

---

## Related Issues

- VS Code Java Extension: Composite build import support
- Gradle: Composite build documentation
- Kotlin Gradle Plugin: DSL type resolution in buildSrc/build-logic

---

## Document Metadata

**Author**: ChiroERP Development Team
**Last Updated**: 2026-02-04
**Tested On**:
- VS Code: 1.x
- Java Extension: redhat.java 1.52.0
- Gradle: 8.x
- Kotlin: 2.2.0
- JDK: 21.0.9 (Eclipse Adoptium)

**Keywords**: Kotlin, Gradle, Composite Build, VS Code, Language Server, Unresolved Reference, buildSrc, build-logic
