# Calendar Booking Application

A Spring Boot application for managing calendar owners, availability, searching and booking appointment slots.

---

## Features

- Create calendar owners  
- Set availability time windows  
- Search available hourly slots  
- Book appointment slots  
- List upcoming appointments  
- In-memory H2 database for persistence  
- API documented with Swagger UI  

---

## Technologies Used

- Java 17+ / Spring Boot
- Spring MVC (REST API)
- H2 Database (in-memory)
- Swagger (OpenAPI) for API documentation
- Maven for build and dependency management

---

## Getting Started

### Prerequisites

- Java 17 or later installed
- Maven installed
- Git installed
- IDE like Eclipse or IntelliJ IDEA

---

### Clone the repository

```bash
git clone https://github.com/bb1160046/calendar-booking.git
cd calendar-booking

---

## Import and Run in Eclipse

### Import as Maven project:

- Open Eclipse  
- Go to **File > Import > Maven > Existing Maven Projects**  
- Select the cloned project folder (`calendar-booking`)  
- Click **Finish**

### Build the project:

- Right-click on the project  
- Select **Run As > Maven clean**  
- Then select **Run As > Maven install**

### Run the application:

- Navigate to `com.accoladehq.calendar.CalendarBookingApiApplication` (or your main class)  
- Right-click > **Run As > Java Application**

---

## Application URLs

| Feature    | URL                            |
|------------|--------------------------------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

---

## H2 Database Console

- **JDBC URL:** `jdbc:h2:mem:caldb`  
- **Username:** `udboy`  
- **Password:**  'udboy' 

Open the H2 console URL, enter the above credentials, and click **Connect**.

---

## Using the API

- Use the Swagger UI to explore and test all available endpoints.  
- Create calendar owners, set availability windows, search available slots, and book appointments via the API.  
- All data is stored in the in-memory H2 database, which resets on every application restart.


 
