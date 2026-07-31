import { useId, useState, type InputHTMLAttributes } from 'react';

type PasswordInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  'data-testid'?: string;
};

/**
 * 密碼輸入框，附「檢視密碼」切換 icon。
 *
 * 切換僅改變本地顯示狀態，不會影響表單值與送出內容；
 * 各頁面沿用原有的 data-testid，只需把 <input type="password"> 換成本元件。
 */
export default function PasswordInput({ id, ...inputProps }: PasswordInputProps) {
  const [visible, setVisible] = useState(false);
  const fallbackId = useId();
  const inputId = id ?? fallbackId;
  const testId = inputProps['data-testid'] ?? inputId;

  return <span className="password-field">
    <input {...inputProps} id={inputId} type={visible ? 'text' : 'password'} />
    <button
      type="button"
      className="password-toggle"
      data-testid={`${testId}-toggle`}
      aria-controls={inputId}
      aria-pressed={visible}
      aria-label={visible ? '隱藏密碼' : '檢視密碼'}
      title={visible ? '隱藏密碼' : '檢視密碼'}
      onClick={() => setVisible(current => !current)}
    >
      {visible ? <EyeOffIcon /> : <EyeIcon />}
    </button>
  </span>;
}

/** 顯示中的眼睛 icon。 */
function EyeIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path d="M12 5c5 0 9 4.5 10 7-1 2.5-5 7-10 7S3 14.5 2 12c1-2.5 5-7 10-7Z" />
    <circle cx="12" cy="12" r="3.2" />
  </svg>;
}

/** 已隱藏的眼睛 icon（加上斜線）。 */
function EyeOffIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path d="M12 5c5 0 9 4.5 10 7-1 2.5-5 7-10 7S3 14.5 2 12c1-2.5 5-7 10-7Z" />
    <circle cx="12" cy="12" r="3.2" />
    <path d="M4 20 20 4" />
  </svg>;
}
