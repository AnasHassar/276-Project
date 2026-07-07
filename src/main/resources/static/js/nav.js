// Reflects the current login state in the shared site nav (#authLinks).
// Requires js/auth.js to be loaded first (for getCsrfCookie()).
document.addEventListener('DOMContentLoaded', () => {
  const authLinks = document.getElementById('authLinks');
  if (!authLinks) return;

  fetch('/api/auth/me', { headers: { Accept: 'application/json' } })
    .then(res => (res.ok ? res.json() : null))
    .then(user => {
      if (!user) return; // not logged in — leave the default Login/Register links

      const label = user.isAdmin ? 'Executive' : (user.fullName || user.username);
      authLinks.innerHTML = `
        <span class="nav-user">Hi, ${label}</span>
        <a href="#" id="logoutLink">Logout</a>
        <form id="logoutForm" method="post" action="/logout" style="display:none">
          <input type="hidden" name="_csrf" id="logoutCsrf">
        </form>`;

      const csrfInput = document.getElementById('logoutCsrf');
      if (csrfInput) csrfInput.value = getCsrfCookie() || '';

      document.getElementById('logoutLink').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('logoutForm').submit();
      });
    })
    .catch(() => {});
});
