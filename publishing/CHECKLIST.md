# Publishing checklist — Modrinth

Everything below is ordered. Items marked **[YOU]** need info only you have.

## 1. Before you upload

- [ ] **Pick your author handle** and replace `"You"` in `src/main/resources/fabric.mod.json` → `authors`, and `Your Name` in the `LICENSE` file, then rebuild.
- [ ] **[YOU] Create a GitHub repo** and push this project. Modrinth strongly expects a source link for mods that include mixins/bytecode patching (transparency for safety scanners), and it unlocks the "Source" button on your page.
- [ ] **Take screenshots** for the gallery:
  - The overlay on a server (ping colored, sparkline visible if you enable it)
  - The settings screen (shows off sliders/colors/drag preview)
  - Recommended: 1920×1080 or wider, PNG
- [ ] (Optional) Record a 30–60 s GIF/short video of dragging the box around and opening settings — pages with media convert far better.

## 2. Create the project

- [ ] Log in at [modrinth.com](https://modrinth.com) → **Create a project** → type: **Mod**
- [ ] **Slug**: suggest `fps-ping-monitor` (the mod id `fpsping` may already be taken by someone else — check first; if free, that works too)
- [ ] **Title**: `FPS & Ping Monitor`
- [ ] **Summary** (short, shows in search): paste the one-liner from `fabric.mod.json` → `description`:
  > Customizable FPS and ping overlay: draggable box, themes, custom colors, sparkline graphs, server TPS, RAM/entity stats, and lag alerts. Press O to configure.
- [ ] **Categories**: `utility` (primary). Optionally also `management`/`social` — utility alone is fine.
- [ ] **Environment**: Client-side
- [ ] **License**: MIT
- [ ] **Description body**: paste the contents of `publishing/MODRINTH_PAGE.md` — but first replace the two placeholder link lines (Source / Issues) with your real URLs, and delete any FAQ entries you don't want.
- [ ] **Icon**: upload `src/main/resources/assets/fpsping/icon.png` (256×256).

## 3. Upload the version

- [ ] Run the release build:
  ```bash
  export JAVA_HOME="/c/Users/Acer/AppData/Local/Programs/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
  ./gradlew build
  ```
- [ ] Upload **`build/libs/fpsping-1.1.0.jar`** — *not* the `-sources` jar (optionally upload the sources jar as an **additional file** on the same version; that's good practice).
- [ ] **Version number**: `1.1.0`
- [ ] **Version title**: `FPS & Ping Monitor 1.1.0`
- [ ] **Game versions**: `1.21.11` (the jar is built for exactly this; `~1.21.11` in metadata means 1.21.11.x patches, so only tick those if you actually test them)
- [ ] **Loaders**: `Fabric`
- [ ] **Dependencies** on the version entry:
  - **Required**: [Fabric API](https://modrinth.com/mod/fabric-api)
  - **Optional**: [Mod Menu](https://modrinth.com/mod/modmenu)
  - (**Embedded**: none — Fabric API is a dependency, not embedded)
- [ ] **Changelog**: paste `publishing/CHANGELOG.md` (or just the 1.1.0 section).

## 4. After publishing

- [ ] View your page logged-out to see what visitors see.
- [ ] Test-download the jar from Modrinth and confirm it launches in your game — this catches truncated uploads and metadata mistakes.
- [ ] Link the Modrinth page from your GitHub README (and vice versa).
- [ ] Modrinth runs automated malware scans; with a public source repo this is instant and drama-free.

## 5. Future releases

Once CI is set up (`.github/workflows/build.yml`), releasing is tag-driven:

1. Bump `version` in `gradle.properties`
2. Add a section to `publishing/CHANGELOG.md`
3. Commit, then tag and push:
   ```bash
   git tag v1.1.1 && git push origin main --tags
   ```
4. CI builds, verifies the tag matches the version, creates the GitHub release,
   and publishes to Modrinth (needs the `MODRINTH_TOKEN` repo secret and the
   project ID filled into the workflow's `modrinth-id`)
5. Keep `MODRINTH_PAGE.md` in sync with any new features

> First-time CI setup for Modrinth: create the project + upload v1.1.0 manually
> (sections 2–3 above), then put its ID into `modrinth-id` in the workflow and
> add the `MODRINTH_TOKEN` secret (Modrinth → Settings → Authorization token,
> "Create versions" scope).

### While a project is still waiting on Modrinth review

Modrinth reviews a project's **first version** by hand. During that window the project
can't be resolved through the public API, and the upload endpoint rejects an unresolvable
slug with a 400 `invalid character '-' in base62 encoding` — so `build.yml` deliberately
*suspends* its Modrinth publish instead of failing the release:

- The GitHub release is always created; a deferred Modrinth publish logs a warning
- `.github/workflows/modrinth-watch.yml` runs every 30 minutes, and when it sees that
  the newest tag has no Modrinth version yet (and the project now resolves), it publishes
  it — reusing the jar from the GitHub release, no rebuild
- Once every tag is published the watcher is a no-op: two API calls and it exits

So a release during review needs **nothing** from you — approval alone is enough for the
version to appear. To watch it happen, open the repo's **Actions** tab and look at
"Modrinth publish watcher" (you can also run it by hand from there with *Run workflow*).
Delete that file if you ever prefer publishing by hand.
