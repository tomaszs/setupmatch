# Run backend Gradle tests in Docker with a persistent Gradle cache (no re-download each run).
param(
    [string]$TestFilter = ""
)

$repoRoot = Split-Path $PSScriptRoot -Parent
$backend = Join-Path $repoRoot "backend"

docker compose -f (Join-Path $repoRoot "docker-compose.test.yml") up -d --wait postgres

$gradleArgs = "./gradlew test --no-daemon --gradle-user-home=/root/.gradle --project-cache-dir=/root/.gradle/project-cache"
if ($TestFilter) {
    $gradleArgs += " --tests $TestFilter"
}

$output = docker run --rm `
    -v "${backend}:/workspace/backend" `
    -v setupmatch-gradle-cache:/root/.gradle `
    -w /workspace/backend `
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/setupmatch `
    -e SPRING_DATASOURCE_USERNAME=setupmatch `
    -e SPRING_DATASOURCE_PASSWORD=setupmatch `
    eclipse-temurin:21-jdk-alpine `
    sh -c $gradleArgs 2>&1

$output | Write-Host
exit $LASTEXITCODE
