# SFSS Nexus

## Overview

SFSS Nexus is a web application for SFU students that consolidates the information they check most often during a school day into a single dashboard. After logging in, students see real-time bus schedules, weather forecasts, upcoming campus events, and student clubs—all powered by a Spring Boot backend that integrates live data from public REST APIs.

**Goal:** Save students time by turning multiple separate apps and tabs into one unified platform.

---

## The Problem

Getting through a day at SFU means juggling several different tools and platforms:

- **Transit:** Checking TransLink Transit app or Google Maps for bus schedules
- **Weather:** Opening a weather app before heading to Burnaby Mountain
- **Events & News:** Scrolling through club Instagram pages, Reddit, and campus posters
- **Navigation:** Using SFU Snap or wandering around to find classrooms

These tools don't connect to each other or account for an SFU student's specific workflow. A simple question like *"What should I check before leaving for campus?"* requires opening 4-5 different apps.

---

## Current Solutions & Limitations

Existing systems address parts of the problem but lack integration:

| System | What It Offers |
|--------|---|
| **Google Maps** | Traffic, transit conditions, arrival estimates |
| **SFU Website** | Campus info, news, academic resources |
| **Weather Channel** | Current weather and forecasts |
| **SFU Snap** | Room/building finder |

**The gap:** No single student-facing page that brings together all the useful information from these sources.

---

## How SFSS Nexus Improves This

Nexus consolidates student-relevant information into one web application:

- **All-in-one dashboard** for transit, weather, events, and campus info
- **Use cases:**
  - Check transit and weather before commuting to stay on time
  - View potential closures due to weather or holidays
  - Browse campus events and student clubs
  - Find classroom locations

---

## Target Audience

- **Primary:** SFU students, especially commuters to Burnaby campus who check bus timing and mountain weather daily
- **Secondary:** Faculty and staff on campus
- **Tertiary:** Prospective students and campus visitors (public-facing features)

---

## Features

### Current Features

- Login and user profiles
- Campus selection (Burnaby, Surrey, etc.)
- Event and news tab

### Future/Stretch Features

- Messaging between users and groups
- Expanded club information
- Interactive classroom map
- Reminders and task management for courses
- Additional campus functionality

---

## Technical Stack

### Backend
- **Framework:** Spring Boot
- **Language:** Java

### APIs & Data Sources
- **TransLink API** – Transit schedules and real-time bus information
- **Weather API** – Current conditions and forecasts
- **Eventbrite API** – Campus events
