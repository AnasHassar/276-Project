document.getElementById('logoutLink').addEventListener('click', (e) => {
  e.preventDefault();
  document.getElementById('logoutForm').submit();
});

function escapeHtml(value) {
  const div = document.createElement('div');
  div.textContent = value == null ? '' : String(value);
  return div.innerHTML;
}

const clubForm      = document.getElementById('clubForm');
const clubIdInput   = document.getElementById('clubId');
const nameInput     = document.getElementById('clubName');
const categoryInput = document.getElementById('category');
const descInput     = document.getElementById('clubDescription');
const emailInput    = document.getElementById('contactEmail');
const logoInput     = document.getElementById('logoUrl');
const formTitle     = document.getElementById('formTitle');
const submitBtn     = document.getElementById('submitBtn');
const cancelBtn     = document.getElementById('cancelEditBtn');
const msg           = document.getElementById('formMsg');

function resetForm() {
  clubForm.reset();
  clubIdInput.value = '';
  formTitle.textContent = 'New Club';
  submitBtn.textContent = 'Add Club';
  cancelBtn.style.display = 'none';
}

function startEdit(club) {
  clubIdInput.value = club.id;
  nameInput.value = club.name || '';
  categoryInput.value = club.category || '';
  descInput.value = club.description || '';
  emailInput.value = club.contactEmail || '';
  logoInput.value = club.logoUrl || '';
  formTitle.textContent = 'Edit Club';
  submitBtn.textContent = 'Save Changes';
  cancelBtn.style.display = 'block';
  msg.textContent = '';
  msg.className = 'form-message';
  clubForm.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

cancelBtn.addEventListener('click', () => {
  resetForm();
  msg.textContent = '';
  msg.className = 'form-message';
});

function renderClubs(clubs) {
  const container = document.getElementById('clubsContent');
  if (!clubs.length) {
    container.innerHTML = '<p style="color:var(--text-muted);margin:0">No clubs yet. Add the first one below.</p>';
    return;
  }
  container.innerHTML = `
    <table class="admin-users-table">
      <thead>
        <tr><th>Name</th><th>Category</th><th>Contact</th><th></th></tr>
      </thead>
      <tbody>
        ${clubs.map(c => `
          <tr>
            <td>${escapeHtml(c.name)}</td>
            <td>${escapeHtml(c.category)}</td>
            <td>${escapeHtml(c.contactEmail)}</td>
            <td class="club-actions">
              <button type="button" class="btn-link" data-action="edit" data-id="${c.id}">Edit</button>
              <button type="button" class="btn-link btn-link-danger" data-action="delete" data-id="${c.id}">Delete</button>
            </td>
          </tr>`).join('')}
      </tbody>
    </table>`;

  container.querySelectorAll('[data-action="edit"]').forEach(btn => {
    btn.addEventListener('click', () => {
      const club = clubs.find(c => String(c.id) === btn.dataset.id);
      if (club) startEdit(club);
    });
  });

  container.querySelectorAll('[data-action="delete"]').forEach(btn => {
    btn.addEventListener('click', () => deleteClub(btn.dataset.id, btn));
  });
}

function loadClubs() {
  return fetch('/api/admin/clubs', { headers: { Accept: 'application/json' } })
    .then(res => {
      if (!res.ok) throw new Error('Failed to load clubs');
      return res.json();
    })
    .then(renderClubs)
    .catch(() => {
      document.getElementById('clubsContent').innerHTML =
        '<p style="color:var(--red-bright);margin:0">Could not load clubs.</p>';
    });
}

function deleteClub(id, btn) {
  if (!confirm('Delete this club? This cannot be undone.')) return;
  btn.disabled = true;
  fetch('/api/admin/clubs/' + id, {
    method: 'DELETE',
    headers: { 'X-XSRF-TOKEN': getCsrfCookie() || '' }
  })
    .then(res => {
      if (!res.ok) throw new Error('Failed to delete club');
      if (clubIdInput.value === String(id)) resetForm();
      return loadClubs();
    })
    .catch(() => {
      btn.disabled = false;
      alert('Could not delete club. Please try again.');
    });
}

clubForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  msg.textContent = '';
  msg.className = 'form-message';

  const body = {
    name:         nameInput.value.trim(),
    category:     categoryInput.value.trim(),
    description:  descInput.value.trim(),
    contactEmail: emailInput.value.trim(),
    logoUrl:      logoInput.value.trim()
  };

  const editingId = clubIdInput.value;
  const url = editingId ? '/api/admin/clubs/' + editingId : '/api/admin/clubs';
  const method = editingId ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfCookie() || '' },
      body: JSON.stringify(body)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || 'Server error');
    }
    msg.textContent = editingId ? '✓ Club updated successfully!' : '✓ Club added successfully!';
    msg.className = 'form-message success';
    resetForm();
    await loadClubs();
  } catch (err) {
    msg.textContent = '✗ ' + err.message;
    msg.className = 'form-message error';
  }
});

loadClubs();