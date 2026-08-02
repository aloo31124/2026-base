type JwtPayload = {
  exp?: unknown;
};

/**
 * 只讀取 JWT payload 的 exp 供前端顯示；簽章與授權仍由後端驗證。
 */
export function getSessionExpiresAt(token: string): number | null {
  try {
    const payloadPart = token.split(".")[1];
    if (!payloadPart) return null;

    const base64 = payloadPart.replaceAll("-", "+").replaceAll("_", "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const bytes = Uint8Array.from(atob(padded), (character) =>
      character.charCodeAt(0),
    );
    const payload = JSON.parse(new TextDecoder().decode(bytes)) as JwtPayload;
    if (
      typeof payload.exp !== "number" ||
      !Number.isFinite(payload.exp) ||
      payload.exp <= 0
    ) {
      return null;
    }

    return payload.exp * 1000;
  } catch {
    return null;
  }
}

/** 依絕對到期時間重算秒數，避免背景分頁節流造成倒數失真。 */
export function getRemainingSeconds(
  expiresAt: number,
  now = Date.now(),
): number {
  return Math.max(0, Math.ceil((expiresAt - now) / 1000));
}

/** 將剩餘秒數固定格式化為 HH:MM:SS；小時可超過 24。 */
export function formatRemainingTime(totalSeconds: number): string {
  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(safeSeconds / 3600);
  const minutes = Math.floor((safeSeconds % 3600) / 60);
  const seconds = safeSeconds % 60;
  return [hours, minutes, seconds]
    .map((value) => String(value).padStart(2, "0"))
    .join(":");
}
