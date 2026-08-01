import type { ButtonHTMLAttributes, ReactNode } from 'react';

type ActionIconButtonProps = Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'children'> & {
  label: string;
  icon: ReactNode;
  tone?: 'default' | 'danger';
};

/** 表格列操作共用的精簡圖示按鈕；文字透過 tooltip 與 aria-label 保留。 */
export default function ActionIconButton({ label, icon, tone = 'default', className = '', ...props }: ActionIconButtonProps) {
  return <button
    {...props}
    type={props.type ?? 'button'}
    className={`action-icon-button ${tone === 'danger' ? 'danger' : ''} ${className}`.trim()}
    aria-label={label}
    title={label}
  >
    <span aria-hidden="true">{icon}</span>
  </button>;
}
