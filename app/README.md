# 🎯 Job Applications Portal

A modern full-stack web application for managing job applications, tracking interview progress, and analyzing resumes using AI-powered insights. Built with React, Spring Boot, and PostgreSQL, containerized with Docker for seamless deployment.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 🎨 Project Overview

**Job Applications Portal** is a comprehensive platform designed to help job seekers manage their application journey:

- **Track Applications**: Monitor the status of all job applications in one centralized dashboard
- **Interview Prep**: Get AI-powered interview questions tailored to specific roles and companies
- **Resume Analysis**: Receive detailed feedback on resume quality, skills gaps, and improvement suggestions
- **Email Notifications**: Automatic email verification and password reset capabilities
- **Role-Based Access**: Support for candidates, recruiters, and administrators

---

## 🛠 Tech Stack

### Frontend
- **React 18** - UI framework
- **Vite 6** - Build tool & dev server
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **React Router** - Client-side navigation
- **Axios** - HTTP client

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.5** - Framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - ORM
- **Jakarta Mail** - Email service
- **Apache Tika** - Resume text extraction

### Database
- **PostgreSQL 17** - Primary database
- **Hibernate** - ORM with automatic schema management

### Deployment
- **Docker & Docker Compose** - Containerization & orchestration
- **nginx/serve** - Static file serving

---

## 🏗 Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│                    (Port 3000 - React/Vite)                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Landing Page  │  Dashboard  │  Profile  │  Auth Pages   │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬─────────────────────────────────────┘
                             │ HTTP/REST
                             │ (API Calls)
┌────────────────────────────▼─────────────────────────────────────┐
│                      API GATEWAY LAYER                            │
│          (Spring Boot Backend - Port 8081)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Authentication  │  User Management  │  AI Services      │  │
│  │  Application Mgmt │  Email Service   │  Resume Analysis  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                   │
│  Security Layer: JWT Tokens, Rate Limiting, CORS                │
└────────────────────────────┬─────────────────────────────────────┘
              │ JDBC           │ SMTP           │ OpenRouter API
              │                │                │
    ┌─────────▼──┐  ┌─────────▼──────┐  ┌──────▼──────────┐
    │ PostgreSQL │  │ Gmail SMTP     │  │ AI Model API   │
    │ Database   │  │ (Email)        │  │ (gpt-3.5)      │
    │ (Port 5432)│  │                │  │                │
    └────────────┘  └────────────────┘  └────────────────┘
```

### Data Flow Diagram

```
User Registration
    │
    ├─► Validation (Email, Password)
    │
    ├─► Hash Password (bcrypt)
    │
    ├─► Store User in DB
    │
    ├─► Generate Verification Token
    │
    ├─► Send Verification Email (SMTP)
    │
    └─► Return JWT Token for Session

Application Tracking
    │
    ├─► User Creates Application Entry
    │
    ├─► Store in Database
    │
    ├─► Update Status (Applied → Reviewed → Interviewed → etc.)
    │
    └─► Dashboard displays real-time status

Resume Analysis (AI)
    │
    ├─► Upload Resume (PDF/DOCX)
    │
    ├─► Extract Text (Apache Tika)
    │
    ├─► Send to OpenRouter API
    │
    ├─► Receive Analysis (Match Score, Suggestions)
    │
    └─► Display Insights to User
```

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker** (latest version) - [Download](https://www.docker.com/products/docker-desktop)
- **Docker Compose** v2+ - Included with Docker Desktop
- **Git** - [Download](https://git-scm.com/)

### System Requirements
- RAM: Minimum 4GB (8GB recommended)
- Disk Space: 2GB available
- OS: Windows 10+, macOS 10.14+, or Linux (Ubuntu 20.04+)

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/rifatbond007/Job-Applications-portal.git
cd Job-Applications-portal/app
```

### Step 2: Configure Environment Variables

Create or update the `.env` file in the app root directory:

```bash
# Copy template if needed
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# Database Configuration
POSTGRES_DB=jobportal1
DB_USERNAME=postgres
DB_PASSWORD=1234

# JWT Configuration
JWT_SECRET=your-32-character-secret-key-minimum-length-required
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# Email Configuration (Gmail with App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
APP_MAIL_FROM=your-email@gmail.com

# AI Configuration (OpenRouter)
OPENROUTER_API_KEY=sk-or-v1-your-actual-key
OPENROUTER_MODEL=gpt-3.5-turbo

# Application Settings
AI_ENABLED=true
RATE_LIMIT_ENABLED=true
RATE_LIMIT_RPM=60
RATE_LIMIT_AI_RPH=20
RATE_LIMIT_LOGIN_RPM=5

# Frontend API URL
VITE_API_URL=http://localhost:8081/api

# Optional
APP_NAME=Job Application Tracker
```

### Step 3: Obtain Prerequisites

#### Gmail App Password (for email delivery)
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable 2-Factor Authentication if not already enabled
3. Go to [App Passwords](https://myaccount.google.com/apppasswords)
4. Select "Mail" and "Windows Computer" (or your device)
5. Copy the 16-character password and paste into `.env` as `MAIL_PASSWORD`

#### OpenRouter API Key (for AI features)
1. Sign up at [OpenRouter](https://openrouter.ai)
2. Navigate to API Keys
3. Create a new API key
4. Copy and paste into `.env` as `OPENROUTER_API_KEY`

---

## ▶️ Running the Application

### Using Docker Compose (Recommended)

**Start the entire stack:**

```bash
# Navigate to app directory
cd Job-Applications-portal/app

# Start all services (db, backend, frontend)
docker compose up -d

# View logs (optional)
docker compose logs -f

# Stop services
docker compose down

# Stop and remove volumes (reset database)
docker compose down -v
```

**Access the application:**
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8081/api
- **Database**: localhost:5432

### Verify Everything is Running

```bash
# Check container status
docker compose ps

# Test frontend
curl http://localhost:3000

# Test backend
curl http://localhost:8081/api/health
```

---

## 📁 Project Structure

```
Job-Applications-Portal/
│
├── app/                                    # Main application directory
│   │
│   ├── client/                            # Frontend (React/Vite)
│   │   ├── src/
│   │   │   ├── components/                # Reusable React components
│   │   │   │   ├── ui/                    # Shadcn UI components
│   │   │   │   ├── landing/               # Landing page components
│   │   │   │   ├── figma/                 # Custom components
│   │   │   │   └── EmptyState.tsx
│   │   │   ├── pages/                     # Page components
│   │   │   │   ├── Auth.tsx
│   │   │   │   ├── Dashboard.tsx
│   │   │   │   ├── EmailVerification.tsx
│   │   │   │   ├── Home.tsx
│   │   │   ├── services/                  # API integration
│   │   │   │   └── api.ts
│   │   │   ├── hooks/                     # Custom React hooks
│   │   │   ├── contexts/                  # Context API
│   │   │   ├── styles/                    # Global styles
│   │   │   ├── App.tsx
│   │   │   └── main.tsx
│   │   ├── Dockerfile                     # Frontend container image
│   │   ├── package.json                   # Dependencies
│   │   ├── vite.config.ts                 # Vite configuration
│   │   └── tsconfig.json
│   │
│   ├── server/                            # Backend (Java/Spring Boot)
│   │   └── jta/                           # Main Java application
│   │       ├── src/main/java/com/Jobtrackr/jta/
│   │       │   ├── ai/                    # AI service module
│   │       │   ├── user/                  # User management
│   │       │   ├── application/           # Job application tracking
│   │       │   ├── company/               # Company data
│   │       │   ├── auth/                  # Authentication & JWT
│   │       │   ├── config/                # Spring configuration
│   │       │   ├── email/                 # Email service
│   │       │   └── JtaApplication         # Main application class
│   │       │
│   │       ├── src/main/resources/
│   │       │   ├── application.properties
│   │       │   └── application-prod.properties
│   │       │
│   │       ├── Dockerfile
│   │       ├── pom.xml
│   │       └── mvnw
│   │
│   ├── docker-compose.yml                 # Container orchestration
│   ├── .env                               # Environment variables (gitignored)
│   ├── .env.example                       # Environment template
│   ├── .dockerignore                      # Docker build optimization
│   ├── README.md                          # This file
│   └── QUICK_START.md                     # Quick start guide
```

---

## ✨ Features

### User Features

#### 🔐 Authentication & Authorization
- User registration with email verification
- JWT-based authentication
- Password reset via email
- Role-based access control (Candidate, Recruiter, Admin)
- Secure password hashing with bcrypt

#### 📊 Application Dashboard
- Track all job applications in one place
- Visual status indicators
- Real-time application count metrics (KPIs)
- Filter and search applications
- One-click application management

#### 🤖 AI-Powered Features
- **Resume Analysis**: Get detailed feedback on match score, strengths, weaknesses, and improvements
- **Interview Prep**: Generate interview questions for specific roles and companies
- **Skill Suggestions**: Personalized skill recommendations and learning paths

#### 📧 Email Notifications
- Automatic verification email on signup
- Password reset emails
- Application status notifications
- Interview reminders

### Technical Features

- **Rate Limiting**: Protect endpoints from abuse (configurable per endpoint)
- **CORS Support**: Secure cross-origin requests
- **Error Handling**: Global exception handling with meaningful error messages
- **Logging**: Comprehensive application logging
- **Health Checks**: Docker health checks for all services
- **Database Persistence**: PostgreSQL with automatic schema management

---

## 🔌 API Endpoints

### Authentication

```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login (returns JWT)
GET    /api/health                 - Health check
```

### Users

```
GET    /api/users/profile          - Get current user profile
PUT    /api/users/profile          - Update user profile
POST   /api/users/password/reset   - Request password reset
```

### Applications

```
GET    /api/applications           - Get all applications (paginated)
POST   /api/applications           - Create new application
GET    /api/applications/{id}      - Get application details
PUT    /api/applications/{id}      - Update application status
DELETE /api/applications/{id}      - Delete application
```

### AI Services

```
POST   /api/ai/analyze-resume      - Analyze resume
POST   /api/ai/interview-prep      - Generate interview questions
POST   /api/ai/suggest-skills      - Get skill suggestions
```

---

## 🗄 Database Schema

### Key Tables
- **users** - User accounts with roles and credentials
- **applications** - Job application records with status
- **resumes** - Uploaded resume files and metadata
- **company** - Company information
- **password_reset_tokens** - JWT tokens for password reset

---

## 🌍 Environment Variables

| Variable | Default | Required |
|----------|---------|----------|
| `POSTGRES_DB` | jobportal1 | ✅ |
| `DB_USERNAME` | postgres | ✅ |
| `DB_PASSWORD` | 1234 | ✅ |
| `JWT_SECRET` | - | ✅ |
| `MAIL_USERNAME` | - | ✅ |
| `MAIL_PASSWORD` | - | ✅ |
| `OPENROUTER_API_KEY` | - | ✅ |
| `VITE_API_URL` | http://localhost:8081/api | ✅ |
| `AI_ENABLED` | true | ❌ |
| `RATE_LIMIT_ENABLED` | true | ❌ |

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>
```

### Docker Won't Start

```bash
# Check Docker daemon
docker ps

# View logs
docker logs jobtracker-backend

# Restart services
docker compose down
docker compose up -d
```

### Email Not Sending

1. Verify `.env` contains correct Gmail credentials
2. Ensure 2FA is enabled on Gmail account
3. Check app password (not regular password)
4. Verify MAIL_PORT is 587
5. Check backend logs: `docker logs jobtracker-backend | grep -i mail`

### Database Connection Issues

```bash
# Check database container
docker logs jobtracker-db

# Verify connection
docker exec jobtracker-db psql -U postgres -d jobportal1 -c "\dt"

# Reset database (WARNING: loses data)
docker compose down -v
docker compose up -d
```

### Frontend Not Loading

1. Check browser console for errors (F12)
2. Verify API URL in `.env`
3. Check backend: `curl http://localhost:8081/api/health`
4. Clear browser cache: Ctrl+Shift+Delete

---

## 🔒 Security Considerations

1. **Never commit `.env` file** - Always use `.env.example` for repository
2. **Use strong JWT_SECRET** - Minimum 32 characters, random
3. **Enable HTTPS in production** - Use SSL/TLS certificates
4. **Protect sensitive data** - Store secrets in environment variables
5. **Rate limiting** - Enabled by default to prevent abuse
6. **Regular backups** - Backup PostgreSQL data regularly
7. **Update dependencies** - Keep libraries and frameworks updated

---

## 🚀 Deployment

For production deployment:

1. Update all environment variables with production values
2. Use strong, randomly generated passwords
3. Enable HTTPS/SSL
4. Configure proper backup strategies
5. Set up monitoring and logging
6. Use container registries for image storage
7. Implement proper CI/CD pipelines

---

## 📝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 📞 Contact

- **GitHub Issues**: [Report bugs](https://github.com/rifatbond007/Job-Applications-portal/issues)
- **Email**: professional.rahuljh@gmail.com

---

## 🎉 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Frontend powered by [React](https://react.dev) & [Vite](https://vitejs.dev)
- UI components from [Shadcn UI](https://ui.shadcn.com)
- Containerization by [Docker](https://www.docker.com)
- AI powered by [OpenRouter](https://openrouter.ai)

---

**Last Updated**: May 23, 2026  
**Version**: 1.0.0
