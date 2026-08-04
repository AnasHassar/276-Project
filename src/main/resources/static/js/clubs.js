let allClubs = [];
let currentSearch = '';
let currentCategory = 'All';

document.addEventListener('DOMContentLoaded', () => {
  setupEventListeners();
  loadClubs();
});

function loadClubs() {
  fetch('/api/clubs')
    .then(response => {
      if (!response.ok) throw new Error('Failed to load clubs (' + response.status + ')');
      return response.json();
    })
    .then(data => {
      allClubs = Array.isArray(data) ? data : [];
      renderClubs();
    })
    .catch(error => {
      console.error('Error loading clubs:', error);
      const container = document.getElementById('clubsList');
      if (container) {
        container.innerHTML = '<div class="card"><p class="clubs-empty">Could not load clubs right now. Please try again later.</p></div>';
      }
    });
}

function renderClubs() {
  const container = document.getElementById('clubsList');
  if (!container) return;

  const filtered = allClubs.filter(club => {
    const name = (club.name || '').toLowerCase();
    const description = (club.description || '').toLowerCase();
    const search = currentSearch.toLowerCase();

    const matchesSearch = search === '' || name.includes(search) || description.includes(search);
    const matchesCategory = currentCategory === 'All' || club.category === currentCategory;

    return matchesSearch && matchesCategory;
  });

  if (filtered.length === 0) {
    container.innerHTML = '<div class="card"><p class="clubs-empty">No clubs found. Try a different search or category.</p></div>';
    return;
  }

  container.innerHTML = filtered.map(club => `
    <div class="club-card">
      ${club.logoUrl
        ? `<img class="club-image club-image--logo" src="${escapeHtml(club.logoUrl)}" alt="${escapeHtml(club.name || '')} logo" loading="lazy" onerror="this.outerHTML='<div class=&quot;club-image&quot;>${escapeHtml((club.name || '?').charAt(0).toUpperCase())}</div>'">`
        : `<div class="club-image">${escapeHtml((club.name || '?').charAt(0).toUpperCase())}</div>`
      }
      <div class="club-content">
        <div class="club-name">${escapeHtml(club.name || 'Unnamed club')}</div>
        <span class="club-category">${escapeHtml(club.category || 'General')}</span>
        <div class="club-description">${escapeHtml(club.description || '')}</div>
        <div class="club-meta">
          <span>${Number(club.memberCount) || 0} members</span>
          ${club.website ? `<a href="${escapeHtml(club.website)}" target="_blank" rel="noopener" class="club-link" onclick="event.stopPropagation()">View on SFSS →</a>` : ''}
        </div>
      </div>
    </div>
  `).join('');
}

function setupEventListeners() {
  const searchInput = document.getElementById('clubSearch');
  if (searchInput) {
    searchInput.addEventListener('input', e => {
      currentSearch = e.target.value.trim();
      renderClubs();
    });
  }

  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', e => {
      document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      e.currentTarget.classList.add('active');
      currentCategory = e.currentTarget.dataset.category || 'All';
      renderClubs();
    });
  });
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
