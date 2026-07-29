---
name: company-supervisor-management
description: 維護 AgentFlow 公司 CRUD、既有使用者主管身分、公司主管綁定、一人一公司限制，以及對應管理 API/React 頁面時使用。
---

# 公司主管管理

## 目錄

- [資料契約](#資料契約) — 三張表與唯一性
- [主管身分契約](#主管身分契約) — 既有使用者與角色
- [綁定契約](#綁定契約) — 多主管與一人一公司
- [刪除與權限契約](#刪除與權限契約) — 關聯保護及 RBAC

## 資料契約

`company` 以 `nvarchar` 保存忽略大小寫唯一的公司名稱與說明；`supervisor_profile` 以 `user_id` 一對一保存主管職稱，職稱同樣使用 `nvarchar`；`company_membership` 以 `user_id` 唯一連接公司。三者皆繼承 `BaseEntity` 的 UUID 與稽核時間。

## 主管身分契約

主管只能由存在且啟用的 `UserAccount` 建立。建立主管資料時附加 `MANAGER` 角色；同一使用者不可有多筆主管資料。主管資料保存職稱，不複製帳號、姓名或信箱。

## 綁定契約

一家公司可有多筆主管或員工成員；同一使用者在 `company_membership` 最多一筆，因此主管與員工都只能屬於一家公司。取消主管綁定後，才可改綁其他公司。

## 刪除與權限契約

仍有成員的公司、仍有公司綁定的主管均拒絕刪除，使用者必須先明確取消綁定。所有 `/api/admin/company-supervisor-management/**` 與 `/company-supervisor-management` 僅允許 `SYSTEM_ADMIN`；403 訊息使用「[公司主管管理]」模組名稱。
