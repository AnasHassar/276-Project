# SFSS Nexus

## Abstract

SFSS Nexus is a web application for SFU students that collects the information they check most often during a school day and puts it on a single page. After a student logs in, they see a dashboard with the next buses leaving their campus, the current weather and a short forecast for the mountain, upcoming campus events, and a list of student clubs. Right now, that information is spread across several different apps and websites. We are building Nexus on a Spring Boot backend, and it will pull live data from public REST APIs such as TransLink&rsquo;s transit feed and a weather service. The point of the app is to save students time by turning four or five separate tabs into one.

## The Problem

Getting through a day at SFU means juggling a handful of different tools. To check when the next bus leaves for the SkyTrain, a student would open the TransLink Transit app or Google Maps. Before heading up Burnaby Mountain, they would check a weather app. To find out what is happening on campus, they may end up scrolling through club Instagram pages, Reddit, and occasionally posters on the wall. When they need to find a classroom, they open SFU Snap or wander around until they find it.

None of these tools connect to each other or are tailored to an SFU student&rsquo;s actual day. A simple question like &ldquo;What should I check before leaving for campus?&rdquo; can turn into opening several different apps. That is the problem we aim to fix with SFSS Nexus.

## Existing Systems

Existing systems each solve part of the problem that SFSS Nexus aims to bring together. Google Maps gives information on traffic and transit conditions. It also gives estimated arrival times and recommended routes for reaching selected destinations. The SFU website gives campus information including news, academic advising, and educational resources. The Weather Channel website gives information on the current and future forecast of a selected city. SFU Snap is a student-made app for finding rooms and buildings, and it does that one job.

What none of these offer is one student-facing page that pulls the useful parts together, the kind of page a student would open every morning before going to campus.

## How Nexus Improves On It

Nexus plans to condense information relevant to students under one web application. Information regarding forecasts, transit conditions, campus events, and campus resources would be conveniently located in one spot. A potential use case of the app may be opening it before commuting to campus, allowing users to check transit and weather conditions to remain punctual or check for potential closures due to weather conditions or holidays.

Including classroom locations and club information would also make the app more useful and more specific to SFU students. Instead of replacing every existing app, SFSS Nexus focuses on combining the most useful daily information into one simple dashboard.

## Target Audience

Our main users are SFU students, especially the large number who commute to the Burnaby campus and care about bus timing and mountain weather every day. Faculty and staff who are on campus are a secondary audience. The public parts of the app, such as campus information and the events list, could also be useful to prospective students and visitors.

## Scope and Epics

The scope of SFSS Nexus is to build a browser-based web application that gives SFU students one place to check useful campus information. For the first version, we want users to be able to log in, select their main campus, and view a simple dashboard with information like weather, transit, events, and campus resources. This keeps the project realistic while still leaving room to add more features later.

The main epics for the project are login/user profiles, the student dashboard, transit information, weather information, and campus events or clubs. These epics give enough work for five group members because each person can focus on one major feature. For example, one member can work on login and database setup, another can work on the dashboard, another can research and connect the TransLink API, another can work on the weather API, and another can help with events/clubs, testing, documentation, GitHub, and Render deployment.

## Stretch Goals

The Nexus idea is flexible and could allow for more features relevant to SFU students. Some features, such as classroom maps, course reminders, club pages, or messaging between users, can be treated as stretch goals. For example, a reminders tab could display a pop-up based on a user&rsquo;s input for a certain date or time, helping students stay on track for their courses. A previous idea was an &ldquo;SFU social media app,&rdquo; which could allow students to communicate with each other or find new people around campus. That idea is less focused on student utility, but parts of it could be added later to support the &ldquo;one-stop application for SFU students&rdquo; vision.

We would like to include some stretch features if time allows, but they are not required for the first working version. By separating the main features from stretch goals, the project stays manageable while still having enough scope for the course.

## Technical Implementation

SFSS Nexus will be built as a browser-based web application using Spring Boot. The first iteration will include a login system, since this is one of the minimum project requirements. By a later iteration, the app will use at least one public REST API through HTTP/HTTPS. The main APIs we are considering are the TransLink API for bus and transit information and a weather API for campus conditions. We may also explore an events-related API, such as Eventbrite, if it is feasible, but campus events can also be stored manually in our database if needed.

The main planned features are login/user profiles, campus selection for locations such as Surrey and Burnaby, transit information, weather information, and an events/news tab. We will choose APIs based on documentation, accessibility, and whether they are realistic to use without giving sensitive payment information.

## Meeting Notes

### First Meeting

In our first meeting, the main topics were introductions, group member backgrounds, and brainstorming possible project concepts. Ideas ranged from a browser-based video game to applications designed to help SFU students. The idea that was settled upon was a web application for SFU students, mainly due to feasibility and practicality.

We also discussed how the work could be divided across the group. Possible responsibilities include login and database setup, frontend dashboard design, transit API integration, weather API integration, events/clubs features, testing, documentation, GitHub organization, and Render deployment. Project roles may change as the project develops, but this gives each group member a major feature area to contribute to.

### Second Meeting

In our second meeting, we focused mainly on developing the project proposal and making the idea more specific. We reviewed the main problem SFSS Nexus is trying to solve and discussed how students currently use separate tools for transit, weather, events, and campus information. We also worked on explaining the existing systems and how our app would improve on them by combining useful information into one dashboard.

During the meeting, we also discussed the scope of the project and made sure it would be realistic for the course. We decided that login/user profiles, the student dashboard, transit information, weather information, and events or clubs would be the main epics. We also talked about which features should be considered stretch goals, such as classroom maps, reminders, and messaging. By the end of the meeting, we had a clearer proposal structure and a better idea of how the work could be divided between group members.
