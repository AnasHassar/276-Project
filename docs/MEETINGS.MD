# SFSS Nexus - Iteration 1 Meeting Notes
**Group:** #4
**Repository:** https://github.com/AnasHassar/276-Project

## Meeting 1 - July 2, 2026 (Iteration 1 planning)

**Participants:** Anas Hassar, Salman Hashir, Maaz Anwar, Manish Madishetty, Christopher Le

**Discussion:**
- Agreed the most important feature would be an Events feature    aimed at incoming current students, who often only hear about events like competitive programming and club activities after searching, or miss them entirely.
- Concept: similar to SFU events, club instagram pages, posters, etc. but focused on all events and information in one place.
- Discussed pulling event data from the Eventbrite API for SFU-related events.

**Decisions:**
- Two user types: standard users (SFU students) who browse events, and admins (clubs, execs) who can post, edit, and delete events.
- Login/logout, registration, roles are required features.
- Landing page needs to be implemented.
- Create events form (for admins) will be added as an additional feature for this iteration.
- Burnaby weather conditions will be added as an additional feature for this iteration.

**Task assignments:**
- Anas - login/logout, registration (role-based), user roles
- Salman - landing page
- Maaz - weather feature
- Manish - event page (displaying events)
- Christopher - create-event form (admin-only)

## Meeting 2 - July 6, 2026 (progress and review)

**Participants:** Anas Hassar, Salman Hashir, Maaz Anwar, Manish Madishetty, Christopher Le

**Status updates:**
- Role-based authentication and registration implemented and merged. - Anas
- Landing page built and pushed. - Salman
- Event form framework built. - Christopher
- Events service created that pulls data from the SFU events website and Eventbrite. - Manish
- Weather feature complete and merged. - Maaz

**Discussion and decisions:**
- Confirmed the app needs a web/desktop landing page rather than a mobile-style UI. Salman to update the layout. No merge conflicts between the current branches.
- Requirements document created

**Action items:**
- Salman — change the landing page to a web/desktop layout
- Team — fill in sections of the requirements document as each feature is finished
- Record a short screencast once the full iteration is working

## SFSS Nexus - Iteration 2 Meeting Notes
## Meeting 1 - July 17, 2026 (Iteration 2 planning)

**Participants:** Anas Hassar, Salman Hashir, Maaz Anwar, Manish Madishetty, Christopher Le

**Work Assignments for Iteration 2:**
- Better UI/UX - Manish
- Translink feature - Christopher & Maaz
- Fix weather API - Maaz
- Fix landing page widgets - Salman
- Club page - Salman
- Club section on the admin dashboard (club CRUD) - Anas
- Admin menu on login (see users, add events, add clubs) - Anas

Decisions: 
- Documentation and screencast can be done Monday, but all code must be
  finalized by Sunday, per the instructor's email.
- Requires writing tests and test user stories.
- All work goes through pull requests, and PRs must be reviewed and/or commented before merging.

## Progress notes - July 18, 2026
- Decided the TransLink GTFS API doesn't return usable JSON, so the transit
  feature will use the Transitland API instead.