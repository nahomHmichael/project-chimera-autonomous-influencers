.PHONY: setup test lint spec-check docker-test help

## Uses Maven Wrapper (./mvnw) so Apache Maven does not need to be on PATH.
## Windows CMD/PowerShell: run `mvnw.cmd test` instead of `make test`.
## Requires JDK 21+ and JAVA_HOME pointing at the JDK (see `java -version`).

## setup: Install dependencies (skip tests)
setup:
	./mvnw clean install -DskipTests

## test: Run all JUnit 5 tests (expect failures — TDD empty slots)
test:
	./mvnw test

## lint: Run Checkstyle code quality checks
lint:
	./mvnw checkstyle:check

## spec-check: Verify all spec files exist and are non-empty
spec-check:
	@echo "🔍 Checking spec files..."
	@test -s specs/_meta.md       && echo "✅ specs/_meta.md" || (echo "❌ specs/_meta.md missing/empty" && exit 1)
	@test -s specs/functional.md  && echo "✅ specs/functional.md" || (echo "❌ specs/functional.md missing/empty" && exit 1)
	@test -s specs/technical.md   && echo "✅ specs/technical.md" || (echo "❌ specs/technical.md missing/empty" && exit 1)
	@test -s CLAUDE.md            && echo "✅ CLAUDE.md" || (echo "❌ CLAUDE.md missing/empty" && exit 1)
	@echo "✅ All spec checks passed"

## docker-test: Build Docker image and run tests inside container (Bonus)
docker-test:
	docker build -t chimera-test .
	docker run --rm chimera-test mvn test

## help: Show available commands
help:
	@grep -E '^##' Makefile | sed 's/## //'
