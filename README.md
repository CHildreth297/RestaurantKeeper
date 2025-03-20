# RememberYourFood

## Description
Our restaurant tracker is a diary that keeps track of the restaurants you’ve eaten at, allowing you to record the title, location, date, rating, comments, and photos of the food you ordered. Entries can be organized into collections based on trips, cities, or any other category you choose, helping you remember your favorite spots or places to avoid. Data persists even if the app is closed and reopened, ensuring your entries and collections remain saved, though photos may have some limitations (see the "Other comments" section about this). You can manually enter a location or allow the app to retrieve it automatically with your permission.

## Figma

Starting screen of the app when it opened without any entries or collections:

<img width="259" alt="Screenshot 2025-03-16 at 8 41 48 PM" src="https://github.com/user-attachments/assets/52343513-d944-4906-b7d3-1556d0675e49" />

Clicking the floating action button will open up a dialogue asking if you want to make a collection or entry:

<img width="269" alt="Screenshot 2025-03-16 at 8 42 03 PM" src="https://github.com/user-attachments/assets/72ed5781-9f91-4ae4-80e6-513dc6967d9a" />

The below figma denotes our original design for the collection screen, which was closer to simply being a dialogue that pops up. However, we realized during development that it was missing some notable things, like a description text box and a "select all that apply" mechanism for adding entries you want in the collection. We added those in during development:

<img width="169" alt="Screenshot 2025-03-16 at 8 42 44 PM" src="https://github.com/user-attachments/assets/f5686fec-a65d-4e3a-a450-b11fd6359af1" />

The below figma for the Entry screen was more or less translated to what it actually looks like in the app but with some minor UI alterations, like the images in the app displaying the selected images in a LazyRow instead of a LazyVerticalGrid:

<img width="109" alt="Screenshot 2025-03-16 at 8 42 56 PM" src="https://github.com/user-attachments/assets/7d4245ff-266a-4909-9b4e-4bc084c3c527" />

The below figma ended up most closely resembling how we ended up showing an empty collection on the HomeScreen:

<img width="204" alt="Screenshot 2025-03-16 at 8 43 20 PM" src="https://github.com/user-attachments/assets/95d60b80-84f1-4746-a24d-d99d143db551" />


## Features 
- Google Maps API
   - Permissions
   - ACCESS_FINE_LOCATION
   - ACCESS_COARSE_LOCATION
   - Lat/Lng
- Google Geocoder
- Room database
- Data class
- Navigation controller
- Coroutine / viewModelScope
- Lazy grids/columns
- Viewmodels
- State flows
- Sealed class
- Toast
- Star icons
- Preview tools
- LocalDate

## Dependencies / Android SDK Versions

### App Dependencies & Configuration
- **Compile SDK:** 35  
- **Target SDK:** 34  
- **Minimum SDK:** (Not specified, but likely Android 12 or lower)  
- **Android SDK Version:** Android 12.0 ("S")  
- **API Level:** 31  

### Emulator Dependencies & Configuration
- **Android Version:** Android 15.0  
- **API Level:** 35  
- **AVD ID:** `Medium_Phone_API_35`  
- **Android Emulator Version:** 35.2.10  
- **Android SDK Platform Tools Version:** 35.0.2  

## Other comments

There was one tough bug we were unable to resolve within the submission deadline which involves permissions of re-loading photos when the user leaves the app and comes back. Specifically, the URIs of the images of the food are correctly stored, but there seems to be an issue loading them outside of the PickMultipleVisualMedia. So within one session of usage of the app, photos will be visible but if you leave the app and come back, you will have to unfortunately re-select them as otherwise they will not be visible. 
