# Simple PowerShell helper to add, commit and push changes from the repo root.
# Run it in PowerShell from the repository root: `scripts\commit-and-push.ps1`.

try {
    $branch = git rev-parse --abbrev-ref HEAD 2>$null
} catch {
    Write-Error "No parece ser un repositorio Git. Ejecuta desde la raíz del repo."
    exit 1
}

Write-Host "Branch actual: $branch"

$changes = git status --porcelain
if ([string]::IsNullOrWhiteSpace($changes)) {
    Write-Host "No hay cambios para commitear." -ForegroundColor Yellow
    exit 0
}

$message = Read-Host "Mensaje de commit"
if ([string]::IsNullOrWhiteSpace($message)) {
    Write-Host "Commit cancelado (mensaje vacío)." -ForegroundColor Red
    exit 1
}

git add .
git commit -m "$message"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Commit falló." -ForegroundColor Red
    exit 1
}

git push -u origin $branch
if ($LASTEXITCODE -ne 0) {
    Write-Host "Push falló. Revisa permisos o la configuración remota." -ForegroundColor Red
    exit 1
}

Write-Host "Push completado a origin/$branch" -ForegroundColor Green
