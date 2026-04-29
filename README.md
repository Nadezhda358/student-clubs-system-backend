# Student Clubs System API

Spring Boot API for managing student clubs, club membership applications, club announcements, school events, event registrations, teacher invitations, and admin reports.

## Overview

The API supports four main usage modes:

- Public browsing of active clubs, published events, and published announcements
- Student self-service for account registration, club applications, and event registration
- Teacher management for clubs they are assigned to
- Admin management across the full system, including teacher invites and reporting

The application uses JWT authentication, MySQL, JPA/Hibernate, Flyway migrations, multipart uploads to Amazon S3, and Springdoc OpenAPI for Swagger UI.

## Main Capabilities

- Student registration and login
- Invite-only teacher registration
- Role-based access for `STUDENT`, `TEACHER`, and `ADMIN`
- Club CRUD, teacher assignment, main image upload, and gallery media upload
- Club membership application review and approval flow
- Event CRUD with draft/published/cancelled states
- Student event registration and cancellation
- Public and management announcement workflows
- Admin reports for overview metrics and events grouped by period
- Soft-delete-aware queries that hide deleted or orphaned data from API consumers

## Tech Stack

- Java 17
- Spring Boot 4.0.1
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Flyway
- MySQL
- JWT via `jjwt`
- Swagger/OpenAPI via Springdoc
- AWS SDK v2 for S3 uploads and presigned read URLs
- Spring Mail for teacher invitation emails

## Domain Model

The schema is created and evolved through Flyway migrations under `src/main/resources/db/migration`.

Core tables/entities:

- `users`: students, teachers, admins
- `teacher_invites`: hashed invite tokens with expiration and usage tracking
- `clubs`
- `club_teachers`: teacher-to-club assignments
- `club_media`: additional club gallery images
- `club_membership_requests`: pending/approved/rejected/cancelled applications
- `club_memberships`: active/left/banned club memberships
- `events`
- `event_registrations`
- `announcements`

Soft delete is used across the main aggregates. Queries are written to avoid leaking deleted rows and to avoid surfacing records whose related club/user/event is no longer available.

## Roles and Access

| Role | What it can do |
| --- | --- |
| Public | Browse active clubs, published events, and published announcements |
| Student | Register an account, apply to clubs, view own clubs/applications/events, register for events |
| Teacher | Manage assigned clubs, assigned club announcements, assigned club events, and view participants |
| Admin | Full management of clubs, events, announcements, teachers, teacher invites, memberships, and reports |

Important access rules:

- `GET /api/clubs/**`, `GET /api/events/**`, and `GET /api/announcements/**` are public.
- Teacher endpoints live under `/api/teacher/**`.
- Admin endpoints live under `/api/admin/**`.
- Teachers can view event participants but cannot change participation status.
- Only admins can create teacher invites.
- Teacher registration is not open; it requires a valid invite token.

## Project Structure

- `src/main/java/.../controllers`: HTTP endpoints
- `src/main/java/.../services`: business logic
- `src/main/java/.../repositories`: JPA repositories and custom queries
- `src/main/java/.../security`: JWT generation and authentication filter
- `src/main/java/.../config`: security, S3, and OpenAPI configuration
- `src/main/resources/application.properties`: default runtime configuration
- `src/main/resources/db/migration`: Flyway migrations
- `src/test/java/...`: service and validation regression tests

## Local Development

### Prerequisites

- Java 17
- MySQL running on port `3307`, or a different host supplied through `MYSQL_HOST`
- Optional: Mailpit/MailHog/SMTP server if you want to test teacher invite emails without the `dev` profile
- Optional: AWS credentials and an S3 bucket if you want media upload endpoints to work

### Default Database Configuration

The app starts with this datasource URL by default:

```text
jdbc:mysql://${MYSQL_HOST:localhost}:3307/student_clubs_system?autoreconnect=true&createDatabaseIfNotExist=true&characterEncoding=utf8
```

Current defaults in `application.properties`:

- Database name: `student_clubs_system`
- Username: `root`
- Password: empty
- Port: `8080`

Flyway is enabled, and `spring.jpa.hibernate.ddl-auto=update` is also enabled in the current configuration.

### Recommended Local Run

Use the `dev` profile when working locally if you do not want the app to send real emails. In `dev`, teacher invitation emails are logged instead of being sent through SMTP.

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

### OpenAPI / Swagger

Once the app is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Seeded Test Accounts

Migration `V21__seed_bulgarian_test_data.sql` adds believable Bulgarian demo data to every active table in the schema.

All seeded users currently use the same password:

```text
Parola2026!
```

Seeded login accounts:

- Admin: `maria.nikolova@ppmg-vratsa.bg`
- Teachers: `daniela.georgieva@ppmg-vratsa.bg`, `nikolay.hristov@ppmg-vratsa.bg`, `milena.todorova@ppmg-vratsa.bg`, `elitsa.stoyanova@ppmg-vratsa.bg`, `viktor.petrov@ppmg-vratsa.bg`
- Students: `petar.ivanov@student.ppmg-vratsa.bg`, `aleks.borisov@student.ppmg-vratsa.bg`, `viktoriya.dimitrova@student.ppmg-vratsa.bg`, `simona.ilieva@student.ppmg-vratsa.bg`, `georgi.kolev@student.ppmg-vratsa.bg`, `niya.marinova@student.ppmg-vratsa.bg`, `stefan.genov@student.ppmg-vratsa.bg`, `radostina.peneva@student.ppmg-vratsa.bg`

## Configuration

### Environment Variables and Properties

| Key | Default | Purpose |
| --- | --- | --- |
| `MYSQL_HOST` | `localhost` | Host used inside the datasource URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | Override DB username through Spring Boot external config |
| `SPRING_DATASOURCE_PASSWORD` | empty | Override DB password through Spring Boot external config |
| `APP_JWT_SECRET` | `student-clubs-system-dev-secret-key-2026` | JWT signing secret; use a strong production secret |
| `APP_JWT_EXP_MIN` | `60` | JWT lifetime in minutes |
| `AWS_S3_BUCKET` | empty | S3 bucket used for uploads |
| `AWS_S3_REGION` | `eu-north-1` | AWS region for S3 client/presigner |
| `AWS_S3_MAX_FILE_SIZE` | `5242880` | Per-file upload limit in bytes |
| `AWS_S3_GET_URL_TTL_MINUTES` | `60` | Presigned read URL lifetime |
| `TEACHER_INVITE_BASE_URL` | `http://localhost:8081/register/teacher` | Frontend page used in invite emails |
| `TEACHER_INVITE_FROM_EMAIL` | `spring.mail.username` or `no-reply@example.com` | Sender address for invite emails |
| `TEACHER_INVITE_SUBJECT` | `Teacher account invitation` | Subject used for invite emails |
| `SMTP_HOST` | `localhost` | SMTP host |
| `SMTP_PORT` | `1025` | SMTP port |
| `SMTP_USERNAME` | empty | SMTP username |
| `SMTP_PASSWORD` | empty | SMTP password |
| `SMTP_AUTH` | `false` | SMTP auth flag |
| `SMTP_STARTTLS` | `true` | STARTTLS flag |

### S3 Requirements

Upload endpoints require all of the following:

- `AWS_S3_BUCKET` configured
- Valid AWS credentials available through the AWS default credential provider chain
- Supported image content type

If `AWS_S3_BUCKET` is blank, upload endpoints fail at runtime.

## Authentication

### Login Flow

1. Call `POST /api/auth/login`
2. Receive a JWT access token
3. Send the token in the `Authorization` header:

```http
Authorization: Bearer <access-token>
```

### JWT Behavior

- Token type in responses: `Bearer`
- Default expiration: `3600` seconds
- Subject: user email
- Claims include:
  - `uid`
  - `role` in Spring Security format, for example `ROLE_STUDENT`

### Teacher Invite Flow

1. Admin calls `POST /api/admin/teacher-invites`
2. The API creates a random token, stores only its SHA-256 hash, and sets a 48-hour expiry
3. The invite link is emailed, or logged in the `dev` profile
4. The invited teacher completes registration through `POST /api/auth/register/teacher`

If every supplied email already belongs to an existing user, the invite endpoint returns `409 Conflict`.

## API Conventions

### Date and Time

- Request and response timestamps use ISO-8601 `OffsetDateTime`
- Event filters such as `from` and `to` also expect ISO-8601 date-time values
- The persistence layer normalizes `OffsetDateTime` values through a `Europe/Sofia` converter before storing them in the database

### Pagination

Most list endpoints return Spring Data `Page<T>` responses.

Common query parameters:

- `page`: zero-based page index
- `size`: page size
- `sort`: Spring sort syntax such as `sort=name,asc`

Common defaults:

- Clubs: name ascending
- Announcements: published date descending, then created date descending
- Public/admin/teacher event listings: business ordering that puts upcoming events before past events unless the client supplies an explicit sort
- Some personalized participation endpoints enforce fixed domain-specific ordering

### Error Format

Errors are returned by the global exception handler in this shape:

```json
{
  "status": 409,
  "error": "Human readable message",
  "timestamp": "2026-04-29T12:34:56+03:00"
}
```

The most common statuses are:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`

### File Upload Rules

Multipart configuration:

- Maximum file size: `5 MB` per file
- Maximum request size: `100 MB`
- Maximum part count: `50`

Supported image content types:

- `image/jpeg`
- `image/jpg`
- `image/png`
- `image/webp`

Upload field names differ by endpoint and are documented below.

### Enum Values

These are the wire values to send in requests and expect in responses:

- `UserRole`: `STUDENT`, `TEACHER`, `ADMIN`
- `EventStatus`: `DRAFT`, `PUBLISHED`, `CANCELLED`
- `EventTimeFilter`: `UPCOMING`, `PAST`, `ALL`
- `RegistrationStatus`: `REGISTERED`, `CANCELLED`
- `MembershipRequestStatus`: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`
- `MembershipStatus`: `ACTIVE`, `LEFT`, `BANNED`
- `EventAudience`: `ALL_STUDENTS`, `MEMBERS_ONLY`
- `ReportPeriod`: `DAY`, `WEEK`, `MONTH`

Internally, the codebase also carries Bulgarian display text for several enums and validation messages, but persisted enum names and API wire values remain English uppercase identifiers.

## Request Models

### `LoginRequest`

```json
{
  "email": "student@example.com",
  "password": "secret"
}
```

### `RegisterStudentRequest`

```json
{
  "email": "student@example.com",
  "password": "secret",
  "firstName": "Ivan",
  "lastName": "Petrov",
  "grade": 10,
  "className": "B"
}
```

Validation notes:

- `grade` must be between `1` and `12`
- `firstName` and `lastName` max length: `80`
- `className` max length: `20`

### `RegisterTeacherRequest`

```json
{
  "token": "invite-token",
  "password": "secret",
  "firstName": "Maria",
  "lastName": "Ivanova"
}
```

### Club Create / Update Payload

JSON create uses `CreateClubDto`; admin update uses `UpsertClubDto`; teacher update uses `ManageClubDto`.

Shared writable fields:

```json
{
  "name": "Robotics Club",
  "description": "Hands-on robotics and programming club.",
  "scheduleText": "Every Tuesday 15:30",
  "room": "Lab 204",
  "contactEmail": "robotics@example.com",
  "contactPhone": "+359 888 123 456",
  "isActive": true
}
```

Additional create-only JSON field:

```json
{
  "teacherIds": [5, 9]
}
```

Notes:

- `name` max length: `160`
- `description` max length: `5000`
- `scheduleText` max length: `2000`
- `room` max length: `80`
- `contactPhone` accepts digits, spaces, parentheses, dashes, and an optional leading `+`
- Club names must be unique
- The admin update DTO currently exposes `createdById`, but the service logic does not apply it

### Club Multipart Create Payload

`POST /api/clubs` also supports `multipart/form-data` with these field names:

- `name`
- `description`
- `scheduleText`
- `room`
- `contactEmail`
- `contactPhone`
- `isActive`
- `teacherIds`
- `mainImage`
- `mediaFiles`

### Membership Application Payload

```json
{
  "motivationText": "I want to join because..."
}
```

### Membership Decision Payload

```json
{
  "status": "APPROVED"
}
```

Allowed decision values for membership review endpoints:

- `APPROVED`
- `REJECTED`

### Event Create / Update Payload

```json
{
  "clubId": 1,
  "title": "Robotics Showcase",
  "description": "Student teams present their work.",
  "startAt": "2026-05-10T15:00:00+03:00",
  "endAt": "2026-05-10T17:00:00+03:00",
  "location": "Main Hall",
  "capacity": 30,
  "registrationDeadline": "2026-05-09T23:59:00+03:00",
  "status": "PUBLISHED",
  "audience": "ALL_STUDENTS"
}
```

Validation and lifecycle rules:

- `title` max length: `200`
- `description` max length: `5000`
- `capacity` may be `null` for unlimited attendance
- `endAt` cannot be before `startAt`
- `registrationDeadline` cannot be after `startAt`
- Published events must not start in the past
- Published events must not have a past registration deadline
- Published events cannot be edited after they start
- Past cancelled events cannot be rescheduled; create a new event instead

### Event Participation Status Payload

```json
{
  "status": "CANCELLED"
}
```

Allowed values:

- `REGISTERED`
- `CANCELLED`

### Announcement Create / Update Payload

```json
{
  "clubId": 1,
  "title": "Next Club Meeting",
  "body": "Bring your laptop and project ideas.",
  "isPublished": true
}
```

Validation notes:

- `title` max length: `200`
- `body` max length: `8000`
- When `isPublished=true`, the service sets `publishedAt` if it was not already set
- When `isPublished=false`, the service clears `publishedAt`

### Teacher Invite Payload

```json
{
  "emails": [
    "teacher1@example.com",
    "teacher2@example.com"
  ]
}
```

## Endpoint Reference

### Authentication

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `POST` | `/api/auth/register` | Public | Register a student account |
| `POST` | `/api/auth/register/teacher` | Public | Register a teacher account from a valid invite token |

### Public Clubs

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/clubs` | Public | List clubs. Anonymous and students effectively see only active clubs. Teachers/admins may also filter inactive clubs. Query params: `active`, `q`, `page`, `size`, `sort` |
| `GET` | `/api/clubs/{id}` | Public | Get club details. Inactive clubs are hidden from anonymous and student callers |

### Admin Club Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/clubs` | Admin | Create a club from JSON |
| `POST` | `/api/clubs` | Admin | Create a club from multipart form data |
| `PUT` | `/api/clubs/{id}` | Admin | Update a club |
| `DELETE` | `/api/clubs/{id}` | Admin | Delete a club |
| `POST` | `/api/clubs/{id}/teachers` | Admin | Add teachers to a club. Body: `{"teacherIds":[...]}` |
| `DELETE` | `/api/clubs/{id}/teachers/{teacherId}` | Admin | Remove a teacher assignment |
| `POST` | `/api/clubs/{id}/main-image` | Admin | Upload or replace the main club image. Multipart field: `file` |
| `POST` | `/api/clubs/{id}/media` | Admin | Append gallery images. Multipart field: `files` |
| `DELETE` | `/api/clubs/{id}/media/{mediaId}` | Admin | Remove a gallery image |

Club deletion behavior:

- Future club events are marked `CANCELLED`
- Club announcements are soft-deleted
- Active memberships are marked as left
- Membership applications are soft-deleted
- The club itself is soft-deleted

### Teacher Club Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/teacher/clubs` | Teacher | List clubs assigned to the current teacher. Query params: `active`, `q`, `page`, `size`, `sort` |
| `GET` | `/api/teacher/clubs/{id}` | Teacher | Get one managed club |
| `PUT` | `/api/teacher/clubs/{id}` | Teacher | Update one managed club |
| `POST` | `/api/teacher/clubs/{id}/main-image` | Teacher | Upload or replace the main club image. Multipart field: `file` |
| `POST` | `/api/teacher/clubs/{id}/media` | Teacher | Append gallery images. Multipart field: `files` |
| `DELETE` | `/api/teacher/clubs/{id}/media/{mediaId}` | Teacher | Remove club media from a managed club |

Teachers cannot:

- Delete clubs
- Add or remove teacher assignments through teacher endpoints

### Student Club Endpoints

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/me/clubs` | Student | List active club memberships for the current student by default. Query params: `active`, `q`, `page`, `size` |

### Membership Applications

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/clubs/{clubId}/membership-applications` | Student | Apply to join a club |
| `GET` | `/api/me/membership-applications` | Student | List own applications. Query params: `status`, `clubId`, `q`, `page`, `size` |
| `POST` | `/api/me/membership-applications/{id}/cancel` | Student | Cancel a pending application |
| `GET` | `/api/admin/membership-applications` | Admin | Review all applications. Query params: `status`, `clubId`, `q`, `page`, `size` |
| `POST` | `/api/admin/membership-applications/{id}` | Admin | Approve or reject an application |
| `GET` | `/api/teacher/membership-applications` | Teacher | Review applications for managed clubs. Query params: `status`, `clubId`, `q`, `page`, `size` |
| `POST` | `/api/teacher/membership-applications/{id}` | Teacher | Approve or reject an application for a managed club |

Membership rules:

- Students can apply only to active clubs
- Only one pending application per student/club is allowed
- Active members cannot apply again
- Banned students cannot apply again
- Only pending applications can be cancelled
- Review endpoints accept only `APPROVED` or `REJECTED`
- Approval activates or restores a club membership unless that membership is banned

### Public and Student Event Endpoints

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/events` | Public | List published events from active clubs. Query params: `clubId`, `q`, `from`, `to`, `timeFilter`, `page`, `size`, `sort` |
| `GET` | `/api/events/{id}` | Public | Get one published event from an active club |
| `GET` | `/api/me/events/registered` | Student | List current student's registered public events. Query params: `clubId`, `q`, `from`, `to`, `timeFilter`, `page`, `size` |
| `POST` | `/api/events/{id}/registrations` | Student | Register the current student for an event |
| `DELETE` | `/api/events/{id}/registrations` | Student | Cancel the current student's registration |
| `GET` | `/api/me/events` | Student | List current student's participations. Query params: `registrationStatus`, `eventStatus`, `q`, `timeFilter`, `page`, `size` |

Event registration rules:

- Only students can self-register
- The club must be active
- The event must be `PUBLISHED`
- Registration must happen on or before the effective deadline
- Effective deadline is `registrationDeadline` when present, otherwise `startAt`
- Capacity is enforced when `capacity` is not `null`
- `MEMBERS_ONLY` events require an active club membership
- Students can re-register only from a previously cancelled participation
- Students can cancel only active registrations, and only before the effective deadline
- `GET /api/me/events` defaults `timeFilter` to `UPCOMING`
- Other event listing endpoints default `timeFilter` to `ALL`

### Teacher Event Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/teacher/events` | Teacher | List events for managed clubs. Query params: `clubId`, `q`, `from`, `to`, `timeFilter`, `status`, `page`, `size`, `sort` |
| `GET` | `/api/teacher/events/{id}` | Teacher | Get one managed event |
| `POST` | `/api/teacher/events` | Teacher | Create an event for a managed club |
| `PUT` | `/api/teacher/events/{id}` | Teacher | Update a managed event |
| `DELETE` | `/api/teacher/events/{id}` | Teacher | Delete a managed event |
| `POST` | `/api/teacher/events/{id}/main-image` | Teacher | Upload or replace the event main image. Multipart field: `file` |
| `GET` | `/api/teacher/events/{id}/participants` | Teacher | List participants for one managed event. Query params: `status`, `q`, `page`, `size` |
| `GET` | `/api/teacher/events/event-participations` | Teacher | List participations across managed clubs. Query params: `clubId`, `eventId`, `registrationStatus`, `eventStatus`, `q`, `timeFilter`, `page`, `size` |

Teachers can view participant data but cannot update participant statuses.

### Admin Event Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/events` | Admin | List all available events. Query params: `clubId`, `q`, `from`, `to`, `timeFilter`, `status`, `page`, `size`, `sort` |
| `GET` | `/api/admin/events/{id}` | Admin | Get one event |
| `POST` | `/api/admin/events` | Admin | Create an event |
| `PUT` | `/api/admin/events/{id}` | Admin | Update an event |
| `DELETE` | `/api/admin/events/{id}` | Admin | Delete an event |
| `POST` | `/api/admin/events/{id}/main-image` | Admin | Upload or replace the event main image. Multipart field: `file` |
| `GET` | `/api/admin/events/{id}/participants` | Admin | List participants for one event. Query params: `status`, `q`, `page`, `size` |
| `PATCH` or `POST` | `/api/admin/events/{eventId}/participants/{studentId}` | Admin | Update one participation status |
| `GET` | `/api/admin/event-participations` | Admin | List participations system-wide. Query params: `clubId`, `eventId`, `registrationStatus`, `eventStatus`, `q`, `timeFilter`, `page`, `size` |

Admin participation status rules:

- Only `REGISTERED` and `CANCELLED` are accepted
- Status cannot be changed after the event has started
- Reopening a participation is only allowed from `CANCELLED`
- Cancelling is only allowed from `REGISTERED`
- Re-registering still enforces audience and capacity checks
- Admins cannot register people into cancelled events

Event deletion behavior:

- Deleting an event soft-deletes its registrations before soft-deleting the event itself

### Public Announcements

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/announcements` | Public | List published announcements from active clubs. Query params: `clubId`, `q`, `from`, `to`, `page`, `size`, `sort` |
| `GET` | `/api/announcements/{id}` | Public | Get one published announcement from an active club |

### Teacher Announcement Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/teacher/announcements` | Teacher | List announcements for managed clubs. Query params: `clubId`, `published`, `q`, `from`, `to`, `page`, `size`, `sort` |
| `GET` | `/api/teacher/announcements/{id}` | Teacher | Get one managed announcement |
| `POST` | `/api/teacher/announcements` | Teacher | Create an announcement for a managed club |
| `PUT` | `/api/teacher/announcements/{id}` | Teacher | Update an announcement for a managed club |
| `DELETE` | `/api/teacher/announcements/{id}` | Teacher | Delete an announcement for a managed club |

### Admin Announcement Management

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/announcements` | Admin | List announcements. Query params: `clubId`, `published`, `q`, `from`, `to`, `page`, `size`, `sort` |
| `GET` | `/api/admin/announcements/{id}` | Admin | Get one announcement |
| `POST` | `/api/admin/announcements` | Admin | Create an announcement |
| `PUT` | `/api/admin/announcements/{id}` | Admin | Update an announcement |
| `DELETE` | `/api/admin/announcements/{id}` | Admin | Delete an announcement |

Announcement visibility rules:

- Public endpoints show only published announcements
- Public endpoints also require the related club to be active
- Teacher/admin management endpoints exclude deleted or orphaned clubs

### Teacher Administration

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/teachers` | Admin | List teacher accounts. Query params: `q`, `page`, `size`, `sort` |
| `POST` | `/api/admin/teacher-invites` | Admin | Create teacher invites for one or more email addresses |

### Admin Reports

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/reports/overview` | Admin | Overview metrics. Query params: `from`, `to` |
| `GET` | `/api/admin/reports/events-by-period` | Admin | Event counts grouped by period. Query params: `from`, `to`, `period` |

Report behavior:

- `period` defaults to `MONTH` when omitted
- Reports count non-draft events only
- Report queries avoid counting data that belongs to deleted or unavailable clubs

## Example Responses

### `LoginResponse`

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 12,
    "email": "student@example.com",
    "firstName": "Ivan",
    "lastName": "Petrov",
    "role": "STUDENT"
  }
}
```

### `EventDto`

```json
{
  "id": 101,
  "clubId": 1,
  "clubName": "Robotics Club",
  "title": "Robotics Showcase",
  "description": "Student teams present their work.",
  "startAt": "2026-05-10T15:00:00+03:00",
  "endAt": "2026-05-10T17:00:00+03:00",
  "location": "Main Hall",
  "mainImageUrl": "https://...",
  "capacity": 30,
  "registeredCount": 18,
  "availableSpots": 12,
  "registrationDeadline": "2026-05-09T23:59:00+03:00",
  "effectiveRegistrationDeadline": "2026-05-09T23:59:00+03:00",
  "registrationOpen": true,
  "status": "PUBLISHED",
  "audience": "ALL_STUDENTS",
  "createdById": 7,
  "createdByName": "Maria Ivanova",
  "createdAt": "2026-04-20T12:00:00+03:00",
  "updatedAt": "2026-04-22T09:15:00+03:00"
}
```

## Operational Notes

- Club and event image URLs are resolved through S3 presigned URLs when possible.
- If a custom sort is not supplied for public/admin/teacher event listing endpoints, upcoming events are returned before past events.
- Query parameters `from` and `to` are validated; `from` must not be after `to`.
- The API accepts Cyrillic form content in validation-tested request models.

## Useful Commands

Build:

```powershell
.\mvnw.cmd clean package
```

Run tests:

```powershell
.\mvnw.cmd test
```

Run locally in dev mode:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```
