# Project Chimera — Makefile (enterprise Java / Maven workflow)
#
# ASSUMPTIONS
# -----------
# - JDK 21+ installed; JAVA_HOME points at the JDK (required by Maven).
# - GNU Make available (Git Bash, WSL, macOS, Linux). Native Windows CMD does not ship Make;
#   use Git Bash here, or invoke Maven directly:  mvnw.cmd <goal>
# - Maven Wrapper (./mvnw, mvnw.cmd) is the canonical build entrypoint so a global `mvn`
#   install is optional.
# - `make test` runs the full Surefire suite (JUnit 5), including sources under `tests/`
#   (build-helper-maven-plugin). Contract tests are expected to fail until implementations
#   and Skill SPI registrations exist (TDD empty slots).
# - Checkstyle is invoked via `mvn checkstyle:check` (no execution bound to a lifecycle phase in
#   pom.xml); `make verify` runs `mvn verify` then Checkstyle in one step.

.PHONY: setup test lint clean verify spec-check docker-build docker-run docker-test help

# Wrapper script (Unix / Git Bash). On Windows CMD/PowerShell: substitute mvnw.cmd manually.
MVN := ./mvnw

## setup: Resolve dependencies, compile, package; skip tests (local prep / CI cache warm-up)
setup:
	$(MVN) clean install -DskipTests

## test: Run all JUnit 5 tests (Surefire); expect failures until scaffolds are implemented
test:
	$(MVN) test

## lint: Static analysis — Checkstyle (checkstyle.xml, maven-checkstyle-plugin)
lint:
	$(MVN) checkstyle:check

## clean: Remove build output (target/)
clean:
	$(MVN) clean

## verify: Maven verify phase (compile, test, package) plus Checkstyle — use `make clean verify` for CI-style clean build
verify:
	$(MVN) verify checkstyle:check

## spec-check: Sanity-check ratified spec files (non-Java)
spec-check:
	@test -s specs/_meta.md                  || (echo "missing/empty: specs/_meta.md" && exit 1)
	@test -s specs/functional.md             || (echo "missing/empty: specs/functional.md" && exit 1)
	@test -s specs/technical.md              || (echo "missing/empty: specs/technical.md" && exit 1)
	@test -s specs/openclaw_integration.md   || (echo "missing/empty: specs/openclaw_integration.md" && exit 1)
	@test -s CLAUDE.md                       || (echo "missing/empty: CLAUDE.md" && exit 1)
	@test -s .cursor/rules/CLAUDE.md         || (echo "missing/empty: .cursor/rules/CLAUDE.md" && exit 1)

## docker-build: OCI image (Spring Boot JAR). Tests skipped during package — use `make test` for the suite
docker-build:
	docker build -t chimera:latest .

## docker-run: Run container on port 8080 (foreground; Ctrl+C stops)
docker-run:
	docker run --rm -p 8080:8080 --name chimera chimera:latest

## docker-test: Build image only; smoke-run the app yourself with `make docker-run`
docker-test: docker-build
	@echo "Image chimera:latest built. Smoke: make docker-run  (then open http://localhost:8080)"

## help: List targets
help:
	@grep -E '^##' Makefile | sed 's/^## /  /'
