# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Tooling and Common Commands

### Environment

- Node version is defined in `.nvmrc` (`v22.20.0`).
- Yarn 3 (`"packageManager": "yarn@3.6.1"`) with workspaces is used; do not use `npm` for development.

### Setup

- Install dependencies for the library and the example app from the repo root: `yarn`.

### Linting, typechecking, and tests

- Lint all JS/TS/TSX: `yarn lint`
- Auto-fix lint/formatting issues: `yarn lint --fix`
- Typecheck TypeScript: `yarn typecheck`
- Run the Jest test suite: `yarn test`
- Run a single test file or pattern (Jest): `yarn test -- src/__tests__/index.test.tsx` or `yarn test -- <pattern>`

### Building and cleaning the library

- Clean build artifacts (`android/build`, `example/*/build`, `lib`): `yarn clean`
- Build the library outputs in `lib/` using `react-native-builder-bob`: `yarn prepare`
- Release a new version to npm and GitHub (maintainers only, uses `release-it` and conventional commits): `yarn release`

### Example app (local manual testing)

The `example/` workspace is a standard React Native app wired to use the local library.

From the repo root (recommended):

- Start Metro for the example app: `yarn example start`
- Run the example app on Android: `yarn example android`
- Run the example app on iOS: `yarn example ios`

From inside `example/` (optional, same underlying CLI):

- Start Metro: `yarn start`
- Run on Android: `yarn android`
- Run on iOS: `yarn ios`
- Build native debug artifacts: `yarn build:android`, `yarn build:ios`

## Project Architecture

### Monorepo layout

This repository is a Yarn workspaces monorepo with two main packages:

- Root package: the published library `react-native-highlight-text-view`.
- Workspace `example/`: the app `react-native-highlight-text-example`, depending on the library via the workspace and used for development and manual QA.

Key directories in the root package:

- `src/` – TypeScript source for the library and the React Native codegen entry point.
- `lib/` – Generated JS and type declarations produced by `yarn prepare`; do not edit files here directly.
- `android/` – Gradle module implementing the Android side of the native view.
- `ios/` – iOS implementation of the native view (`HighlightTextView.h` / `HighlightTextView.mm`) plus the CocoaPods spec `HighlightText.podspec`.
- `example/` – React Native app used for local development and verifying the native behavior of the component.
- Config and tooling: `babel.config.js`, `eslint.config.mjs`, `tsconfig*.json`, `lefthook.yml`, `turbo.json`, Jest configuration inside `package.json`.

### HighlightTextView component and codegen

The JS/TS surface of the library is intentionally thin and relies on React Native’s New Architecture codegen:

- `src/HighlightTextViewNativeComponent.ts` defines:
  - `OnChangeEventData` for the change event payload.
  - `TextAlignment` union type describing horizontal/vertical alignment values.
  - `HighlightTextViewProps`, which extends `ViewProps` and adds:
    - Presentation props (`color`, `textColor`, `fontFamily`, `fontSize`, `fontWeight`, `lineHeight`, `highlightBorderRadius`).
    - Layout and alignment props (`textAlign`, `verticalAlign`, padding props, background inset props).
    - Text/input behavior (`text`, `isEditable`, `autoFocus`, `onChange`).
  - It exports `codegenNativeComponent<HighlightTextViewProps>('HighlightTextView')`, which is the primary JS component.
- `src/index.tsx` simply re-exports this native component as the library’s main entry point.
- The `codegenConfig` field in `package.json` ties this JS definition to platform-specific codegen:
  - `name: "HighlightTextViewSpec"`, `type: "all"`, `jsSrcsDir: "src"`.
  - Platform-specific settings for Android (`javaPackageName: "com.highlighttext"`) and iOS (component provider for `HighlightTextView`).

When adding or changing props:

1. Update `HighlightTextViewProps` (and related types) in `src/HighlightTextViewNativeComponent.ts`.
2. Regenerate/build via `yarn prepare` so codegen artifacts and `lib/` stay in sync.
3. Update the native implementations on both Android and iOS to handle the new props.

### Native implementation (Android and iOS)

- **Android (`android/`)**
  - A standard React Native library Android module, wired via `build.gradle` and `gradle.properties`.
  - Implementation files under `android/src/` define the Fabric view and handle the highlight rendering, text input behavior, and prop mapping coming from the TS codegen spec.
  - Android builds respect Turbo task inputs declared in `turbo.json` under `build:android`, which is mainly relevant for CI caching and incremental builds.

- **iOS (`ios/`)**
  - `HighlightTextView.h` / `HighlightTextView.mm` implement the iOS side of the Fabric view, reading the same set of props as defined in `HighlightTextViewProps`.
  - `HighlightText.podspec` describes the pod used when the library is integrated into an iOS app, including the New Architecture configuration.
  - Turbo’s `build:ios` task in `turbo.json` is configured to cache based on the iOS sources, podspecs, and `example/ios`.

The native and JS layers are connected entirely through React Native’s codegen configuration; there is no hand-written bridging module. Any change in props must be reflected in both TS types and native implementations.

### Example app

- Located in `example/`, bootstrapped with `@react-native-community/cli`.
- Depends on `react-native-highlight-text-view: "*"`, which resolves to the local workspace library during development.
- Used as the primary way to exercise the component and visually verify changes to highlighting, padding, and layout behavior.
- For detailed device and simulator instructions (Metro, Android Emulator, iOS Simulator), see `example/README.md`.

### Testing, linting, and Git hooks

- Tests use Jest with the React Native preset, configured in the root `package.json`.
  - Current test entry lives under `src/__tests__/`.
- Linting and formatting use ESLint + Prettier, configured via `eslint.config.mjs`.
- Type checking uses TypeScript with `tsconfig.json` / `tsconfig.build.json`.

Git hooks are managed via `lefthook.yml`:

- `pre-commit` runs ESLint and `tsc` on staged JS/TS files via `npx eslint {staged_files}` and `npx tsc`.
- `commit-msg` runs `npx commitlint --edit` to enforce the conventional commits format (see `CONTRIBUTING.md` for the allowed types like `feat`, `fix`, `refactor`, `docs`, `test`, `chore`).

### README highlights

The root `README.md` documents the public API and important usage patterns:

- Consumers import the component from the published package: `import { HighlightTextView } from 'react-native-highlight-text-view';`.
- The component accepts props controlling:
  - Highlight color (`color`), text color (`textColor`), font and size (`fontFamily`, `fontSize`, `lineHeight`).
  - Border radius and padding around the highlighted glyphs.
  - Background "inset" props that shrink the highlight region inward from the font’s line box (useful for fonts with large ascenders/descenders).
  - Alignment behavior (horizontal and vertical; vertical alignment is iOS-only).
  - Editable behavior (`text`, `isEditable`, `autoFocus`, `onChange`).
- The README includes examples showing:
  - How to combine `lineHeight` with background insets to make highlights on multiple lines "touch" vertically.
  - How to use `autoFocus` to open the keyboard on mount.
  - How to safely change `fontFamily` at runtime by passing it as a `key` prop to force a React remount, which is required for some fonts with unusual metrics.

For detailed prop documentation, advanced layout tuning, and usage examples, refer directly to `README.md`.

### Contributing conventions

Key points from `CONTRIBUTING.md` relevant to automated work in this repo:

- Ensure `yarn typecheck`, `yarn lint`, and `yarn test` all pass before sending changes.
- Commit messages must follow the conventional commits specification; the `commitlint` hook will reject non-conforming messages.
- Use the example app (`yarn example start`, `yarn example android` / `yarn example ios`) to manually verify UI and native behavior changes to `HighlightTextView`.
