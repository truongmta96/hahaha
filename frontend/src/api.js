let auth = "",
  csrf;
export function credentials(username, password) {
  auth =
    "Basic " +
    btoa(
      String.fromCharCode(
        ...new TextEncoder().encode(username + ":" + password),
      ),
    );
}
export function clear() {
  auth = "";
  csrf = null;
}
export async function api(path, method = "GET", body) {
  if (method !== "GET" && !csrf) csrf = await api("/auth/csrf");
  const r = await fetch("/api" + path, {
    method,
    headers: {
      ...(auth ? { Authorization: auth } : {}),
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(method !== "GET" ? { [csrf.headerName]: csrf.token } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await r.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  if (!r.ok)
    throw new Error(
      data?.message ||
        (r.status === 401
          ? "Sai tài khoản hoặc mật khẩu"
          : `Yêu cầu thất bại (${r.status})`),
    );
  return data;
}
