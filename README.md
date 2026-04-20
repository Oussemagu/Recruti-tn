# Recruti.tn - Smart Recruitment Platform

Recruti.tn is an AI-powered recruitment platform designed to automate the hiring cycle.  It features automated CV-job matching, integrated technical quizzes, and real-time application tracking

---

## 1. Execution Instructions

### Prerequisites
Ensure you have the following installed on your machine:
*  **Docker & Docker Compose**: Essential for running the containerized services
* **Web Browser**: Latest version of Chrome or Firefox.

### Dependencies & Tools
*  **Frontend**: Angular 17 with Tailwind CSS
*  **Backend**: Spring Boot 3.x (Java 17)
*  **Database**: MySQL 8.x
*  **API Testing**: Postman

### Configuration Files
The project uses a `docker-compose.yml` file to orchestrate the environment.  You may need to configure external API keys (e.g., Google Calendar API, Open Router for AI) in the backend environment variables if you are running the full integration[cite: 143, 166].

### How to Launch
1.  Clone the repository.
2.  Open a terminal in the project root directory.
3.  Run the following command to start all services:
    ```bash
    docker-compose up --build
    ```
4.  Access the application:
    * **Frontend**: `http://localhost:4200`
    *  **Backend API**: `http://localhost:8080`.

---

## 2. Technical Documentation

### Project Structure
 The project follows a **MVC Architecture**:
*  **Presentation Tier (Frontend)**: Developed with Angular, structured into components and services for a reactive UI.
*  **Logic Tier (Backend)**: Built with Spring Boot, following a modular monolithic approach with clear separation of Controllers, Services, and Repositories
*  **Data Tier**: MySQL database managed via Spring Data JPA and Hibernate.

### General Architecture
 The application is fully containerized using **Docker**
*  **Auth**: Stateless authentication using **JWT (JSON Web Tokens)**
*  **Security**: Password hashing using **BCrypt**
*  **Integrations**: OAuth 2.0 for Google Calendar to schedule interviews

### Main Components
*  **Matching Engine**: Automatically scores CVs against job descriptions
*  **Quiz Module**: Allows recruiters to create technical assessments
*  **Interview Scheduler**: Integrated with Google Meet
---

## 3. User Guide

### Main Features
* **For Candidates**: 
    *  Create a profile and upload a CV.
    *  Apply for jobs and take technical quizzes.
    *  Track application status in real-time.
* **For Recruiters**: 
    *  Post and manage job offers.
    *  Review candidate scores (AI-matching + Quiz results).
    *  Schedule interviews via Google Meet.
* **For Admins**:
    *  Monitor platform activity and manage user accounts.

### Test Accounts & Data
For testing purposes, you can use the following roles (if initialized in the database):
* **Admin**: `admin@recruti.tn` / `admin123`
* **Recruiter**: `hr@company.tn` / `recruiter123`
* **Candidate**: `john.doe@email.com` / `candidate123`

