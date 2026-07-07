# SFSS Nexus — Submission Details

## GitHub Repository
https://github.com/AnasHassar/276-Project

## Render Web App
<!-- Replace with your actual Render URL once deployed, e.g. https://sfss-nexus.onrender.com -->
TODO: Add Render URL here

## Screencast Demo
<!-- Replace with your screencast link (YouTube, Loom, Google Drive, etc.) -->
TODO: Add screencast link here

### Screencast Requirements
- Address bar must show the Render host URL (not localhost)
- Show the Events page loading real SFU campus events
- Show the event detail page
- Show the admin event creation form (admin-events.html) and the new club event appearing in the feed
- Show the home page with upcoming events section

## Deployment Notes
- The app is deployed on Render.
- PostgreSQL is provided by Render (or a separate Render PostgreSQL service).
- Environment variables are set in Render Dashboard:
  - `URL_DB`
  - `DB_USER`
  - `DB_PASS`
- The app runs on port 8080 inside the container; Render maps it to HTTPS.
