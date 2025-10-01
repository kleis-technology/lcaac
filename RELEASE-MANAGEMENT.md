# Release management

This guide describes the steps required to release a new version of **LCA as Code (LCAAC)**.

## Prerequisites
- You have the latest version of the main branch.
- You have the necessary permissions to tag releases and trigger workflows.
- You have access to the repository homebrew-lcaac for package updates.

## Release Steps
1. **Update the version number**

   Update the version consistently across the following files:
    - `resources/META-INF/lcaac.properties`
    - `gradle.properties`
    - `README.md` → update the installation script from source (around line 31)
    
   ⚠️ Double-check that all version numbers match.

2.**Merge your pull request**

   Ensure your changes are merged into the `main` branch.
3.**Create a Git tag**

   Add a tag to the main branch (replace `X.Y.Z` with the new version number).

   Use semantic versioning (`MAJOR.MINOR.PATCH`) format
   ```
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```
   This will trigger the publication workflow that builds and publishes the release artifacts.

## Post-Release Steps
- Update Homebrew and Nix formulas
  To make the new version available via brew and nix by following the instructions in the `kleis-technology/homebrew-lcaac` repository.