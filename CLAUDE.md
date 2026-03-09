# CLAUDE.md

## Repository Sync

Before starting work, check whether collaborators have pushed new commits or other changes to the remote repository.

If the remote has changes that are not present locally, pull them first so the local working copy stays in sync with the latest repository state.

## Settings System

When adding or changing behaviour that involves settings (`florencedevelopment.florenceclient.settings`), agents should not feel restricted by the current setting types or their UI. If a request requires a kind of setting or UI that does not fit the existing types or widgets, extend the system as needed: add new setting subclasses, new builders, and corresponding UI handling. Prefer reusing existing patterns where they fit, but do not avoid introducing new setting types or UI components when the feature requires it.

## Release Process

For any release, tagging, changelog, or publishing work, read [`RELEASE_STRUCTURE.md`](/Users/ash/Downloads/meteor-client-master/RELEASE_STRUCTURE.md) first and follow that structure.
