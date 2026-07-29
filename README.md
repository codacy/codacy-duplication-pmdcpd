[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9b4cb2fec91146649dcf514278f24eab)](https://www.codacy.com/gh/codacy/codacy-duplication-pmdcpd?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-duplication-pmdcpd&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9b4cb2fec91146649dcf514278f24eab)](https://www.codacy.com/gh/codacy/codacy-duplication-pmdcpd?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-duplication-pmdcpd&utm_campaign=Badge_Coverage)
[![Build Status](https://circleci.com/gh/codacy/codacy-duplication-pmdcpd.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-duplication-pmdcpd)
[![Docker Version](https://images.microbadger.com/badges/version/codacy/codacy-duplication-pmdcpd.svg)](https://microbadger.com/images/codacy/codacy-duplication-pmdcpd "Get your own version badge on microbadger.com")

# Codacy Duplication PMD CPD

This is the duplication docker we use at Codacy to have [PMD CPD](https://pmd.github.io/) support.
You can also create a docker to integrate the tool and language of your choice!
             
## Usage

You can create the docker by doing:

```bash
./scripts/publish.sh
```

The docker is ran with the following command:

```bash
docker run -it -v $srcDir:/src  <DOCKER_NAME>:<DOCKER_VERSION>
docker run -it -v $PWD/src/test/resources:/src codacy/codacy-duplication-pmdcpd:latest
```

## Test

```bash
./scripts/test.sh
```

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo — most commonly bumping the wrapped PMD CPD version, but also base image / orb / dependency bumps. Follow it top to bottom; it tells you what to change, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy duplication engine**: a Scala application (`src/main/scala/com/codacy/duplication/pmd/`, built on `codacy-duplication-scala-seed`) that packages [PMD's CPD (Copy/Paste Detector)](https://pmd.github.io/) as a Docker image Codacy's platform runs against a customer's source code to detect duplicated code blocks. It is **not** a pattern-based linting engine — there is no `docs/patterns.json`, no per-rule configuration, and no `DocGenerator`. The tool has one job: run CPD across the supported languages (Apex, C#, C, C++, JavaScript, JSP, Java, Go, Swift, Python, Scala, PLSQL, VisualForce — see the `allLanguages` list in `src/main/scala/com/codacy/duplication/pmd/Cpd.scala`) and translate its output into Codacy's `DuplicationClone` format (`src/main/scala/com/codacy/duplication/pmd/Duplication.scala`).

The `docs/` directory here only holds **test fixtures** (`docs/duplication-tests/<language>-<with|no>-results/`, each with a `src/` folder of sample code and an expected `results.xml`) used by `codacy-plugins-test` in duplication mode. There is no generated documentation to regenerate on a version bump — this repo has no patterns/description files at all.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `project/Dependencies.scala` → `pmdVersion` | Which PMD (and therefore CPD) release is bundled, applied uniformly to every `pmd-<language>` artifact | Bump to the target version; confirm all the per-language PMD modules (`pmd-apex`, `pmd-scala`, `pmd-java`, etc.) publish that version to Maven Central. |
| `project/Dependencies.scala` → `Codacy.duplicationSeed` | The `codacy-duplication-scala-seed` version (shared duplication-engine scaffolding/API) | Bump only if the task specifically calls for a seed upgrade. |
| `project/plugins.sbt` → `codacy-sbt-plugin` | sbt build-plugin providing Codacy's shared sbt tasks (`codacyCoverage`, etc.) | Check the latest published version if asked to bump build tooling. |
| `.circleci/config.yml` → `codacy/base` orb | Shared CircleCI steps (`checkout_and_version`, `sbt`, `publish_docker`, `tag_version`) | Check the latest published version. |
| `.circleci/config.yml` → `codacy/plugins-test` orb | Runs `codacy-plugins-test` in CI | Same as above. |
| `Dockerfile` → base image (currently `amazoncorretto:8-alpine3.17-jre`) | Runtime the packaged app runs on | Only bump if the new PMD version raises its minimum JRE requirement, or as a standalone security/base-image bump. |
| `project/build.properties` → `sbt.version` | sbt version used to build | Only bump if specifically scoped. |

There is no `docs/patterns.json` or `DocGenerator` step in this repo — do not look for one.

### 3. Step-by-step update procedure

1. **Bump the version(s)** as scoped by the task (most commonly just `pmdVersion` in `project/Dependencies.scala`).
2. **Compile** with `sbt compile` and `sbt test:compile` (see `scripts/compile.sh`).
3. **Lint/format** with `scripts/lint.sh` (`sbt scalafmtCheck`, `sbt scapegoat`, `sbt "scalafixCli --test"`).
4. **Run the native test suite** with `scripts/test.sh` (`sbt coverage test`, `sbt coverageReport`/`coverageAggregate`; this also exercises `CpdSpec.scala` against the fixtures under `src/test/resources`).
5. **Build the Docker image locally**: `sbt docker:publishLocal` (or `./scripts/publish.sh` to stage a snapshot-tagged image), which stages the app and builds the image from the `Dockerfile`.
6. **Run `codacy-plugins-test` locally** before pushing — clone https://github.com/codacy/codacy-plugins-test and run its duplication-mode Docker tests (CI invokes this orb with `run_duplication_tests: true`, `run_json_tests: false`) against your local image tag, validating each `docs/duplication-tests/<language>-*` fixture still produces the expected `results.xml`.
7. **Iterate on failures**, re-running only the relevant test command after each fix. A PMD/CPD version bump can change tokenization or clone-detection output, so pay close attention to any fixture whose expected `results.xml` no longer matches — decide whether the fixture needs updating or the change is a genuine regression.
8. **Commit** the version bump(s) in one change (and any fixture updates that were genuinely required by upstream behavior changes, not just to make the diff pass).
9. **Push and open a PR.**
10. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. **The CI environment's toolchain can differ from your local one**, so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human.

### 4. Common failure modes and fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| A `docs/duplication-tests/<language>-*` fixture's `results.xml` no longer matches after a PMD bump | CPD's tokenizer or clone-detection heuristics changed between PMD versions (this has happened before, e.g. PMD 5.x → 6.15 required raising the minimum token count to keep results stable) | Compare actual vs. expected output; if the new output is correct/expected given the version bump, update the fixture's `results.xml` deliberately (do not blindly regenerate everything). |
| `pmd-<language>` dependency fails to resolve at the new version | Not every PMD module version is published in lockstep, or a language module was renamed/dropped upstream | Check Maven Central for that exact `pmd-<language>` artifact/version before committing to the bump. |

### 5. Definition of done

- Version bump(s) reflected in every file from the table in section 2 that applies to this task.
- `scripts/compile.sh` and `scripts/lint.sh` pass locally.
- `scripts/test.sh` passes locally.
- Docker image builds successfully (`sbt docker:publishLocal` / `./scripts/publish.sh`).
- `codacy-plugins-test` duplication-mode tests all pass locally against the freshly built image.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure until all pass.

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.

