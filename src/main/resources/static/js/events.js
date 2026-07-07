const SOURCE_LABELS = {
  eventbrite: 'Eventbrite',
  sfu_events: 'SFU Events',
  club: 'Club Event'
};

let allEvents = [];
let currentSort = 'soonest';
let currentSource = '';
let searchTimer = null;

const list = document.getElementById('eventsList');
const searchInput = document.getElementById('searchInput');
const filterBtn = document.getElementById('filterBtn');
const filterPanel = document.getElementById('filterPanel');
const sortBtn = document.getElementById('sortBtn');
const sortPanel = document.getElementById('sortPanel');
const sortLabel = document.getElementById('sortLabel');

function formatDate(iso) {
  if (!iso) return 'Date TBA';
  const d = new Date(iso);
  return d.toLocaleDateString('en-CA', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleTimeString('en-CA', { hour: 'numeric', minute: '2-digit' });
}

function truncate(text, max) {
  if (!text) return '';
  return text.length > max ? text.substring(0, max) + '…' : text;
}

function buildCard(ev) {
  const tags = ev.tags ? ev.tags.split(',').filter(t => t.trim()) : [];
  const tagHtml = tags.slice(0, 4).map(t =>
    `<span class="event-card__tag">#${t.trim()}</span>`
  ).join('');

  const sourceLabel = SOURCE_LABELS[ev.source] || ev.source;
  const dateStr = formatDate(ev.startDate);
  const timeStr = formatTime(ev.startDate);

  const heroHtml = ev.imageUrl
    ? `<img class="event-card__hero" src="${ev.imageUrl}" alt="${ev.title}" loading="lazy" onerror="this.style.display='none'">`
    : `<div class="event-card__hero-placeholder">
         <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="1.5">
           <rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/>
         </svg>
       </div>`;

  const attendeeHtml = ev.attendeeCount
    ? `<div class="event-card__meta-row">
         <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="8" r="3"/><path d="M3 20c0-3 2.5-5 6-5s6 2 6 5"/><circle cx="17" cy="9" r="2.5"/><path d="M14.5 15.2c2.7.3 4.5 2 4.5 4.8"/></svg>
         ${ev.attendeeCount} attending
       </div>`
    : '';

  return `
    <article class="event-card" data-id="${ev.id}">
      ${heroHtml}
      <div class="event-card__body">
        <span class="event-card__source-badge ${ev.source}">${sourceLabel}</span>
        <h2 class="event-card__title">${ev.title}</h2>
        <div class="event-card__meta">
          <div class="event-card__meta-row">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/></svg>
            ${dateStr}${timeStr ? ' · ' + timeStr : ''}
          </div>
          ${ev.location ? `<div class="event-card__meta-row">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a7 7 0 0 1 7 7c0 5-7 13-7 13S5 14 5 9a7 7 0 0 1 7-7z"/><circle cx="12" cy="9" r="2.5"/></svg>
            ${ev.location}
          </div>` : ''}
          ${attendeeHtml}
        </div>
        ${ev.description ? `<p class="event-card__desc">${truncate(ev.description.replace(/<[^>]+>/g, ''), 160)}</p>` : ''}
        ${tagHtml ? `<div class="event-card__tags">${tagHtml}</div>` : ''}
        <button class="event-card__learn-more" onclick="openDetail(${ev.id})">Learn More</button>
      </div>
    </article>`;
}

function renderEvents(events) {
  if (!events.length) {
    list.innerHTML = '<p class="events-empty">No events found. Check back later!</p>';
    return;
  }
  list.innerHTML = events.map(buildCard).join('');
}

function deduplicateEvents(events) {
  const seen = new Set();
  return events.filter(ev => {
    const key = ev.externalId
      ? `${ev.source || 'club'}-${ev.externalId}`
      : `${ev.source || 'club'}-${ev.title || ''}-${ev.location || ''}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function applyFiltersAndSort() {
  let filtered = deduplicateEvents(allEvents).filter(ev => {
    if (currentSource && ev.source !== currentSource) return false;
    return true;
  });

  const q = searchInput.value.trim().toLowerCase();
  if (q) {
    const terms = q.split(/\s+/).filter(t => t.length > 0);
    filtered = filtered.filter(ev => {
      const text = [
        ev.title, ev.description, ev.location, ev.tags, ev.organizer
      ].filter(Boolean).join(' ').toLowerCase();
      return terms.every(term => text.includes(term));
    });
  }

  if (currentSort === 'latest') {
    filtered.sort((a, b) => new Date(b.startDate || 0) - new Date(a.startDate || 0));
  } else {
    filtered.sort((a, b) => new Date(a.startDate || 0) - new Date(b.startDate || 0));
  }

  renderEvents(filtered);
}

function loadEvents() {
  fetch('/api/events')
    .then(r => r.json())
    .then(data => {
      allEvents = data;
      applyFiltersAndSort();
    })
    .catch(() => {
      list.innerHTML = '<p class="events-empty">Could not load events. Please try again later.</p>';
    });
}

function openDetail(id) {
  window.location.href = `event-detail.html?id=${id}`;
}

searchInput.addEventListener('input', () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(applyFiltersAndSort, 300);
});

filterBtn.addEventListener('click', (e) => {
  e.stopPropagation();
  filterPanel.classList.toggle('open');
  sortPanel.classList.remove('open');
});

sortBtn.addEventListener('click', (e) => {
  e.stopPropagation();
  sortPanel.classList.toggle('open');
  filterPanel.classList.remove('open');
});

document.querySelectorAll('input[name="srcFilter"]').forEach(radio => {
  radio.addEventListener('change', () => {
    currentSource = radio.value;
    filterPanel.classList.remove('open');
    applyFiltersAndSort();
  });
});

document.querySelectorAll('.sort-option').forEach(opt => {
  opt.addEventListener('click', () => {
    currentSort = opt.dataset.sort;
    sortLabel.textContent = opt.textContent;
    document.querySelectorAll('.sort-option').forEach(o => o.classList.remove('active'));
    opt.classList.add('active');
    sortPanel.classList.remove('open');
    applyFiltersAndSort();
  });
});

document.addEventListener('click', () => {
  filterPanel.classList.remove('open');
  sortPanel.classList.remove('open');
});

loadEvents();
