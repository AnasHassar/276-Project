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
const preset = new URLSearchParams(window.location.search).get('search');
if (preset && searchInput) searchInput.value = preset;

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
  const tagHtml = tags.slice(0, 2).map(t =>
    `<span class="event-card__tag">${t.trim()}</span>`
  ).join('');
  const dateStr = formatDate(ev.startDate);
  const timeStr = formatTime(ev.startDate);
  const heroHtml = ev.imageUrl
    ? `<img class="event-card__hero" src="${ev.imageUrl}" alt="${ev.title}" loading="lazy" onerror="this.style.display='none'">`
    : `<div class="event-card__hero-placeholder"><span aria-hidden="true">✦</span></div>`;

  return `
    <article class="event-card" data-id="${ev.id}" role="link" tabindex="0" onclick="openEvent('${encodeURIComponent(ev.url || '')}', ${ev.id})" onkeydown="if(event.key === 'Enter' || event.key === ' ') openEvent('${encodeURIComponent(ev.url || '')}', ${ev.id})">
      ${heroHtml}
      <div class="event-card__body">
        <div class="event-card__date">${dateStr}${timeStr ? ' · ' + timeStr : ''}</div>
        <h2 class="event-card__title">${ev.title}</h2>
        ${ev.location ? `<div class="event-card__location">${ev.location}</div>` : ''}
        ${ev.description ? `<p class="event-card__desc">${truncate(ev.description.replace(/<[^>]+>/g, ''), 115)}</p>` : ''}
        ${tagHtml ? `<div class="event-card__tags">${tagHtml}</div>` : ''}
        <span class="event-card__learn-more">Details <span aria-hidden="true">→</span></span>
      </div>
    </article>`;
}

function renderFeatured(event) {
  const featured = document.getElementById('featuredEvent');
  if (!featured) return;
  if (!event) {
    featured.classList.remove('has-featured');
    featured.innerHTML = '';
    return;
  }
  const dateStr = formatDate(event.startDate);
  const timeStr = formatTime(event.startDate);
  const image = event.imageUrl
    ? `<img class="featured-card__image" src="${event.imageUrl}" alt="${event.title}" loading="lazy">`
    : '<div class="featured-card__image featured-card__image--empty"></div>';
  featured.classList.add('has-featured');
  featured.innerHTML = `<article class="featured-card" role="link" tabindex="0" onclick="openEvent('${encodeURIComponent(event.url || '')}', ${event.id})">
    ${image}
    <div class="featured-card__body">
      <span class="featured-card__label">Featured this week</span>
      <h2 class="featured-card__title">${event.title}</h2>
      <div class="featured-card__meta">${dateStr}${timeStr ? ' · ' + timeStr : ''}${event.location ? ' · ' + event.location : ''}</div>
      <span class="featured-card__action">Explore event <span aria-hidden="true">→</span></span>
    </div>
  </article>`;
}

function renderEvents(events) {
  const resultCount = document.getElementById('resultCount');
  if (resultCount) resultCount.textContent = `${events.length} events`;
  const featured = document.getElementById('featuredEvent');
  if (featured) {
    featured.classList.remove('has-featured');
    featured.innerHTML = '';
  }
  if (!events.length) {
    list.innerHTML = '<p class="events-empty">No events found. Check back later!</p>';
    return;
  }
  list.innerHTML = events.map(buildCard).join('');
}

function normalizeText(text) {
  return (text || '').toLowerCase().trim().replace(/\s+/g, ' ');
}

function deduplicateEvents(events) {
  const seen = new Set();
  return events.filter(ev => {
    const key = `${normalizeText(ev.title)}::${normalizeText(ev.location)}::${normalizeText(ev.startDate)}`;
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

function openEvent(encodedUrl, id) {
  const url = decodeURIComponent(encodedUrl || '');
  if (/^https?:\/\//i.test(url)) {
    window.location.href = url;
    return;
  }
  window.location.href = `event-detail.html?id=${encodeURIComponent(id)}`;
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
    const filterCount = document.getElementById('filterCount');
    if (filterCount) filterCount.textContent = currentSource ? '(1)' : '';
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
