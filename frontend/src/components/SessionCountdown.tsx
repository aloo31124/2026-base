import { useEffect, useMemo, useRef, useState } from "react";
import {
  formatRemainingTime,
  getRemainingSeconds,
  getSessionExpiresAt,
} from "../features/auth/sessionExpiry";

const WARNING_THRESHOLD_SECONDS = 5 * 60;

export default function SessionCountdown({
  token,
  onExpire,
}: {
  token: string;
  onExpire: () => void;
}) {
  const expiresAt = useMemo(() => getSessionExpiresAt(token), [token]);
  const [remainingSeconds, setRemainingSeconds] = useState(() =>
    expiresAt === null ? null : getRemainingSeconds(expiresAt),
  );
  const trackedExpiry = useRef<number | null>(null);
  const expiryHandled = useRef(false);

  if (trackedExpiry.current !== expiresAt) {
    trackedExpiry.current = expiresAt;
    expiryHandled.current = false;
  }

  useEffect(() => {
    if (expiresAt === null) {
      setRemainingSeconds(null);
      return;
    }

    const syncCountdown = () => {
      const next = getRemainingSeconds(expiresAt);
      setRemainingSeconds(next);
      if (next === 0 && !expiryHandled.current) {
        expiryHandled.current = true;
        onExpire();
      }
    };

    syncCountdown();
    const intervalId = window.setInterval(syncCountdown, 1000);
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") syncCountdown();
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);
    window.addEventListener("focus", syncCountdown);
    return () => {
      window.clearInterval(intervalId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      window.removeEventListener("focus", syncCountdown);
    };
  }, [expiresAt, onExpire]);

  if (remainingSeconds === null) return null;

  const warning = remainingSeconds <= WARNING_THRESHOLD_SECONDS;
  return (
    <span
      className={`session-countdown${warning ? " warning" : ""}`}
      data-testid="session-countdown"
      role="timer"
      aria-label={`登入剩餘時間 ${formatRemainingTime(remainingSeconds)}`}
      aria-live={warning ? "polite" : "off"}
    >
      <span className="session-countdown-label">登入倒數</span>
      <strong data-testid="session-countdown-value">
        {formatRemainingTime(remainingSeconds)}
      </strong>
    </span>
  );
}
