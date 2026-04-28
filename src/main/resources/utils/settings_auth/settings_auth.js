async function settingsAuthStatus() {
  try {
    const r = await fetch('/settings_auth/status', {
      method: 'GET',
      credentials: 'same-origin',
      cache: 'no-store',
    });
    if (!r.ok) return false;
    const data = await r.json();
    return !!data.authorized;
  } catch (_) {
    return false;
  }
}

async function settingsAuthLogin(password) {
  const r = await fetch('/settings_auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin',
    cache: 'no-store',
    body: JSON.stringify({ password }),
  });
  if (r.ok) return { ok: true };
  let msg = 'Неверный пароль';
  try {
    const data = await r.json();
    if (data && data.message) msg = data.message;
  } catch (_) {}
  return { ok: false, message: msg };
}

export async function settingsAuthLogout() {
  try {
    await fetch('/settings_auth/logout', {
      method: 'POST',
      credentials: 'same-origin',
      cache: 'no-store',
      // allow request to be sent during navigation (best-effort)
      keepalive: true,
    });
  } catch (_) {
    // best-effort: ignore
  }
}

function el(id) {
  return document.getElementById(id);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitUntilAuthorized({ attempts = 8, baseDelayMs = 60 } = {}) {
  for (let i = 0; i < attempts; i++) {
    if (await settingsAuthStatus()) return true;
    // small backoff: 60, 90, 120, ...
    await sleep(baseDelayMs + i * 30);
  }
  return false;
}

export async function ensureSettingsAccess(onSuccess) {
  if (await settingsAuthStatus()) {
    onSuccess();
    return;
  }

  // Redirect to dedicated login page (no modal)
  window.location.href = '/settings/login';
}

