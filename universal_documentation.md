# Universal Project Documentation

This document provides a comprehensive overview of the SmartSure Insurance Management System, including its architecture, technology stack, and operational procedures.

## Table of Contents
- [1. Overview](#1-overview)
- [2. Technology Stack](#2-technology-stack)
  - [2.1 Backend Technologies](#21-backend-technologies)
  - [2.2 Frontend Technologies](#22-frontend-technologies)
  - [2.3 DevOps & Infrastructure](#23-devops--infrastructure)
- [3. System Architecture](#3-system-architecture)
  - [3.1 High-Level Architecture](#31-high-level-architecture)
  - [3.2 Microservices Overview](#32-microservices-overview)
- [4. End-to-End Execution Flow](#4-end-to-end-execution-flow)
  - [4.1 Local Setup](#41-local-setup)
  - [4.2 Running the Application](#42-running-the-application)
- [5. Code Flow](#5-code-flow)
  - [5.1 API Gateway](#51-api-gateway)
  - [5.2 Service-to-Service Communication](#52-service-to-service-communication)
  - [5.3 Asynchronous Communication](#53-asynchronous-communication)
- [6. Reverse Proxy with Nginx](#6-reverse-proxy-with-nginx)
- [7. CI/CD Pipeline](#7-ci-cd-pipeline)
  - [7.1 GitHub Actions](#71-github-actions)
  - [7.2 SonarQube](#72-sonarqube)
- [8. Deployment](#8-deployment)
  - [8.1 Backend Deployment on Azure VM](#81-backend-deployment-on-azure-vm)
  - [8.2 Frontend Deployment](#82-frontend-deployment)

---

## 2. Technology Stack

### 2.1 Backend Technologies

The backend is built using a microservices architecture with Java and the Spring Boot framework.

- **Java 21**: The core programming language for all backend services.
- **Spring Boot 3.5.13**: Framework for creating stand-alone, production-grade Spring-based Applications.
  - *Why?*: Simplifies the development of new Spring applications with convention over configuration.
- **Spring Cloud 2025.0.1**: Provides tools for developers to quickly build some of the common patterns in distributed systems (e.g., configuration management, service discovery, circuit breakers, intelligent routing, etc.).
  - *Why?*: Essential for building a resilient and scalable microservices architecture.
- **Spring Cloud Gateway**: API Gateway built on top of Spring WebFlux.
  - *Why?*: Provides a single entry point for all client requests, and handles routing, security, and other cross-cutting concerns.
- **Spring Cloud Config**: Centralized configuration management.
  - *Why?*: Allows for managing application configuration across all services in a central place.
- **Spring Cloud Netflix Eureka**: Service discovery.
  - *Why?*: Allows microservices to find and communicate with each other without hardcoding hostnames and ports.
- **Spring Cloud OpenFeign**: Declarative REST client.
  - *Why?*: Simplifies writing HTTP clients for service-to-service communication.
- **Spring Data JPA & PostgreSQL**: For data persistence.
  - *Why?*: JPA provides a standard way to interact with the database, and PostgreSQL is a powerful open-source relational database.
- **Spring Security & JWT**: For authentication and authorization.
  - *Why?*: Secures the application by ensuring that only authenticated and authorized users can access resources.
- **RabbitMQ**: Message broker for asynchronous communication.
  - *Why?*: Decouples services and enables reliable asynchronous communication patterns like publish/subscribe.
- **Razorpay**: Payment gateway integration.
  - *Why?*: To handle online payments for insurance policies.
- **Micrometer & Prometheus**: For metrics collection and monitoring.
  - *Why?*: To gain insights into the application's performance and health.
- **Logstash & ELK Stack**: For centralized logging.
  - *Why?*: To aggregate logs from all services for easier debugging and analysis.
- **SpringDoc OpenAPI**: For API documentation.
  - *Why?*: Automatically generates interactive API documentation.
- **JaCoCo & SonarQube**: For code coverage and quality analysis.
  - *Why?*: To ensure code quality and maintainability.

### 2.2 Frontend Technologies

The frontend is a single-page application built with React.

- **React 19**: A JavaScript library for building user interfaces.
  - *Why?*: Component-based architecture allows for reusable UI elements and efficient development.
- **Vite**: A fast build tool for modern web projects.
  - *Why?*: Provides a faster and leaner development experience compared to older tools like Webpack.
- **TypeScript**: A typed superset of JavaScript.
  - *Why?*: Adds static types to JavaScript, which helps in catching errors early and improving code quality.
- **React Router**: For routing in the React application.
  - *Why?*: Enables navigation between different views in a single-page application.
- **Tailwind CSS**: A utility-first CSS framework.
  - *Why?*: Allows for rapidly building custom designs without writing a lot of custom CSS.
- **Axios**: A promise-based HTTP client for the browser and Node.js.
  - *Why?*: Used for making API requests from the frontend to the backend.
- **Vitest & React Testing Library**: For testing React components.
  - *Why?*: To ensure the UI components work as expected.

### 2.3 DevOps & Infrastructure

- **Docker & Docker Compose**: For containerization and local development setup.
  - *Why?*: To create a consistent development environment and simplify deployment.
- **GitHub Actions**: For CI/CD pipelines.
  - *Why?*: To automate the build, test, and deployment process.
- **Nginx**: As a reverse proxy.
  - *Why?*: To route incoming traffic to the appropriate frontend or backend service.
- **Azure VM**: For hosting the backend services.
  - *Why?*: A flexible and scalable cloud computing service.

## 3. System Architecture

### 3.1 High-Level Architecture

The system follows a microservices architecture. The frontend is a React-based single-page application that communicates with the backend services through an API Gateway. The backend consists of several independent microservices, each responsible for a specific business capability.

[I will add a diagram here later if you want one]

### 3.2 Microservices Overview

- **admin-service**: Manages administrative tasks.
- **api-gateway**: The single entry point for all client requests. It handles routing, authentication, and other cross-cutting concerns.
- **auth-service**: Handles user authentication and authorization.
- **claims-service**: Manages insurance claims.
- **config-server**: Provides centralized configuration for all microservices.
- **eureka-server**: Service discovery server where all microservices register themselves.
- **notification-service**: Sends notifications (e.g., email) to users.
- **payment-service**: Integrates with Razorpay to handle payments.
- **policy-service**: Manages insurance policies.

## 4. End-to-End Execution Flow

### 4.1 Local Setup

To run the application locally, you need to have Docker and Docker Compose installed.

1.  **Start the infrastructure**: Navigate to the root directory and run `docker-compose up -d`. This will start the necessary infrastructure services like PostgreSQL, RabbitMQ, Eureka, and the Config Server.
2.  **Run the backend services**: Each backend service can be started individually by running its main application class from your IDE or by using the Maven Spring Boot plugin (`mvn spring-boot:run`).
3.  **Run the frontend**: Navigate to the `frontend` directory and run `npm install` followed by `npm run dev`.

### 4.2 Running the Application

Once all the services are running, the application can be accessed at `http://localhost:5173` (the default Vite port).

## 5. Code Flow

### 5.1 API Gateway

All incoming requests from the frontend first hit the **API Gateway**. The gateway is responsible for:

1.  **Authentication**: It validates the JWT token present in the request header.
2.  **Routing**: Based on the request path, it forwards the request to the appropriate downstream microservice.

### 5.2 Service-to-Service Communication

Microservices communicate with each other using **Spring Cloud OpenFeign**. For example, if the `claims-service` needs to get policy information, it will use a Feign client to call an endpoint on the `policy-service`. This communication is synchronous.

### 5.3 Asynchronous Communication

For long-running tasks or to decouple services, **RabbitMQ** is used. For example, when a new policy is created, the `policy-service` might publish a message to a RabbitMQ exchange. The `notification-service` would then consume this message and send a confirmation email to the user. This communication is asynchronous.

## 6. Reverse Proxy with Nginx

In a production-like environment, **Nginx** is used as a reverse proxy. It sits in front of the entire application and provides several benefits:

- **Single Host**: It allows you to serve both the frontend and backend from a single domain.
- **Load Balancing**: It can distribute traffic across multiple instances of a service.
- **SSL Termination**: It can handle HTTPS and encrypt/decrypt traffic.

The `nginx.conf` file in the `frontend` directory is configured to:
- Serve the static files of the React application.
- Forward all requests starting with `/api` to the API Gateway.

## 7. CI/CD Pipeline

### 7.1 GitHub Actions

The project uses **GitHub Actions** for continuous integration and continuous deployment. The workflows are defined in the `.github/workflows` directory (I am assuming this location, as it is standard). The pipeline is typically configured to:

1.  **Trigger**: On every push to the `main` branch.
2.  **Build**: Compile the Java code and build the JAR files for each service.
3.  **Test**: Run unit and integration tests.
4.  **Code Analysis**: Run SonarQube analysis to check for code quality and security vulnerabilities.
5.  **Build Docker Images**: Build Docker images for each service.
6.  **Push to Registry**: Push the Docker images to a container registry (e.g., Docker Hub, Azure Container Registry).
7.  **Deploy**: Deploy the new images to the Azure VM.

### 7.2 SonarQube

**SonarQube** is integrated into the CI pipeline to perform static code analysis. The results are pushed to SonarCloud. This helps in maintaining high code quality by identifying bugs, vulnerabilities, and code smells. The SonarQube configuration is present in the `pom.xml` of each backend service.

## 8. Deployment

### 8.1 Backend Deployment and Frontend Deployment  on Azure VM

The backend microservices are deployed as Docker containers on an **Azure Virtual Machine**. The CI/CD pipeline automates this process. The general steps are:

1.  The pipeline builds and pushes the Docker images for each service.
2.  The pipeline connects to the Azure VM (e.g., via SSH).
3.  On the VM, the pipeline pulls the latest Docker images.
4.  The pipeline then stops the old containers and starts the new ones, often using a script or `docker-compose`.
