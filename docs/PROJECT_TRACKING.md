# ChiroERP Interactive Project Tracking

This document describes the interactive project tracking system that integrates with GitHub to monitor implementation progress.

## Overview

ChiroERP uses a **multi-layered tracking approach**:

1. **📋 Documentation Validation** (`validate-docs.ps1`) - Ensures docs stay consistent
2. **📊 Progress Tracking** (`track-progress.ps1`) - Analyzes code to measure completion
3. **🤖 GitHub Integration** - Auto-updates issues, projects, and generates badges
4. **📈 Progress Reports** - Markdown/JSON reports for stakeholders

## Components

### 1. Progress Tracker Script (`scripts/track-progress.ps1`)

**Features:**
- Scans codebase for modules and source files
- Analyzes ADR implementation status
- Calculates completion % per domain
- Generates reports in multiple formats
- Optionally updates GitHub Issues/Projects

**Usage:**
```powershell
# View progress in console
.\scripts\track-progress.ps1

# Generate markdown report
.\scripts\track-progress.ps1 -OutputFormat markdown -OutputPath docs/PROGRESS.md

# Generate JSON for automation
.\scripts\track-progress.ps1 -OutputFormat json -OutputPath progress.json

# Update GitHub Project (requires gh CLI + GH_TOKEN)
.\scripts\track-progress.ps1 -UpdateGitHub
```

### 2. GitHub Actions Workflow (`.github/workflows/progress-tracker.yml`)

**Triggers:**
- ✅ Every push to main/develop branches
- ✅ On pull requests (shows impact in PR comments)
- ✅ Weekly (Monday 9AM UTC) - Creates issues for missing modules
- ✅ Manual trigger via workflow_dispatch

**Actions:**
- Runs progress tracker
- Generates reports (Markdown + JSON)
- Commits `docs/PROGRESS.md` to main branch
- Comments on PRs with progress summary
- Weekly: Auto-creates GitHub issues for unimplemented modules
- Generates progress badge

### 3. GitHub Project Board Integration

**Setup Required:**
1. Create GitHub Project in your repository
2. Add custom fields:
   - `Domain` (select: Finance, Procurement, Inventory, etc.)
   - `Module Path` (text)
   - `Completion %` (number)
   - `Source Files` (number)
   - `Has Tests` (checkbox)

3. Configure automation rules:
   - Auto-move to "In Progress" when PR opened
   - Auto-move to "Done" when module has 5+ source files
   - Auto-label by domain

**Workflow will:**
- Create issues for each unimplemented module
- Update issue status based on code analysis
- Track progress in Project board views

## Progress Calculation Logic

### Module Status

| Status | Criteria |
|--------|----------|
| **Not Started** | No source files (`src/main/` empty or missing) |
| **Scaffolded** | 1-4 source files |
| **Implemented** | 5+ source files |

### ADR Status

Extracted from ADR frontmatter:
- `**Status**: Implemented` → Implemented
- `**Status**: In Progress` → In Progress
- `**Status**: Draft` → Not Started

### Domain Progress

Calculated as: `(Implemented Modules / Total Modules) * 100%`

## Integration with validate-docs.ps1

The two scripts work together:

| Script | Purpose | When to Run |
|--------|---------|-------------|
| `validate-docs.ps1` | **Documentation consistency** | Every commit (CI) |
| `track-progress.ps1` | **Implementation progress** | Weekly + milestones |

**Combined workflow:**
1. Developer creates module → `validate-docs.ps1` ensures docs updated
2. Weekly → `track-progress.ps1` measures actual implementation
3. GitHub Actions → Updates Project board automatically

## Viewing Progress

### 1. Console View
```powershell
.\scripts\track-progress.ps1
```

Output:
```
╔══════════════════════════════════╗
║ ChiroERP Implementation Progress ║
╚══════════════════════════════════╝

━━━ OVERALL PROGRESS ━━━

Modules: [████████░░░░░░░░░░] 42% (15/36)
ADRs   : [██████████░░░░░░░░] 52% (30/57)

━━━ DOMAIN PROGRESS ━━━

Platform       : [████████████████████] 100% (5/5)
Finance        : [████████░░░░░░░░░░░░] 40% (2/5)
Procurement    : [░░░░░░░░░░░░░░░░░░░░] 0% (0/2)
...
```

### 2. Markdown Report (`docs/PROGRESS.md`)

Auto-generated and committed to repository. View at:
https://github.com/your-org/chiroerp/blob/main/docs/PROGRESS.md

Contains:
- Overall status table
- Domain progress with visual bars
- Complete module list with status

### 3. GitHub Project Board

Visual kanban board showing:
- Not Started → In Progress → Done
- Filtered by domain
- Burndown charts
- Velocity tracking

### 4. Progress Badge

Add to README.md:
```markdown
![Implementation Progress](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/your-org/chiroerp/main/badge.json)
```

## Example Workflow

### Scenario: Starting Finance GL Module

1. **Create module structure:**
   ```bash
   mkdir -p bounded-contexts/finance/finance-gl/src/main/kotlin
   touch bounded-contexts/finance/finance-gl/build.gradle.kts
   ```

2. **Add source files:**
   ```bash
   # Add domain models, repositories, services
   # Once you have 5+ .kt files, status changes to "Implemented"
   ```

3. **Update ADR status:**
   ```markdown
   # In docs/adr/ADR-009-financial-accounting-domain.md
   **Status**: In Progress  # or Implemented
   ```

4. **Run validation:**
   ```powershell
   # Check docs consistency
   .\scripts\validate-docs.ps1

   # See progress update
   .\scripts\track-progress.ps1
   ```

5. **Commit changes:**
   ```bash
   git add .
   git commit -m "feat(finance): Implement GL module core"
   git push
   ```

6. **GitHub Actions automatically:**
   - Validates documentation
   - Updates progress report
   - Comments on PR showing new progress
   - Updates GitHub Project board
   - Closes "Implement finance-gl" issue

## Customization

### Add Custom Metrics

Edit `track-progress.ps1`:

```powershell
# Add new metric
$script:ProgressData.summary.testCoverage = Calculate-TestCoverage

# Add to report
Write-Progress-Bar -Current $coverage -Total 100 -Label "Coverage"
```

### Custom GitHub Labels

Edit `.github/workflows/progress-tracker.yml`:

```yaml
labels: ['module-implementation', 'enhancement', module.domain, 'priority:high']
```

### Custom Project Fields

Modify GitHub API calls to match your Project schema.

## Troubleshooting

### "Found 0 modules"

Check that:
- Modules have `build.gradle.kts` files
- Module paths don't contain `build-logic`, `buildSrc`, or `.gradle`
- PowerShell is running from repository root

### "GitHub CLI not installed"

Install GitHub CLI:
```bash
# Windows
winget install GitHub.cli

# macOS
brew install gh

# Linux
See: https://github.com/cli/cli#installation
```

Then authenticate:
```bash
gh auth login
```

### "Permission denied" on GitHub updates

Ensure workflow has permissions:
```yaml
permissions:
  contents: write
  issues: write
  pull-requests: write
```

## Future Enhancements

- [ ] HTML dashboard with charts (Chart.js/D3.js)
- [ ] Integration with Temporal workflow tracking
- [ ] Automatic milestone creation based on progress
- [ ] Slack/Teams notifications on milestones
- [ ] Test coverage integration
- [ ] Code quality metrics (SonarQube)
- [ ] Dependency graph visualization
- [ ] Time-to-implement predictions (ML)

## References

- [GitHub Projects V2 API](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects)
- [GitHub Actions - actions/github-script](https://github.com/actions/github-script)
- [ADR-008: CI/CD Pipeline](../adr/ADR-008-cicd-network-resilience.md)
- [validate-docs.ps1 Script](../../scripts/validate-docs.ps1)

---

**Status**: ✅ Implemented
**Last Updated**: 2026-02-03
**Maintainer**: Development Team
