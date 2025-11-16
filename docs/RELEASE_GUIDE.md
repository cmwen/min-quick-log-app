# Release Guide

This guide explains how to create different types of releases for the Android app using the release workflow.

## Release Types

The release workflow supports three types of releases:

1. **Production** - Stable releases for end users (e.g., `v1.0.0`)
2. **Beta** - Feature-complete releases for testing (e.g., `v1.1.0-beta.1`)
3. **Prerelease** - Early testing releases including alpha and release candidates (e.g., `v1.2.0-alpha.1`, `v1.2.0-rc.1`)

## Version Naming Conventions

Follow semantic versioning with optional prerelease suffixes:

- **Production**: `v1.0.0`, `v2.1.3`
- **Beta**: `v1.1.0-beta.1`, `v1.1.0-beta.2`
- **Alpha**: `v1.2.0-alpha.1`, `v1.2.0-alpha.2`
- **Release Candidate**: `v1.3.0-rc.1`, `v1.3.0-rc.2`

## Creating Releases

### Method 1: Manual Workflow Dispatch

Navigate to **Actions → Android Release → Run workflow** in GitHub and fill in:

1. **version_name**: The semantic version (e.g., `1.0.0`, `1.1.0-beta.1`)
2. **version_code**: Monotonic integer that must increase with each release (e.g., `5`)
3. **release_type**: Choose from:
   - `production` - For stable releases
   - `beta` - For beta testing releases
   - `prerelease` - For alpha/RC releases
4. **create_tag**: Whether to create a git tag (default: true)

#### Example: Production Release

```
version_name: 1.0.0
version_code: 10
release_type: production
create_tag: true
```

#### Example: Beta Release

```
version_name: 1.1.0-beta.1
version_code: 11
release_type: beta
create_tag: true
```

#### Example: Alpha/RC Release

```
version_name: 1.2.0-alpha.1
version_code: 12
release_type: prerelease
create_tag: true
```

### Method 2: Push a Git Tag

Create and push a git tag with the appropriate naming convention:

```bash
# Production release
git tag v1.0.0
git push origin v1.0.0

# Beta release
git tag v1.1.0-beta.1
git push origin v1.1.0-beta.1

# Alpha release
git tag v1.2.0-alpha.1
git push origin v1.2.0-alpha.1

# Release candidate
git tag v1.3.0-rc.1
git push origin v1.3.0-rc.1
```

The workflow will automatically detect the release type from the tag name:
- Tags with `-beta` suffix → marked as beta
- Tags with `-alpha` or `-rc` suffix → marked as prerelease
- Tags without suffix → marked as production

## GitHub Release Behavior

- **Production releases**: Created as regular releases (not marked as prerelease)
- **Beta/Prerelease releases**: Marked as "Pre-release" in GitHub and shown separately in the releases list

## Version Code Management

The `version_code` must be a monotonically increasing integer. This is used by Android to determine which version is newer.

**Best practices:**
- Increment by 1 for each new release
- Never reuse a version code
- Keep a record of used version codes to avoid conflicts

Example progression:
```
v1.0.0        → version_code: 1
v1.0.1        → version_code: 2
v1.1.0-beta.1 → version_code: 3
v1.1.0-beta.2 → version_code: 4
v1.1.0        → version_code: 5
v1.2.0-rc.1   → version_code: 6
v1.2.0        → version_code: 7
```

## Release Artifacts

Each release generates:
- **APK**: `app/build/outputs/apk/release/*.apk` - For direct installation
- **AAB**: `app/build/outputs/bundle/release/*.aab` - For Play Store distribution

Both artifacts are:
- Uploaded as workflow artifacts
- Attached to the GitHub Release

## Prerequisites

Before creating a release, ensure:

1. All required secrets are configured (see [secrets-management.md](secrets-management.md))
2. The code builds successfully on the main branch
3. All tests pass
4. Version numbers follow the conventions above

## Troubleshooting

### Release Type Not Detected Correctly

If pushing a tag and the release type isn't detected:
- Ensure the tag name follows the naming convention exactly
- The detection logic looks for `-beta`, `-alpha`, or `-rc` in the tag name
- You can manually specify the release type using workflow_dispatch

### Version Code Conflicts

If you see version code conflicts:
- Check the version codes used in previous releases
- Ensure you're using a higher version code than all previous releases
- Update `APP_VERSION_CODE` in `gradle.properties` if needed

### Failed Signing

If signing fails:
- Verify all signing secrets are correctly set
- Check that the keystore is valid and not expired
- Ensure passwords match the keystore configuration
