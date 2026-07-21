document.addEventListener('DOMContentLoaded', () => {
  // weather stuff is handled in weather.js

  fetch('/api/auth/me', { headers: { Accept: 'application/json' } })
    .then(res => (res.ok ? res.json() : null))
    .then(user => {
      if (!user) return;
      const nameEl = document.getElementById('userName');
      if (nameEl && user.fullName) {
        const firstName = user.fullName.split(' ')[0];
        nameEl.textContent = ' ' + firstName + '!';
      }
    })
    .catch(() => {});
});
