# Contribuir al proyecto Proyecto-FinanceFlow

Este documento explica un flujo simple para que puedas hacer tus commits y mostrar tus avances en el repositorio remoto.

- **Configurar identidad (una sola vez)**
  - `git config --global user.name "Tu Nombre"`
  - `git config --global user.email "tu@correo.com"`

- **Flujo recomendado**
  1. Crea una rama por cada tarea/avance: `git checkout -b work/<tu-nombre>-descripcion`.
  2. Haz cambios locales y prueba la aplicación.
  3. Usa el script `scripts\commit-and-push.ps1` o estos comandos:
     - `git add .`
     - `git commit -m "feat: descripcion corta del cambio"`
     - `git push -u origin <tu-rama>`

- **Convenciones sugeridas para mensajes de commit**
  - `feat: ` para nuevas funcionalidades
  - `fix: ` para correcciones de bug
  - `chore: ` para tareas de mantenimiento

- **Ver tus avances**
  - En GitHub (o el remoto) podrás ver tus commits en la rama que hayas empujado.
  - Usa `git log --oneline --decorate --graph` para ver el historial localmente.

- **Script de ayuda**
  - Ejecuta `scripts\commit-and-push.ps1` desde la raíz del repo (PowerShell) para añadir, commitear y pushear con un prompt de mensaje.

Si quieres, puedo hacer los commits y push iniciales de estos archivos por ti — dime si quieres que los suba a `origin` ahora.
