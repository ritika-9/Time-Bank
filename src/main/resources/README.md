# ⏰ TimeBank — Skill Exchange Platform

A web platform where users exchange skills using **time credits** instead of money.
1 hour of help = 1 credit.

## Tech Stack
- **Backend:** Java, Spring Boot, Spring Security, JWT
- **Frontend:** Angular
- **Database:** MySQL
- **Real-time:** WebSocket chat

## Features
- JWT Authentication
- Help Requests (demand side)
- Sessions (supply side)
- Real-time chat
- Automated credit transfer
- Google Meet scheduling
- AI-powered matching

## Setup Instructions
1. Clone the repo
2. Copy `application.properties.example` to `application.properties`
3. Fill in your MySQL credentials
4. Run `mvn spring-boot:run`
5. Backend starts on `http://localhost:8080`

## API Endpoints
### Auth
- POST `/api/auth/register`
- POST `/api/auth/login`

### Requests
- POST `/api/requests`
- GET `/api/requests`
- POST `/api/requests/{id}/apply`
- PUT `/api/requests/{id}/accept/{applicantId}`
- PUT `/api/requests/{id}/complete`

### Users
- GET `/api/users/profile`