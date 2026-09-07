# transdep-demo-app

Demo repo — **do not use in production**.

Test target for [`vuln-remediation-agent`](https://github.com/siddharthoak/vuln-remediation-agent)'s
**transitive-dependency gap**, documented in that repo's `Vuln_Resolution_README.md` §7.

## The chain

```
transdep-demo-app  ->  transdep-demo-service-lib  ->  transdep-demo-util-common
(this repo,               (mid-tier lib)                (declares log4j-core 2.14.1
 no vuln dep                                              directly -- CVE-2021-44228,
 declared directly)                                       Log4Shell)
```

`pom.xml` here declares **only** `transdep-demo-service-lib` (via [JitPack](https://jitpack.io)).
`log4j-core` never appears in this repo's `pom.xml` at all — it's pulled in two hops down.
A scanner walking the resolved dependency graph (`mvn dependency:tree`) will still flag it
against this repo.

## Why this repo exists

`vuln-remediation-agent`'s Fixer (`CodeFixer._bump_pom_version()`) currently only walks
`<dependency>` elements *literally written* in the target repo's `pom.xml` — it doesn't
resolve the effective POM or walk the transitive graph. Pointing `GITHUB_REPO_TARGET` at
this repo reproduces that gap end-to-end:

1. `security-scan.yml` (copied from `vulnerable-java-app`) runs Trivy + Grype against this
   repo and should report the log4j-core CVE even though it's not a direct dependency.
2. The Fixer should fail to find `log4j-core` as a direct `<dependency>` in this `pom.xml`
   and — per the current (buggy) behavior — silently drop the finding rather than routing
   it to a triage issue.

## Setup to use as `GITHUB_REPO_TARGET`

Set in `vuln-remediation-agent`'s `config/.env`:

```
GITHUB_REPO_TARGET=siddharthoak/transdep-demo-app
```

Then trigger the `Security Scan` workflow manually (Actions tab → Security Scan → Run workflow)
or open a PR against `main`, same as the existing `vulnerable-java-app` walkthrough in the
main repo's `README.md`.
