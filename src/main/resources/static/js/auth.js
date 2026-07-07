function getCsrfCookie() {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

document.addEventListener('DOMContentLoaded', () => {
  const csrfInput = document.getElementById('csrfToken');
  if (csrfInput) {
    csrfInput.value = getCsrfCookie() || '';
  }

  const params = new URLSearchParams(window.location.search);
  const messageEl = document.getElementById('authMessage');
  if (!messageEl) return;

  if (params.has('error')) {
    const raw = params.get('error');
    messageEl.textContent = raw && raw.length ? raw : 'Invalid username or password.';
    messageEl.className = 'auth-message auth-message-error';
  } else if (params.has('registered')) {
    messageEl.textContent = 'Account created! Please log in.';
    messageEl.className = 'auth-message auth-message-success';
  } else if (params.has('logout')) {
    messageEl.textContent = 'You have been logged out.';
    messageEl.className = 'auth-message auth-message-success';
  }
  
});
