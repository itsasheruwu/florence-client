# Release Structure

This document defines the standard structure for Florence Client releases.

## Versioning

- Tag format: `v<mc-version>-<release-number>`
- Example: `v1.21.11-1`
- Release artifact: `florence-client-<mc-version>-<release-number>.jar`

## Git Flow

1. Land release-ready changes on `main`.
2. Update [`CHANGELOG.md`](/Users/ash/Downloads/meteor-client-master/CHANGELOG.md) with the new release entry.
3. Build the release artifact with the intended build number.
4. Push `main`.
5. Create an annotated Git tag for the release.
6. Publish the GitHub release and attach the built JAR.

## Release Notes Format

Each release should use this section layout:

```md
## [vX.Y.Z-N] - YYYY-MM-DD

Short summary of the release.

### Added
- New features

### Changed
- Improvements or behavior changes

### Fixed
- Bug fixes

### Notes
- Compatibility notes
- Build or packaging notes
```

## GitHub Release Layout

Use this structure for the GitHub release body:

```md
Short summary of the release.

Added:
- New features

Changed:
- Improvements

Fixed:
- Bug fixes

Notes:
- Minecraft version
- Artifact name
- Special compatibility or migration details
```

## Release Checklist

- `CHANGELOG.md` updated
- Release version chosen
- JAR builds successfully
- `main` pushed to GitHub
- Tag pushed to GitHub
- GitHub release published
- Release artifact uploaded

## Current First Release

- Tag: `v1.21.11-1`
- Artifact: `florence-client-1.21.11-1.jar`
- Release page: [Florence Client v1.21.11-1](https://github.com/itsasheruwu/florence-client/releases/tag/v1.21.11-1)
