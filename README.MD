SFSS Nexus
Abstract
SFSS Nexus is a web application for SFU students that collects the information they check most often during a school day and puts it on a single page. After a student logs in, they see a dashboard with the next buses leaving their campus, the current weather and a short forecast for the mountain, upcoming campus events, and a list of student clubs. Right now that information is spread across several different apps and websites. We are building the Nexus on a Spring Boot backend and it pulls live data from public REST APIs such as TransLink’s transit feed and a weather service. The point of the app is to save students time by turning four or five separate tabs into one.

The Problem

Getting through a day at SFU means juggling a handful of various tools. To check when the next bus leaves for the SkyTrain, a student would open the TransLink Transit app or Google Maps. Before heading up Burnaby Mountain they would check a weather app. To find out what's happening on campus they end up scrolling through club Instagram pages, Reddit, and occasionally posters on the wall. When they need to find a classroom they open SFU Snap or wander around till they find it.

None of these tools connect to each other, or are tailored to an SFU student’s actual day. So a simple question like “what should I check before leaving for campus” turns into opening various different apps. That is the problem we aim to fix with SFSS Nexus.

Existing Systems

Alternative systems are more specific to what SFU Nexus plans to offer. Google Maps gives information on traffic and transit conditions. It also gives estimated arrival times and recommended routes for reaching selected destinations. The SFU websites gives campus information including news, academic advising and educational resources. The Weather Channel website gives information on the current and future forecast of a selected city. SFU Snap is a student-made app for finding rooms and buildings and it does that one job.

What none of these offer is one student-facing page that pulls the useful parts together, the kind of page you would open every morning.

How Nexus Improves On it

Nexus plans to condense information relevant to a student under one web application. Information regarding forecast, transit conditions and campus events would conveniently be located in one spot. A potential use case of the app may be opening it prior to commuting to campus, allowing users to check on transit and weather conditions to remain punctual, or checking for potential closures due to weather conditions or holidays.

Information on classroom locations and clubs may also increase the tailorability towards SFU students. 
Target Audience
Our main users are SFU students, especially the large number who commute to the Burnaby campus and care about bus timing and mountain weather every day. Faculty and staff who are on campus are a secondary audience. The public parts of the app, like campus info and the events list, could also be useful to prospective students and visitors.
Scope

Future/Stretch
Messaging between users/groups (social media app?)
Club information
Classroom map
Reminders tab for courses
Technical (APIs, etc.)
USES:
Translink API
Weather API
Eventbrite API


FEATURES:
Login / User profiles
Select campus to get information of (Surrey, Burnaby, ect)
Event / News tab

