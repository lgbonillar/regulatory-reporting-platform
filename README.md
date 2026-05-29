# Regulatory Reporting Platform

Full-stack platform for Excel validation, regulatory processing, audit tracking, and enterprise-style workflow simulation.

This repository is organized as a monorepo and is intended to be easy to run locally through Docker Compose.

## What this system is

The platform simulates a regulatory reporting workflow where users upload Excel files, the system validates them, processing
jobs are executed, and the results can be approved, rejected, or revoked depending on the workflow state.

The project is designed to stay modular so future regulatory modules can be added without losing architectural context.


## <img src="https://cdn.simpleicons.org/docker/2496ED" width="20" height="20" alt="Docker logo" /> Run Locally with Docker

The recommended way to test the full system locally is by using [Docker Compose](https://docs.docker.com/compose/) from the repository root.

### 1. Start the platform

```bash
docker compose up -d
docker compose ps
docker compose down
docker compose up -d --build
```

## 🌐 Local URLs

| Service              | URL                                                                              |
| -------------------- | -------------------------------------------------------------------------------- |
| Frontend application | [`http://localhost:4200`](http://localhost:4200)                                 |
| Backend API          | [`http://localhost:8080`](http://localhost:8080)                                 |
| Backend health check | [`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health) |

## 🛠️ Area guides

Area-specific development instructions live in their own folders:

| Area                           | Guide                                      |
| ------------------------------ | ------------------------------------------ |
| Backend development            | [backend/README.md](./backend/README.md)   |
| Frontend development           | [frontend/README.md](./frontend/README.md) |
| Database scripts and seed data | [database/README.md](./database/README.md) |
| Postman workflows              | [postman/README.md](./postman/README.md)   |
| Sample Excel files             | [samples/README.md](./samples/README.md)   |
