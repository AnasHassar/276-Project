const SOURCE_READ_MORE = {
  eventbrite: 'Read more on Eventbrite',
  sfu_events: 'Read more on SFU Events',
  club: 'View event details'
};

function formatDate(iso) {
  if (!iso) return 'Date TBA';
  const d = new Date(iso);
  return d.toLocaleDateString('en-CA', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
}

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleTimeString('en-CA', { hour: 'numeric', minute: '2-digit' });
}

function renderDetail(ev) {
  const tags = ev.tags ? ev.tags.split(',').filter(t => t.trim()) : [];
  const tagHtml = tags.slice(0, 6).map(t =>
    `<span class="event-card__tag">#${t.trim()}</span>`
  ).join('');

  const heroHtml = ev.imageUrl
    ? `<img class="event-detail-hero" src="${ev.imageUrl}" alt="${ev.title}" onerror="this.style.display='none'">`
    : `<div class="event-detail-hero-placeholder">
         <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="1.5">
           <rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/>
         </svg>
       </div>`;

  const descriptionText = ev.description
    ? ev.description.replace(/<[^>]+>/g, '').substring(0, 600)
    : 'No description available.';

  const readMoreLabel = SOURCE_READ_MORE[ev.source] || 'View original event';

  const mapsUrl = ev.location
    ? `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(ev.location)}`
    : '#';

  const hostedBy = ev.organizer && ev.organizer.trim()
    ? `Hosted by ${ev.organizer}`
    : '';

  const endTimeHtml = ev.endDate
    ? `<div class="detail-row">
         <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
         <span>${formatTime(ev.startDate)}${ev.endDate ? ' – ' + formatTime(ev.endDate) : ''}</span>
       </div>`
    : (ev.startDate ? `<div class="detail-row">
         <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
         <span>${formatTime(ev.startDate)}</span>
       </div>` : '');

  const content = document.getElementById('detailContent');
  content.innerHTML = `
    <div class="card" style="padding:0;overflow:hidden;margin-bottom:16px">
      ${heroHtml}
      <div class="event-detail-card">
        <h1>${ev.title}</h1>
        ${hostedBy ? `<p class="hosted-by">${hostedBy}</p>` : ''}
        <div class="detail-rows">
          <div class="detail-row">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/></svg>
            <span>${formatDate(ev.startDate)}</span>
          </div>
          ${endTimeHtml}
          ${ev.location ? `<div class="detail-row">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a7 7 0 0 1 7 7c0 5-7 13-7 13S5 14 5 9a7 7 0 0 1 7-7z"/><circle cx="12" cy="9" r="2.5"/></svg>
            <span>${ev.location} &nbsp;<a href="${mapsUrl}" target="_blank" rel="noopener">View Map</a></span>
          </div>` : ''}
        </div>

        ${tagHtml ? `<div class="event-card__tags" style="margin-bottom:18px">${tagHtml}</div>` : ''}

        <h2>About this Event</h2>
        <p class="about-text">${descriptionText}${ev.description && ev.description.replace(/<[^>]+>/g, '').length > 600 ? '…' : ''}</p>
        ${ev.url ? `<a class="read-more-link" href="${ev.url}" target="_blank" rel="noopener">${readMoreLabel} ↗</a>` : ''}
      </div>
    </div>`;

  document.title = `${ev.title} — SFSS Nexus`;
}

const params = new URLSearchParams(window.location.search);
const id = params.get('id');

if (!id) {
  window.location.href = 'events.html';
} else {
  fetch(`/api/events/${id}`)
    .then(r => {
      if (!r.ok) throw new Error('Not found');
      return r.json();
    })
    .then(renderDetail)
    .catch(() => {
      document.getElementById('detailContent').innerHTML =
        '<p class="events-empty">Event not found. <a href="events.html" style="color:var(--red-bright)">Back to events</a></p>';
    });
}
