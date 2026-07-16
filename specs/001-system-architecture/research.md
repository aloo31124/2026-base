# Research：系統架構設計

## 目錄

- [技術版本](#技術版本) — 官方穩定版選擇
- [建置整合](#建置整合) — Gradle Node plugin

## 技術版本

**Decision**：Spring Boot 4.1.0、Gradle 9.6.1、React 19.2.7；Java 21。

**Rationale**：前三者為 2026-07-16 官方穩定線；Java 21 是本機已安裝、Boot 4.1 支援且能立即驗證的 LTS。

**Alternatives considered**：Java 26 最新功能版需要額外安裝，降低本機重現性。

## 建置整合

**Decision**：`backend/build.gradle` 使用 Node Gradle plugin 下載 Node LTS 並執行 `../frontend` build。

**Rationale**：滿足 Gradle 設定只在 backend 且單一命令建置兩端。

