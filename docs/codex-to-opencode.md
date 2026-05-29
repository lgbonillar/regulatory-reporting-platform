# Codex to OpenCode Handover

## Objetivo del trabajo

Consolidar la base documental del proyecto para operar con enfoque SDD (Specification/Documentation Driven Development), dejando alineados los documentos operativos principales con el estado real del sistema:

- `AGENTS.md` (raíz y por áreas)
- `README.md` por área
- `docs/` como fuente de verdad funcional y técnica
- guías para `postman/` y `samples/`

También se buscó mantener coherencia entre arquitectura implementada, flujo de negocio real (upload validation + processing workflow), seeds y pruebas API.

## Decisiones tomadas

1. Mantener la arquitectura backend actual y documentarla, sin refactor adicional estructural en esta etapa.
2. Tratar `docs/` como fuente de verdad inmediata usando archivos planos existentes (`architecture.md`, `api-spec.md`, `business-rules.md`, `database-model.md`, `deployment.md`) y no forzar todavía carpetas `docs/specs`, `docs/adr`, `docs/api`.
3. Documentar Node en frontend con versión explícita y uso de `.nvmrc`.
4. Separar responsabilidades documentales:
   - README: guía humana de uso y setup.
   - AGENTS: reglas operativas para agentes/automatización.
5. Estandarizar backend docs con estado real:
   - paquete base `dev.lgbonillar.regreporting`
   - módulos reales `config/shared/users/upload/processing/modules`
   - endpoints reales de auth, report-files y processing-jobs.
6. Completar documentación faltante de `postman/` y `samples/`.
7. Agregar `docs/testing-strategy.md` para cerrar el circuito SDD con criterios de validación.

## Errores encontrados

1. Desalineación documental previa:
   - `backend/AGENTS.md` tenía paquete base viejo (`com.mrcrafterman...`) y estructura no vigente.
   - `AGENTS.md` raíz referenciaba jerarquía `docs/specs|adr|api` inexistente en el árbol real.
2. Faltaban documentos clave:
   - `backend/README.md` no existía.
   - no existían `postman/README.md`, `postman/AGENTS.md`, `samples/README.md`, `samples/AGENTS.md`.
3. `docs/*.md` estaban vacíos al momento de esta sesión.
4. Inconsistencia potencial en expectativas de API por evolución de flujos (se resolvió documentando contratos actuales en `docs/api-spec.md`).

## Pendientes

1. Revisar y, si aplica, versionar formalmente los commits/documentación por bloques (frontend/docs/backend/docs).
2. Evaluar si ya conviene migrar de docs planos a estructura:
   - `docs/specs/`
   - `docs/adr/`
   - `docs/api/`
3. Crear primeros ADRs formales (si deciden activar estructura ADR):
   - validación en upload-time
   - envelope estándar de respuestas
   - modelo JWT + refresh + active session
4. Verificar que `README.md` raíz enlace solo a documentos existentes en todo momento.
5. Alinear documentación con cambios futuros de permisos (ejemplo: capacidades de AUDITOR cuando se habiliten endpoints de eventos/reportes).

## Próximos pasos recomendados

1. Validación de consistencia documental
   - Ejecutar una revisión rápida de enlaces y rutas en todos los `README.md` y `AGENTS.md`.

2. Cierre de gobernanza SDD
   - Definir regla de PR/commit: cambios de comportamiento deben incluir actualización de `docs/business-rules.md` + `docs/api-spec.md` + seeds/Postman si aplica.

3. Consolidar contrato backend-frontend
   - Usar `docs/api-spec.md` como referencia única para DTOs frontend y pruebas Postman.

4. Preparar siguiente bloque funcional
   - Con base en el estado actual, continuar con:
     - pruebas E2E manuales Postman sobre flujos críticos
     - integración frontend de findings/validation-runs donde falte
     - endurecer permisos por rol en endpoints pendientes

5. Opcional inmediato
   - Incluir `docs/testing-strategy.md` en la jerarquía descrita en `AGENTS.md` raíz para hacerlo explícito como documento obligatorio de referencia.

## Archivos creados/actualizados relevantes en esta sesión

- Actualizados:
  - `AGENTS.md` (raíz)
  - `backend/AGENTS.md`
  - `docs/architecture.md`
  - `docs/business-rules.md`
  - `docs/api-spec.md`
  - `docs/database-model.md`
  - `docs/deployment.md`

- Creados:
  - `backend/README.md`
  - `postman/README.md`
  - `postman/AGENTS.md`
  - `samples/README.md`
  - `samples/AGENTS.md`
  - `docs/testing-strategy.md`
  - `docs/codex-to-opencode.md` (este archivo)
