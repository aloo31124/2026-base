import { FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../app/api";
import AppShell from "../components/AppShell";
import type { MyTask } from "./MyTasksPage";

export default function MyTaskEditPage() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const [task, setTask] = useState<MyTask | null>(null);
  const [workStatus, setWorkStatus] = useState("PENDING");
  const [progressContent, setProgressContent] = useState("");
  const [progressPercent, setProgressPercent] = useState(10);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  /** 載入本人任務明細並初始化編輯欄位。 */
  const load = async () => {
    try {
      const data = await api<MyTask>(`/task-assignment/inbox/${id}`);
      setTask(data);
      setWorkStatus(data.workStatus);
      setProgressContent(data.progressContent ?? "");
      setProgressPercent(data.progressPercent);
    } catch (reason) {
      setError((reason as Error).message);
    }
  };
  useEffect(() => {
    void load();
  }, [id]);

  /** 保存工作狀態、內容及十等分進度。 */
  const save = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api(`/task-assignment/tasks/${id}/progress`, {
        method: "PUT",
        body: JSON.stringify({ workStatus, progressContent, progressPercent }),
      });
      setMessage("工作進度更新成功。");
      setError("");
      await load();
    } catch (reason) {
      setError((reason as Error).message);
    }
  };

  /** 將選取檔案轉為 Base64 後上傳。 */
  const upload = async (file?: File) => {
    if (!file) return;
    const base64Content = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result).split(",")[1]);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
    try {
      await api(`/task-assignment/tasks/${id}/attachments`, {
        method: "POST",
        body: JSON.stringify({
          fileName: file.name,
          contentType: file.type || "application/octet-stream",
          base64Content,
        }),
      });
      setMessage("附件上傳成功。");
      await load();
    } catch (reason) {
      setError((reason as Error).message);
    }
  };

  /** 執行編輯頁的提交、退回或延期流程。 */
  const action = async (kind: "submit" | "return" | "extend") => {
    try {
      if (kind === "submit")
        await api(`/task-assignment/tasks/${id}/submit`, { method: "POST" });
      if (kind === "return") {
        const reason = window.prompt("請輸入退回原因");
        if (!reason) return;
        await api(`/task-assignment/tasks/${id}/return`, {
          method: "POST",
          body: JSON.stringify({ reason }),
        });
      }
      if (kind === "extend") {
        const reason = window.prompt("請輸入申請延期原因");
        if (!reason) return;
        await api(`/task-assignment/tasks/${id}/extension-requests`, {
          method: "POST",
          body: JSON.stringify({ reason }),
        });
      }
      navigate("/my-tasks");
    } catch (reason) {
      setError((reason as Error).message);
    }
  };

  return (
    <AppShell>
      <div className="content my-task-edit-content">
        <header className="page-heading">
          <div>
            <div className="eyebrow">Task progress</div>
            <h1>{task?.name ?? "我的任務編輯"}</h1>
            <p>{task?.content}</p>
          </div>
          <button
            className="btn secondary"
            onClick={() => navigate("/my-tasks")}
          >
            返回列表
          </button>
        </header>
        <form className="card my-task-edit-card" onSubmit={save}>
          <section className="form-section">
            <h2>工作進度</h2>
            <div className="form-grid">
              <label>
                狀態
                <select
                  data-testid="my-task-work-status"
                  value={workStatus}
                  onChange={(e) => setWorkStatus(e.target.value)}
                >
                  <option value="PENDING">待處理</option>
                  <option value="IN_PROGRESS">進行中</option>
                  <option value="COMPLETED">已完成</option>
                </select>
              </label>
              <label className="full">
                內容
                <textarea
                  data-testid="my-task-progress-content"
                  maxLength={4000}
                  value={progressContent}
                  onChange={(e) => setProgressContent(e.target.value)}
                />
              </label>
            </div>
            <label className="progress-field">
              進度：{progressPercent}%
              <input
                data-testid="my-task-progress"
                type="range"
                min="10"
                max="100"
                step="10"
                value={progressPercent}
                onChange={(e) => setProgressPercent(Number(e.target.value))}
              />
              <div className="progress-track">
                <span style={{ width: `${progressPercent}%` }} />
              </div>
            </label>
            <div className="actions">
              <button className="btn primary" data-testid="my-task-save">
                儲存進度
              </button>
            </div>
          </section>
        </form>
        <section className="card attachment-card">
          <h2>附件</h2>
          <label className="upload-control">
            上傳圖片、附件或影片
            <input
              data-testid="my-task-attachment"
              type="file"
              accept="image/*,video/*,.pdf,.doc,.docx,.xls,.xlsx,.zip"
              onChange={(e) => void upload(e.target.files?.[0])}
            />
          </label>
          <ul>
            {task?.attachments.map((item) => (
              <li key={item.id}>
                {item.fileName}（{Math.ceil(item.fileSize / 1024)} KB）
              </li>
            ))}
          </ul>
        </section>
        <section className="card workflow-actions">
          <h2>任務操作</h2>
          <button
            className="btn primary"
            data-testid="my-task-submit"
            onClick={() => void action("submit")}
          >
            提交審核
          </button>
          <button
            className="btn secondary"
            onClick={() => void action("return")}
          >
            退回
          </button>
          <button
            className="btn secondary"
            onClick={() => void action("extend")}
          >
            申請延期
          </button>
        </section>
        {(message || error) && (
          <p
            className={error ? "error" : "success"}
            data-testid="my-task-message"
          >
            {error || message}
          </p>
        )}
      </div>
    </AppShell>
  );
}
