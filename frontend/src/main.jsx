import React, { useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import { api, credentials, clear } from "./api";
import "./styles.css";
const nav = [
  ["dashboard", "◈", "Tổng quan", "REPORT_READ"],
  ["products", "▦", "Sản phẩm", "PRODUCT_READ"],
  ["customers", "◎", "Khách hàng", "CUSTOMER_READ"],
  ["inventory", "▤", "Tồn kho", "INVENTORY_READ"],
  ["receipts", "⇥", "Nhập kho & phê duyệt", "RECEIPT_READ"],
  ["orders", "▣", "Đặt hàng & bán hàng", "ORDER_READ"],
  ["employees", "♙", "Nhân viên", "EMPLOYEE_READ"],
  ["warehouses", "⌂", "Kho hàng", "WAREHOUSE_READ"],
  ["accounts", "⚿", "Tài khoản & phân quyền", "ACCOUNT_WRITE"],
  ["audit", "◷", "Lịch sử thao tác", "AUDIT_READ"],
];
const fields = {
  products: [
    ["sku", "SKU"],
    ["name", "Tên sản phẩm"],
    ["category", "Danh mục"],
    ["unit", "Đơn vị"],
    ["price", "Giá bán", "number"],
  ],
  customers: [
    ["code", "Mã KH"],
    ["name", "Tên khách hàng"],
    ["phone", "Điện thoại"],
    ["email", "Email", "email"],
  ],
  employees: [
    ["code", "Mã NV"],
    ["name", "Họ tên"],
    ["email", "Email", "email"],
    ["department", "Phòng ban"],
  ],
  warehouses: [
    ["code", "Mã kho"],
    ["name", "Tên kho"],
  ],
  accounts: [
    ["username", "Tài khoản"],
    ["password", "Mật khẩu (12–72 byte)", "password"],
    ["employeeId", "ID nhân viên (tùy chọn)", "number"],
    ["roleIds", "ID vai trò, ngăn cách dấu phẩy"],
  ],
};
const cols = {
  products: ["id", "sku", "name", "category", "unit", "price"],
  customers: ["id", "code", "name", "phone", "email"],
  employees: ["id", "code", "name", "email", "department"],
  warehouses: ["id", "code", "name"],
  inventory: ["productId", "warehouseId", "quantity"],
  receipts: ["id", "reference", "supplier", "status", "approvalLevel"],
  orders: ["id", "reference", "customerId", "total", "status"],
  accounts: ["id", "username", "employeeId", "enabled"],
  audit: ["id", "actorId", "action", "entityType", "entityId", "createdAt"],
};
const labels = {
  id: "ID",
  sku: "SKU",
  name: "Tên",
  category: "Danh mục",
  unit: "Đơn vị",
  price: "Giá",
  code: "Mã",
  phone: "Điện thoại",
  email: "Email",
  department: "Phòng ban",
  productId: "ID sản phẩm",
  warehouseId: "Kho",
  quantity: "Số lượng",
  reference: "Chứng từ",
  supplier: "Nhà cung cấp",
  status: "Trạng thái",
  approvalLevel: "Tầng duyệt",
  customerId: "Khách hàng",
  total: "Tổng tiền",
  username: "Tài khoản",
  employeeId: "Nhân viên",
  enabled: "Hoạt động",
  actorId: "Người thao tác",
  action: "Hành động",
  entityType: "Đối tượng",
  entityId: "ID đối tượng",
  createdAt: "Thời gian",
};
const money = (v) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    v || 0,
  );
function App() {
  const [user, U] = useState(null),
    [view, V] = useState({
      page: "dashboard",
      number: 0,
      after: 0,
      history: [],
      filter: "",
    }),
    [rows, R] = useState([]),
    [stats, S] = useState({}),
    [next, N] = useState(false),
    [busy, B] = useState(false),
    [error, E] = useState(""),
    [refresh, F] = useState(0),
    [modal, M] = useState(null),
    [detail, D] = useState(null),
    [roles, Roles] = useState([]);
  const { page, number, after, history, filter } = view;
  const can = (p) => user?.permissions.some((a) => a.authority === p);
  const reload = () => F((x) => x + 1);
  const go = (p) => {
    R([]);
    N(false);
    V({ page: p, number: 0, after: 0, history: [], filter: "" });
    D(null);
    M(null);
    E("");
  };
  useEffect(() => {
    if (!user) return;
    let live = true;
    B(true);
    E("");
    let path =
      page === "dashboard" ? "/dashboard" : `/${page}?page=${number}&size=25`;
    if (page === "products")
      path = `/products?after=${after}&size=25&sku=${encodeURIComponent(filter)}`;
    if (page === "inventory")
      path += `&warehouseId=${encodeURIComponent(filter || "1")}`;
    if (page === "orders" && filter)
      path += `&customerId=${encodeURIComponent(filter)}`;
    api(path)
      .then((data) => {
        if (!live) return;
        if (page === "dashboard") S(data);
        else {
          R(data.content);
          N(!data.last);
        }
      })
      .catch((e) => {
        if (live) E(e.message);
      })
      .finally(() => {
        if (live) B(false);
      });
    if (page === "accounts")
      api("/roles")
        .then((r) => {
          if (live) Roles(r);
        })
        .catch(() => {});
    return () => {
      live = false;
    };
  }, [user, view, refresh]);
  async function login(e) {
    e.preventDefault();
    B(true);
    E("");
    const f = new FormData(e.target);
    credentials(f.get("username"), f.get("password"));
    try {
      const u = await api("/auth/me");
      U(u);
      go(
        nav.find((p) => u.permissions.some((a) => a.authority === p[3]))?.[0] ||
          "dashboard",
      );
    } catch (e) {
      clear();
      E(e.message);
    } finally {
      B(false);
    }
  }
  async function change(path, method, body) {
    B(true);
    E("");
    try {
      await api(path, method, body);
      M(null);
      D(null);
      reload();
    } catch (e) {
      E(e.message);
    } finally {
      B(false);
    }
  }
  async function inspect(row) {
    try {
      D(await api(`/${page}/${row.id}`));
    } catch (e) {
      E(e.message);
    }
  }
  if (!user)
    return (
      <div className="login">
        <section>
          <div className="brand">▦ Paperflow</div>
          <p className="eyebrow">VẬN HÀNH VĂN PHÒNG PHẨM</p>
          <h1>Chào mừng trở lại</h1>
          <p className="muted">Đăng nhập để quản lý bán hàng và kho của bạn.</p>
          <form onSubmit={login}>
            <label>
              Tài khoản
              <input
                name="username"
                defaultValue="admin"
                autoComplete="username"
                required
              />
            </label>
            <label>
              Mật khẩu
              <input
                name="password"
                type="password"
                autoComplete="current-password"
                required
              />
            </label>
            {error && (
              <p className="error" role="alert">
                {error}
              </p>
            )}
            <button disabled={busy}>Đăng nhập →</button>
          </form>
          <p className="small muted">
            Mật khẩu admin được cấu hình khi khởi động.
          </p>
        </section>
        <section className="art">
          <p className="eyebrow">QUẢN LÝ TẬP TRUNG</p>
          <h2>
            Từ từng cây bút.
            <br />
            Đến hàng nghìn đơn hàng.
          </h2>
          <p>Một không gian cho sản phẩm, kho hàng và đội ngũ.</p>
          <div className="paper">
            PAPER
            <br />
            <b>FLOW.</b>
            <p>01 — STATIONERY OPERATIONS</p>
          </div>
        </section>
      </div>
    );
  const createPermission = {
    products: "PRODUCT_WRITE",
    customers: "CUSTOMER_WRITE",
    employees: "EMPLOYEE_WRITE",
    warehouses: "WAREHOUSE_WRITE",
    accounts: "ACCOUNT_WRITE",
    receipts: "RECEIPT_WRITE",
    orders: "ORDER_WRITE",
  }[page];
  return (
    <div className="shell">
      <aside>
        <div className="brand">▦ Paperflow</div>
        <p className="nav-label">KHÔNG GIAN LÀM VIỆC</p>
        <nav>
          {nav
            .filter((p) => can(p[3]))
            .map(([key, icon, title]) => (
              <button
                key={key}
                className={page === key ? "selected" : ""}
                onClick={() => go(key)}
              >
                <span>{icon}</span>
                {title}
              </button>
            ))}
        </nav>
        <div className="profile">
          <b>{user.username}</b>
          <button
            className="link"
            onClick={async () => {
              try {
                await api("/auth/logout", "POST");
              } catch {}
              clear();
              U(null);
              E("");
            }}
          >
            Đăng xuất
          </button>
        </div>
      </aside>
      <main>
        <header>
          Không gian làm việc / <b>{nav.find((p) => p[0] === page)?.[2]}</b>
          <span>VPP • Quản lý tập trung</span>
        </header>
        <div className="content">
          <div className="title">
            <div>
              <p className="eyebrow">PAPERFLOW / OPERATIONS</p>
              <h1>{nav.find((p) => p[0] === page)?.[2]}</h1>
              <p className="muted">
                {page === "dashboard"
                  ? "Theo dõi hoạt động kinh doanh và kho hàng của bạn."
                  : "Dữ liệu trực tiếp • Tối đa 25 bản ghi mỗi trang"}
              </p>
            </div>
            {createPermission && can(createPermission) && (
              <button onClick={() => M("create")}>＋ Thêm mới</button>
            )}
          </div>
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          {page === "dashboard" ? (
            <>
              <div className="stats">
                {[
                  ["Sản phẩm", stats.products, "▦"],
                  ["Khách hàng", stats.customers, "◎"],
                  ["Đơn đặt hàng", stats.orders, "▣"],
                  ["Doanh thu đã xuất", money(stats.revenue), "↗"],
                ].map(([l, v, i]) => (
                  <article key={l}>
                    <span className="icon">{i}</span>
                    <p>{l}</p>
                    <h2>{busy ? "…" : (v ?? "—")}</h2>
                    <small className="muted">Lưu đệm tối đa 60 giây</small>
                  </article>
                ))}
              </div>
              <div className="grid">
                <article className="panel">
                  <p className="eyebrow">QUY TRÌNH KHO</p>
                  <h2>Kiểm soát từng bước nhập hàng</h2>
                  <div className="steps">
                    <span>
                      01
                      <br />
                      <b>Lập phiếu</b>
                    </span>
                    →
                    <span>
                      02
                      <br />
                      <b>Duyệt cấp 1</b>
                    </span>
                    →
                    <span>
                      03
                      <br />
                      <b>Duyệt cấp 2</b>
                    </span>
                    →
                    <span>
                      04
                      <br />
                      <b>Ghi tăng tồn</b>
                    </span>
                  </div>
                  <p className="muted">
                    Người lập và hai người duyệt phải khác nhau. Tồn kho chỉ
                    tăng sau khi hoàn tất phê duyệt.
                  </p>
                </article>
                <article className="panel accent">
                  <p className="eyebrow">CẦN XỬ LÝ</p>
                  <h2 className="large">{stats.pendingReceipts ?? "—"}</h2>
                  <p>Phiếu nhập chờ phê duyệt</p>
                  {can("RECEIPT_READ") && (
                    <button onClick={() => go("receipts")}>
                      Xem phiếu nhập →
                    </button>
                  )}
                </article>
              </div>
              <article className="panel">
                <h3>Bắt đầu công việc</h3>
                <div className="actions">
                  {nav
                    .filter(
                      (p) =>
                        ["products", "inventory", "orders"].includes(p[0]) &&
                        can(p[3]),
                    )
                    .map((p) => (
                      <button
                        className="secondary"
                        key={p[0]}
                        onClick={() => go(p[0])}
                      >
                        {p[1]} {p[2]} →
                      </button>
                    ))}
                </div>
              </article>
            </>
          ) : (
            <article className="table-panel">
              {["products", "orders", "inventory"].includes(page) && (
                <form
                  className="toolbar"
                  key={page}
                  onSubmit={(e) => {
                    e.preventDefault();
                    V({
                      ...view,
                      filter: new FormData(e.target).get("filter"),
                      number: 0,
                      after: 0,
                      history: [],
                    });
                  }}
                >
                  <input
                    name="filter"
                    placeholder={
                      page === "products"
                        ? "Tiền tố SKU…"
                        : page === "orders"
                          ? "ID khách hàng — lịch sử mua hàng"
                          : "ID kho (mặc định 1)"
                    }
                    aria-label="Bộ lọc"
                  />
                  <button className="secondary">Lọc dữ liệu</button>
                </form>
              )}
              {page === "accounts" && (
                <p className="roles">
                  Vai trò: {roles.map((r) => `${r.id}: ${r.name}`).join(" · ")}
                </p>
              )}
              <div className="scroll">
                <table>
                  <thead>
                    <tr>
                      {cols[page].map((c) => (
                        <th key={c}>{labels[c]}</th>
                      ))}
                      {(["orders", "receipts", "accounts"].includes(page) ||
                        (fields[page] &&
                          page !== "accounts" &&
                          can(createPermission))) && <th>Thao tác</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {!busy &&
                      rows.map((row) => (
                        <tr key={row.id}>
                          {cols[page].map((c) => (
                            <td key={c}>
                              {c === "status" ? (
                                <span className={"badge " + row[c]}>
                                  {row[c]}
                                </span>
                              ) : ["price", "total"].includes(c) ? (
                                money(row[c])
                              ) : typeof row[c] === "boolean" ? (
                                row[c] ? (
                                  "Có"
                                ) : (
                                  "Không"
                                )
                              ) : (
                                String(row[c] ?? "—")
                              )}
                            </td>
                          ))}
                          {["orders", "receipts"].includes(page) && (
                            <td>
                              <button
                                className="link"
                                onClick={() => inspect(row)}
                              >
                                Chi tiết →
                              </button>
                            </td>
                          )}
                          {fields[page] &&
                            page !== "accounts" &&
                            can(createPermission) && (
                              <td>
                                <button className="link" onClick={() => M(row)}>
                                  Sửa
                                </button>
                              </td>
                            )}
                          {page === "accounts" && (
                            <td>
                              <button
                                className="link"
                                onClick={() =>
                                  change(
                                    `/accounts/${row.id}/enabled?enabled=${!row.enabled}`,
                                    "PUT",
                                  )
                                }
                              >
                                {row.enabled ? "Khóa" : "Mở khóa"}
                              </button>
                              <button
                                className="link"
                                onClick={() => D({ account: row })}
                              >
                                Vai trò
                              </button>
                            </td>
                          )}
                        </tr>
                      ))}
                  </tbody>
                </table>
                {(busy || !rows.length) && (
                  <div className="empty">
                    {busy
                      ? "Đang tải dữ liệu…"
                      : "Chưa có dữ liệu. Thêm mới hoặc nạp dữ liệu mẫu."}
                  </div>
                )}
              </div>
              <footer>
                <small className="muted">Trang {number + 1}</small>
                <div>
                  <button
                    className="secondary"
                    disabled={!number || busy}
                    onClick={() =>
                      V({
                        ...view,
                        number: number - 1,
                        after: history[number - 1] || 0,
                      })
                    }
                  >
                    ← Trước
                  </button>
                  <button
                    className="secondary"
                    disabled={!next || busy}
                    onClick={() =>
                      V({
                        ...view,
                        number: number + 1,
                        after: rows.at(-1).id,
                        history: [...history.slice(0, number), after],
                      })
                    }
                  >
                    Sau →
                  </button>
                </div>
              </footer>
            </article>
          )}
        </div>
      </main>
      {modal && (
        <div className="overlay">
          <section className="dialog">
            <div className="title">
              <h2>
                {modal?.id ? "Sửa" : "Thêm"}{" "}
                {page === "orders"
                  ? "đơn hàng"
                  : page === "receipts"
                    ? "phiếu nhập"
                    : "dữ liệu"}
              </h2>
              <button className="secondary" onClick={() => M(null)}>
                Đóng
              </button>
            </div>
            {error && <p className="error">{error}</p>}
            {["orders", "receipts"].includes(page) ? (
              <DocumentForm
                page={page}
                busy={busy}
                save={(body) => change("/" + page, "POST", body)}
              />
            ) : (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const body = Object.fromEntries(new FormData(e.target));
                  for (const [k, , t] of fields[page])
                    if (t === "number")
                      body[k] = body[k] === "" ? null : Number(body[k]);
                  if (page === "accounts")
                    body.roleIds = body.roleIds.split(",").map(Number);
                  if (["products", "employees"].includes(page))
                    body.active = body.active === "on";
                  change(
                    "/" + page + (modal?.id ? "/" + modal.id : ""),
                    modal?.id ? "PUT" : "POST",
                    body,
                  );
                }}
              >
                {fields[page].map(([k, l, t]) => (
                  <label key={k}>
                    {l}
                    <input
                      name={k}
                      defaultValue={
                        typeof modal === "object" ? (modal[k] ?? "") : undefined
                      }
                      type={t || "text"}
                      min={t === "number" ? 0 : undefined}
                      step={k === "price" ? ".01" : undefined}
                      required={
                        ![
                          "phone",
                          "email",
                          "department",
                          "employeeId",
                        ].includes(k)
                      }
                    />
                  </label>
                ))}
                {["products", "employees"].includes(page) && (
                  <label className="checkbox">
                    <input
                      type="checkbox"
                      name="active"
                      defaultChecked={
                        typeof modal === "object" ? modal.active : true
                      }
                    />{" "}
                    Đang hoạt động
                  </label>
                )}
                {page === "accounts" && (
                  <p className="small muted">
                    {roles.map((r) => `${r.id}: ${r.name}`).join(" · ")}
                  </p>
                )}
                <button disabled={busy}>Lưu dữ liệu</button>
              </form>
            )}
          </section>
        </div>
      )}
      {detail && (
        <div className="overlay">
          <section className="dialog wide">
            <div className="title">
              <h2>Chi tiết {detail.account ? "phân quyền" : "chứng từ"}</h2>
              <button className="secondary" onClick={() => D(null)}>
                Đóng
              </button>
            </div>
            {error && <p className="error">{error}</p>}
            {detail.account ? (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  change(
                    `/accounts/${detail.account.id}/roles`,
                    "PUT",
                    new FormData(e.target)
                      .get("roleIds")
                      .split(",")
                      .map(Number),
                  );
                }}
              >
                <p>{detail.account.username}</p>
                <p className="small">
                  {roles.map((r) => `${r.id}: ${r.name}`).join(" · ")}
                </p>
                <label>
                  ID vai trò, ngăn cách dấu phẩy
                  <input name="roleIds" required placeholder="Ví dụ: 2,3" />
                </label>
                <button disabled={busy}>Lưu vai trò</button>
              </form>
            ) : (
              <>
                <p>
                  {(detail.receipt || detail.order).reference} •{" "}
                  <b>{(detail.receipt || detail.order).status}</b>
                </p>
                <table>
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th>Số lượng</th>
                      <th>Đơn giá</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detail.lines.map((l) => (
                      <tr key={l.id}>
                        <td>#{l.productId}</td>
                        <td>{l.quantity}</td>
                        <td>{money(l.unitCost ?? l.unitPrice)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {detail.approvals?.map((a) => (
                  <p key={a.id}>
                    Cấp {a.approvalLevel} • {a.decision} • người duyệt #
                    {a.actorId} • {a.note}
                  </p>
                ))}
                {detail.receipt?.status === "PENDING" &&
                  can(
                    detail.receipt.approvalLevel === 0
                      ? "RECEIPT_APPROVE_1"
                      : "RECEIPT_APPROVE_2",
                  ) && (
                    <form
                      onSubmit={(e) => {
                        e.preventDefault();
                        change(
                          `/receipts/${detail.receipt.id}/decision`,
                          "POST",
                          Object.fromEntries(new FormData(e.target)),
                        );
                      }}
                    >
                      <label>
                        Quyết định
                        <select name="decision">
                          <option value="APPROVE">Phê duyệt</option>
                          <option value="REJECT">Từ chối</option>
                        </select>
                      </label>
                      <label>
                        Ghi chú
                        <input name="note" maxLength="1000" />
                      </label>
                      <button disabled={busy}>
                        Gửi quyết định cấp {detail.receipt.approvalLevel + 1}
                      </button>
                    </form>
                  )}
                {detail.order?.status === "PLACED" && can("ORDER_WRITE") && (
                  <button
                    disabled={busy}
                    onClick={() =>
                      change(`/orders/${detail.order.id}/fulfill`, "POST")
                    }
                  >
                    Xuất hàng & trừ tồn
                  </button>
                )}
              </>
            )}
          </section>
        </div>
      )}
    </div>
  );
}
function DocumentForm({ page, busy, save }) {
  const [lines, L] = useState([{ productId: "", quantity: 1, unitPrice: 0 }]);
  const edit = (i, k, v) =>
    L((old) => old.map((l, n) => (n === i ? { ...l, [k]: v } : l)));
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const f = new FormData(e.target);
        save({
          warehouseId: Number(f.get("warehouseId")),
          customerId: page === "orders" ? Number(f.get("customerId")) : null,
          supplier: f.get("supplier"),
          lines: lines.map((l) => ({
            productId: Number(l.productId),
            quantity: Number(l.quantity),
            unitPrice: Number(l.unitPrice),
          })),
        });
      }}
    >
      <label>
        ID kho
        <input
          name="warehouseId"
          type="number"
          min="1"
          defaultValue="1"
          required
        />
      </label>
      {page === "orders" ? (
        <label>
          ID khách hàng
          <input name="customerId" type="number" min="1" required />
        </label>
      ) : (
        <label>
          Nhà cung cấp
          <input name="supplier" maxLength="255" required />
        </label>
      )}
      <p className="small muted">
        {page === "orders"
          ? "Giá bán lấy từ danh mục. Đơn đặt chưa giữ tồn."
          : "Cần hai cấp duyệt để tăng tồn kho."}
      </p>
      {lines.map((l, i) => (
        <div className="line" key={i}>
          <label>
            ID sản phẩm
            <input
              type="number"
              min="1"
              value={l.productId}
              required
              onChange={(e) => edit(i, "productId", e.target.value)}
            />
          </label>
          <label>
            Số lượng
            <input
              type="number"
              min="1"
              max="1000000000"
              value={l.quantity}
              required
              onChange={(e) => edit(i, "quantity", e.target.value)}
            />
          </label>
          {page === "receipts" && (
            <label>
              Giá nhập
              <input
                type="number"
                min="0"
                step=".01"
                value={l.unitPrice}
                required
                onChange={(e) => edit(i, "unitPrice", e.target.value)}
              />
            </label>
          )}
          <button
            type="button"
            className="secondary"
            disabled={lines.length === 1}
            onClick={() => L((old) => old.filter((_, n) => n !== i))}
          >
            ×
          </button>
        </div>
      ))}
      <div className="actions">
        <button
          type="button"
          className="secondary"
          disabled={lines.length >= 500}
          onClick={() =>
            L((old) => [...old, { productId: "", quantity: 1, unitPrice: 0 }])
          }
        >
          ＋ Thêm dòng
        </button>
        <button disabled={busy}>Tạo chứng từ</button>
      </div>
    </form>
  );
}
createRoot(document.getElementById("root")).render(<App />);
