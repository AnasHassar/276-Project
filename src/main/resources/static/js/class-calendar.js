let allClassEvents = [];
let currentView = 'all';

const classList = document.getElementById('classList');
const searchInput = document.getElementById('searchInput');
const viewFilterBtn = document.getElementById('viewFilterBtn');
const viewFilterPanel = document.getElementById('viewFilterPanel');
const viewFilterLabel = document.getElementById('viewFilterLabel');
const classForm = document.getElementById('classForm');
const formTitle = document.getElementById('formTitle');
const submitBtn = document.getElementById('submitBtn');
const cancelEditBtn = document.getElementById('cancelEditBtn');
const formMsg = document.getElementById('formMsg');
const recurringInput = document.getElementById('recurring');
const recurrenceGroup = document.getElementById('recurrenceGroup');

function escapeHtml(value) {
  const div = document.createElement('div');
  div.textContent = value == null ? '' : String(value);
  return div.innerHTML;
}

function formatTime(time) {
  if (!time) return '';
  const parts = time.split(':');
  const date = new Date();
  date.setHours(Number(parts[0] || 0), Number(parts[1] || 0), 0, 0);
  return date.toLocaleTimeString('en-CA', { hour: 'numeric', minute: '2-digit' });
}

function formatDay(day) {
  if (!day) return 'Unspecified';
  return day.charAt(0) + day.slice(1).toLowerCase();
}

function setMessage(text, type) {
  formMsg.textContent = text || '';
  formMsg.className = 'form-message' + (type ? ' ' + type : '');
}

function resetForm() {
  classForm.reset();
  document.getElementById('classId').value = '';
  formTitle.textContent = 'Add Class';
  submitBtn.textContent = 'Save Class';
  cancelEditBtn.style.display = 'none';
  recurringInput.checked = false;
  recurrenceGroup.classList.remove('visible');
}

function populateForm(classEvent) {
  document.getElementById('classId').value = classEvent.id;
  document.getElementById('title').value = classEvent.title || '';
  document.getElementById('dayOfWeek').value = classEvent.dayOfWeek || '';
  document.getElementById('startTime').value = classEvent.startTime || '';
  document.getElementById('endTime').value = classEvent.endTime || '';
  document.getElementById('location').value = classEvent.location || '';
  document.getElementById('notes').value = classEvent.notes || '';
  recurringInput.checked = !!classEvent.recurring;
  document.getElementById('recurrenceEndDate').value = classEvent.recurrenceEndDate || '';
  recurrenceGroup.classList.toggle('visible', recurringInput.checked);
  formTitle.textContent = 'Edit Class';
  submitBtn.textContent = 'Update Class';
  cancelEditBtn.style.display = 'block';
}

function buildCard(classEvent) {
  const recurringLabel = classEvent.recurring ? 'Weekly' : 'One-time';
  const recurringClass = classEvent.recurring ? 'recurring' : 'one-time';
  const locationHtml = classEvent.location ? `<div class="class-card__meta">${escapeHtml(classEvent.location)}</div>` : '';
  const notesHtml = classEvent.notes ? `<p class="class-card__notes">${escapeHtml(classEvent.notes)}</p>` : '';
  const endDateHtml = classEvent.recurring && classEvent.recurrenceEndDate
    ? `<div class="class-card__meta">Repeats until ${escapeHtml(classEvent.recurrenceEndDate)}</div>`
    : '';

  return `
    <article class="class-card" data-id="${classEvent.id}">
      <div class="class-card__header">
        <div>
          <div class="class-card__day">${escapeHtml(formatDay(classEvent.dayOfWeek))}</div>
          <h3 class="class-card__title">${escapeHtml(classEvent.title)}</h3>
        </div>
        <span class="class-pill ${recurringClass}">${recurringLabel}</span>
      </div>
      <div class="class-card__meta">${escapeHtml(formatTime(classEvent.startTime))} - ${escapeHtml(formatTime(classEvent.endTime))}</div>
      ${locationHtml}
      ${endDateHtml}
      ${notesHtml}
      <div class="class-card__actions">
        <button type="button" class="btn-link" data-action="edit">Edit</button>
        <button type="button" class="btn-link btn-link-danger" data-action="delete">Delete</button>
      </div>
    </article>`;
}

function applyFilters() {
  const q = searchInput.value.trim().toLowerCase();
  const filtered = allClassEvents.filter(item => {
    if (currentView === 'recurring' && !item.recurring) return false;
    if (currentView === 'one-time' && item.recurring) return false;
    if (!q) return true;
    const text = [item.title, item.location, item.notes, item.dayOfWeek, item.recurrenceRule]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return text.includes(q);
  });

  document.getElementById('classCount').textContent = `${filtered.length} classes`;
  document.getElementById('totalCount').textContent = String(allClassEvents.length);
  document.getElementById('weeklyCount').textContent = String(allClassEvents.filter(item => item.recurring).length);
  document.getElementById('oneTimeCount').textContent = String(allClassEvents.filter(item => !item.recurring).length);

  if (!filtered.length) {
    classList.innerHTML = '<div class="card"><p class="class-empty">No classes found. Add your first one on the right.</p></div>';
    return;
  }

  classList.innerHTML = filtered.map(buildCard).join('');
}

function loadClasses() {
  fetch('/api/class-events', { headers: { Accept: 'application/json' } })
    .then(res => {
      if (res.status === 401 || res.status === 302) {
        window.location.href = '/login.html';
        return null;
      }
      if (!res.ok) {
        throw new Error('Failed to load classes');
      }
      return res.json();
    })
    .then(data => {
      if (!data) return;
      allClassEvents = data;
      applyFilters();
    })
    .catch(() => {
      classList.innerHTML = '<div class="card"><p class="class-empty">Could not load classes right now.</p></div>';
    });
}

function saveClass(event) {
  event.preventDefault();
  const classId = document.getElementById('classId').value;
  const recurring = recurringInput.checked;

  const payload = {
    title: document.getElementById('title').value.trim(),
    dayOfWeek: document.getElementById('dayOfWeek').value,
    startTime: document.getElementById('startTime').value,
    endTime: document.getElementById('endTime').value,
    location: document.getElementById('location').value.trim(),
    notes: document.getElementById('notes').value.trim(),
    recurring,
    recurrenceRule: recurring ? 'WEEKLY' : null,
    recurrenceEndDate: recurring ? document.getElementById('recurrenceEndDate').value || null : null
  };

  const invalid = !payload.title || !payload.dayOfWeek || !payload.startTime || !payload.endTime;
  if (invalid) {
    setMessage('Please fill in the required class details.', 'error');
    return;
  }

  setMessage('', '');

  const method = classId ? 'PUT' : 'POST';
  const url = classId ? `/api/class-events/${encodeURIComponent(classId)}` : '/api/class-events';

  fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfCookie() || ''
    },
    body: JSON.stringify(payload)
  })
    .then(async res => {
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.error || 'Could not save class');
      }
      return res.json();
    })
    .then(() => {
      setMessage(classId ? 'Class updated.' : 'Class added.', 'success');
      resetForm();
      loadClasses();
    })
    .catch(err => {
      setMessage(err.message, 'error');
    });
}

classList.addEventListener('click', (event) => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const card = button.closest('.class-card');
  if (!card) return;
  const classId = card.getAttribute('data-id');
  const classEvent = allClassEvents.find(item => String(item.id) === String(classId));
  if (!classEvent) return;

  if (button.dataset.action === 'edit') {
    populateForm(classEvent);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  if (button.dataset.action === 'delete') {
    if (!window.confirm(`Delete ${classEvent.title}? This removes the saved class block.`)) return;
    fetch(`/api/class-events/${encodeURIComponent(classId)}`, {
      method: 'DELETE',
      headers: { 'X-XSRF-TOKEN': getCsrfCookie() || '' }
    })
      .then(res => {
        if (!res.ok) throw new Error('Could not delete class');
        loadClasses();
      })
      .catch(err => setMessage(err.message, 'error'));
  }
});

searchInput.addEventListener('input', applyFilters);

viewFilterBtn.addEventListener('click', (event) => {
  event.stopPropagation();
  viewFilterPanel.classList.toggle('open');
});

document.querySelectorAll('.dropdown-panel .sort-option').forEach(option => {
  option.addEventListener('click', () => {
    currentView = option.dataset.view;
    viewFilterLabel.textContent = option.textContent;
    document.querySelectorAll('.dropdown-panel .sort-option').forEach(item => item.classList.remove('active'));
    option.classList.add('active');
    viewFilterPanel.classList.remove('open');
    applyFilters();
  });
});

document.addEventListener('click', () => {
  viewFilterPanel.classList.remove('open');
});

classForm.addEventListener('submit', saveClass);
cancelEditBtn.addEventListener('click', () => {
  resetForm();
  setMessage('', '');
});
recurringInput.addEventListener('change', () => {
  recurrenceGroup.classList.toggle('visible', recurringInput.checked);
});

loadClasses();