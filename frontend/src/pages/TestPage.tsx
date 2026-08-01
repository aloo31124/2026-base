import { useEffect, useState, type FormEvent } from "react";
import { api } from "../app/api";
import AppShell from "../components/AppShell";
import ActionIconButton from "../components/ActionIconButton";

interface Row {
  id: string;
  name: string;
  description: string;
  testStatus: string;
}

export default function TestPage() {
  const [rows, setRows] = useState<Row[]>([]);
  const [name, setName] = useState("連線測試");
  const [error, setError] = useState("");

  async function refresh() {
    try {
      setRows(await api<Row[]>("/test/testTemp"));
    } catch (e) {
      setError((e as Error).message);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  async function add(event: FormEvent) {
    event.preventDefault();
    await api<Row>("/test/testTemp", {
      method: "POST",
      body: JSON.stringify({
        name,
        description: "前後端與 MSSQL CRUD 驗證",
        testStatus: "READY",
      }),
    });
    await refresh();
  }

  async function update(row: Row) {
    await api<Row>(`/test/testTemp/${row.id}`, {
      method: "PUT",
      body: JSON.stringify({
        ...row,
        name: `${row.name}-已更新`,
        testStatus: "DONE",
      }),
    });
    await refresh();
  }

  async function remove(id: string) {
    await api(`/test/testTemp/${id}`, { method: "DELETE" });
    await refresh();
  }

  return (
    <AppShell>
      <div className="content">
        <header className="page-heading">
          <div>
            <p className="eyebrow">Database connectivity</p>
            <h1>資料連線測試</h1>
            <p>路由 test/testTemp/，驗證 test 資料表增刪改查。</p>
          </div>
        </header>

        <form className="card inline-form" onSubmit={add}>
          <input
            data-testid="test-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <button data-testid="create-test" className="btn primary">
            新增測試資料
          </button>
        </form>

        {error && <p className="error">{error}</p>}

        <section className="card">
          <table>
            <thead>
              <tr>
                <th>名稱</th>
                <th>描述</th>
                <th>狀態</th>
                <th>操作</th>
              </tr>
            </thead>

            <tbody>
              {rows.map((row) => (
                <tr key={row.id} data-testid="test-row">
                  <td data-label="名稱">{row.name}</td>
                  <td data-label="描述">{row.description}</td>
                  <td data-label="狀態">
                    <span className="tag">{row.testStatus}</span>
                  </td>
                  <td data-label="操作">
                    <div className="table-actions">
                      <ActionIconButton
                        label="編輯"
                        icon="✎"
                        onClick={() => void update(row)}
                      />
                      <ActionIconButton
                        label="刪除"
                        icon="×"
                        tone="danger"
                        onClick={() => void remove(row.id)}
                      />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </AppShell>
  );
}
