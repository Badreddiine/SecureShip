# Scans every image of the stack with Trivy (run from the project root).
# Trivy runs in a container, so nothing to install. Reports go to .\reports\

$images = @(
    "secureship:naive",        # naive image, for comparison
    "secureship-api:latest",   # multi-stage / non-root image
    "nginx:1.27-alpine",
    "postgres:16-alpine",
    "redis:7-alpine"
)

New-Item -ItemType Directory -Force reports | Out-Null

foreach ($img in $images) {
    $name = $img -replace '[:/]', '_'
    Write-Host "=== $img ===" -ForegroundColor Cyan

    docker run --rm `
        -v /var/run/docker.sock:/var/run/docker.sock `
        -v trivy-cache:/root/.cache `
        -v "${PWD}\reports:/reports" `
        aquasec/trivy:latest image --quiet `
        --severity CRITICAL,HIGH `
        --format table `
        --output "/reports/$name.txt" `
        $img

    # Only CRITICAL, only fixes that exist (what you can act on)
    docker run --rm `
        -v /var/run/docker.sock:/var/run/docker.sock `
        -v trivy-cache:/root/.cache `
        aquasec/trivy:latest image --quiet `
        --severity CRITICAL --ignore-unfixed `
        $img
}