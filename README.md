
# WhereAreYou - Android GPS tracking app
This repository contains the source code for a project called "WhereAreYou", an Android app with the ability to record user's locations* and display that information on a map.
This project proposed by teacher Dr. Ghodrat Moghadampour and was developed in collaboration with Huy Pham, who provided the [backend APIs](https://github.com/pqhuy98/gps_tracker).

*Data is only recorded with user's consent.

## Application Features / Requirements:
Quoting the requirements:
> The application allows registering and following GPS locations of users and displaying them on the map. This means that the application should allow a user to register and set the interval (like 5 minutes), after which the application will get the current GPS location of the user and will record new GPS locations of the user at the given intervals until the user tells the app to stop recording . The application should also display the travelled path of the user on the map. The application should also allow following the travelled path by other users. This means that if a user gives the name (which can also be a secrete long code) of another registered user and the date (like 27.2.2020) and time interval (like 10-12), the application will display on the map, the path travelled by the user on the given date and time interval.

Main features:
 - User authentication: login/logout, registering.
 - Custom interval to record / submit location information.
 - Display a user's path on a map in certain time-range (within time-frame of chosen date).
   - Intuitive native inputs - selecting date/time with ease.
 - Work fully in background, by utilizing "foreground service" (showing user a notification about the status of the application).
 - Automatically request permission and location settings if needed (GPS-accuracy, allow locations,...).
 
## UI Screenshots
 *Taken from emulator*
 ### Login screen 
<img src="https://raw.githubusercontent.com/Av0c/gps_tracker_frontend/master/docs/screenshots/login.png" width="300">

 ### Record screen
<img src="https://raw.githubusercontent.com/Av0c/gps_tracker_frontend/master/docs/screenshots/record.png" width="300">

 ### Map screen 
<img src="https://raw.githubusercontent.com/Av0c/gps_tracker_frontend/master/docs/screenshots/map.png" width="300">

 ### Example datepicker 
<img src="https://raw.githubusercontent.com/Av0c/gps_tracker_frontend/master/docs/screenshots/map_control.png" width="300">

## Installation
This project is developed in Android Studio.
To run, clone this repository and import into Android Studio. Then app can be run on an emulator or a connected device. 
It is also possible to build an .apk file to allow installation on another device.
 ### Required SDKs
This project targets Android API 29, with a minimum of API 26.
Make sure your Android Studio has the following SDKs installed (*Android Studio > Tools > SDK Manager*):
 - Android 8.0 Oreo (API 26) up to Android 10.0 Q (API 29).
 - Google Play Services (for Google maps).
 ## Architecture
The application contains 2 main activities:
 - LoginActivity - Activity to handle user login/registering.
 - MainActivity - Activity contains 2 fragments:
   - RecordFragment - Fragment allowing user to select interval and start/stop location recording.
     - LocationUpdatesService - Service handling submitting coordinates to backend, works in foreground/background.
   - MapFragment - Fragment containing map and map control, allowing viewing a user's previous locations.
     - MapControlFragment - Fragment handling parameters for map (date, time, username)

A tabbed layout is implemented, user can switch between screens by swiping or pressing the desired tab in the tab selector.

More detailed information is available as code comments.
