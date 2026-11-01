# Contrat d'API JobTrack v1

Base URL : `/api/v1`
Toutes les routes sont protégées par `Authorization: Bearer <accessToken>` sauf `auth/**` et `health`.
Les erreurs utilisent le format RFC 7807 :

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "La requête est invalide.",
  "instance": "/api/v1/applications",
  "errors": [{ "field": "company", "message": "ne doit pas être vide" }]
}
```

## 1. Authentification et utilisateur

### POST /auth/register
```json
{ "email": "ada@example.com", "password": "motdepasse123" }
```
→ `201`
```json
{
  "accessToken": "jwt...",
  "refreshToken": "opaque...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": { "id": "uuid", "email": "ada@example.com", "role": "JOB_SEEKER", "firstName": null, "lastName": null, "headline": null, "location": null, "plan": "FREE" }
}
```

### POST /auth/login
Même corps, même réponse `200`.

### POST /auth/refresh
```json
{ "refreshToken": "opaque..." }
```
→ `200` même réponse (le refresh token est rotaté).

### POST /auth/logout
```json
{ "refreshToken": "opaque..." }
```
→ `204`

### GET /users/me
```json
{ "id": "uuid", "email": "ada@example.com", "role": "JOB_SEEKER", "firstName": "Ada", "lastName": "Lovelace", "headline": "Développeuse Java", "location": "Paris", "plan": "FREE", "createdAt": "2026-01-01T10:00:00Z" }
```

### PUT /users/me/profile
```json
{ "firstName": "Ada", "lastName": "Lovelace", "headline": "Développeuse Java", "location": "Paris" }
```
→ `200` même réponse que `GET /users/me`.

## 2. Candidatures

### ApplicationSummary
```json
{
  "id": "uuid",
  "company": "Acme",
  "position": "Développeur Java",
  "location": "Paris",
  "workMode": "HYBRID",
  "contractType": "CDI",
  "salaryMin": 45000,
  "salaryMax": 55000,
  "source": "LinkedIn",
  "offerUrl": "https://...",
  "status": "APPLIED",
  "appliedAt": "2026-01-10",
  "contactId": null,
  "contactName": null,
  "nextReminderAt": "2026-01-17T09:00:00Z",
  "interactionCount": 2,
  "createdAt": "2026-01-10T10:00:00Z",
  "updatedAt": "2026-01-12T10:00:00Z"
}
```

### ApplicationDetail
Ajoute :
```json
{
  "notes": "Notes libres",
  "history": [{ "id": "uuid", "previousStatus": null, "newStatus": "TO_APPLY", "changedAt": "..." }],
  "interactions": [{ "id": "uuid", "type": "MESSAGE", "message": "Relance envoyée", "occurredAt": "..." }],
  "reminders": [{ "id": "uuid", "title": "Relancer", "dueAt": "...", "completed": false }],
  "generatedCvId": null
}
```

### Endpoints
- `GET /applications?page=0&size=20&sort=updatedAt,desc&status=APPLIED&source=LinkedIn&search=acme&contractType=CDI&workMode=REMOTE&from=2026-01-01&to=2026-12-31`
  → `Page<ApplicationSummary>` (`content`, `totalElements`, `totalPages`, `number`, `size`, `first`, `last`).
  Champs de tri autorisés : `updatedAt`, `createdAt`, `appliedAt`, `company`, `position`, `status`.
- `POST /applications` → `201 ApplicationDetail`
- `GET /applications/{id}` → `ApplicationDetail`
- `PUT /applications/{id}` → `ApplicationDetail`
- `PATCH /applications/{id}/status` `{ "status": "INTERVIEW" }` → `ApplicationDetail`
- `DELETE /applications/{id}` → `204`
- `GET /applications/{id}/history` → `[StatusChange]`
- `GET /applications/export.csv` (mêmes filtres) → `text/csv` avec `Content-Disposition: attachment`
- `POST /applications/import` (`multipart/form-data`, champ `file`) → `{ "imported": 12, "skipped": 2, "errors": [{ "line": 4, "message": "..." }] }`

### Corps de création / mise à jour
```json
{
  "company": "Acme",
  "position": "Développeur Java",
  "location": "Paris",
  "workMode": "HYBRID",
  "contractType": "CDI",
  "salaryMin": 45000,
  "salaryMax": 55000,
  "source": "LinkedIn",
  "offerUrl": "https://...",
  "appliedAt": "2026-01-10",
  "notes": "Notes",
  "status": "TO_APPLY",
  "contactId": null
}
```
`company` et `position` obligatoires.

### Statuts
`TO_APPLY`, `CV_READY`, `APPLIED`, `FOLLOWED_UP`, `INTERVIEW`, `OFFER`, `REJECTED`, `NO_RESPONSE`, `WITHDRAWN`.
Modes de travail : `ON_SITE`, `REMOTE`, `HYBRID`.
Contrats : `INTERNSHIP`, `APPRENTICESHIP`, `FIXED_TERM`, `PERMANENT`, `FREELANCE`, `OTHER`.

### CSV
Colonnes exportées / importées :
`company,position,location,workMode,contractType,salaryMin,salaryMax,source,offerUrl,status,appliedAt,notes`
Séparateur `;` ou `,` accepté à l'import, encodage UTF-8.

## 3. Contacts

```json
{ "id": "uuid", "name": "Marie Curie", "role": "Recruteuse", "linkedinUrl": "https://...", "email": "marie@acme.fr", "createdAt": "..." }
```
- `GET /contacts` → `[Contact]`
- `POST /contacts` → `201 Contact`
- `PUT /contacts/{id}` → `Contact`
- `DELETE /contacts/{id}` → `204`

## 4. Interactions

```json
{ "id": "uuid", "applicationId": "uuid", "type": "MESSAGE", "message": "Relance", "occurredAt": "..." }
```
Types : `MESSAGE`, `FOLLOW_UP`, `INTERVIEW`, `CALL`, `EMAIL`, `NOTE`.
- `GET /applications/{id}/interactions`
- `POST /applications/{id}/interactions`
- `DELETE /applications/{id}/interactions/{interactionId}`

## 5. Rappels

```json
{ "id": "uuid", "applicationId": "uuid", "applicationCompany": "Acme", "applicationPosition": "Développeur", "title": "Relancer", "dueAt": "...", "completed": false }
```
- `GET /applications/{id}/reminders`
- `POST /applications/{id}/reminders` `{ "title": "Relancer", "dueAt": "2026-01-17T09:00:00Z" }`
- `PATCH /applications/{id}/reminders/{reminderId}/complete`
- `DELETE /applications/{id}/reminders/{reminderId}`
- `GET /reminders?from=...&to=...&completed=false` → rappels globaux triés par échéance.

## 6. Dashboard

`GET /dashboard`
```json
{
  "totalApplications": 24,
  "applied": 18,
  "responses": 7,
  "interviews": 4,
  "offers": 1,
  "rejected": 3,
  "noResponse": 8,
  "responseRate": 38.9,
  "interviewRate": 22.2,
  "offerRate": 5.6,
  "averageResponseDays": 6.4,
  "applicationsByStatus": [{ "status": "APPLIED", "count": 6 }],
  "applicationsBySource": [{ "source": "LinkedIn", "count": 10, "responses": 4, "interviews": 2, "responseRate": 40.0, "interviewRate": 20.0 }],
  "weekly": [{ "weekStart": "2026-01-05", "applications": 4, "responses": 1, "interviews": 0 }],
  "funnel": [{ "stage": "Candidatures", "count": 18 }, { "stage": "Réponses", "count": 7 }, { "stage": "Entretiens", "count": 4 }, { "stage": "Offres", "count": 1 }],
  "dueReminders": [],
  "upcomingInterviews": [{ "id": "uuid", "applicationId": "uuid", "company": "Acme", "position": "Développeur", "occurredAt": "..." }],
  "topSources": []
}
```

## 7. Profil

`GET /profile`
```json
{
  "experiences": [{ "id": "uuid", "company": "Acme", "jobTitle": "Développeur", "startedOn": "2023-01-01", "endedOn": null, "description": "..." }],
  "educations": [{ "id": "uuid", "institution": "Université", "degree": "Master", "startedOn": "2021-09-01", "endedOn": "2023-06-30", "description": "..." }],
  "projects": [{ "id": "uuid", "name": "JobTrack", "description": "...", "technologies": "Java, React" }],
  "skills": [{ "id": "uuid", "name": "Java", "level": "ADVANCED", "context": "PROFESSIONAL" }],
  "languages": [{ "id": "uuid", "name": "Français", "level": "NATIVE" }]
}
```
CRUD :
- `POST|PUT|DELETE /profile/experiences[/{id}]`
- `POST|PUT|DELETE /profile/educations[/{id}]`
- `POST|PUT|DELETE /profile/projects[/{id}]`
- `POST|PUT|DELETE /profile/skills[/{id}]`
- `POST|PUT|DELETE /profile/languages[/{id}]`

Niveaux de compétence : `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`.
Contextes : `PROFESSIONAL`, `PERSONAL`, `ACADEMIC`.

## 8. CV

### POST /cv/analyze
```json
{ "offerText": "Nous recherchons un développeur Java...", "applicationId": null }
```
→ `200`
```json
{
  "score": 72,
  "coveredSkills": ["Java", "Spring"],
  "missingSkills": ["Docker"],
  "missingInformation": ["Expérience professionnelle"],
  "alerts": ["Compétence Docker absente du profil : à confirmer."],
  "experienceGap": "Aucun écart majeur détecté.",
  "suggestions": ["Ajoutez un projet concret utilisant Docker."]
}
```

### POST /cv/generate
Même corps + `title` optionnel → `201 GeneratedCv`
```json
{
  "id": "uuid",
  "title": "CV — Acme — Développeur Java",
  "applicationId": null,
  "headline": "Développeur Java",
  "summary": "Profil réel, sans invention.",
  "skills": ["Java", "Spring"],
  "experiences": [{ "jobTitle": "Développeur", "company": "Acme", "startedOn": "2023-01-01", "endedOn": null, "description": "..." }],
  "projects": [{ "name": "JobTrack", "description": "...", "technologies": "Java" }],
  "educations": [{ "degree": "Master", "institution": "Université", "startedOn": "2021-09-01", "endedOn": "2023-06-30" }],
  "missingInformation": ["Docker"],
  "createdAt": "..."
}
```

- `GET /cv` → `[CvSummary]` (`id`, `title`, `applicationId`, `score`, `createdAt`)
- `GET /cv/{id}` → `GeneratedCv`
- `DELETE /cv/{id}` → `204`
- `GET /cv/{id}/export?format=docx|pdf|txt` → fichier (`application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `application/pdf`, `text/plain`)

Le quota est consommé une seule fois par analyse ou génération.

## 9. Entretien

`POST /interview/prepare`
```json
{ "offerText": "...", "applicationId": null }
```
→ `200`
```json
{
  "technicalQuestions": [{ "question": "...", "reason": "...", "profileEvidence": ["..."] }],
  "behavioralQuestions": [{ "question": "...", "reason": "...", "profileEvidence": [] }],
  "starPrompts": ["Situation : ...", "Tâche : ...", "Action : ...", "Résultat : ..."],
  "recruiterQuestions": ["..."],
  "missingEvidence": ["..."]
}
```

## 10. Organisations

### Organization
```json
{ "id": "uuid", "name": "École 42", "role": "ORG_ADMIN", "memberCount": 12, "createdAt": "...", "groups": [{ "id": "uuid", "name": "Promo 2026", "memberCount": 8 }] }
```
- `POST /organizations` `{ "name": "École 42" }` → `201`
- `GET /organizations` → `[Organization]`
- `GET /organizations/{id}` → `Organization`
- `GET /organizations/{id}/groups` → `[Group]`
- `POST /organizations/{id}/groups` `{ "name": "Promo 2026" }` → `201 Group`
- `POST /organizations/{id}/invitations` `{ "email": "...", "role": "MEMBER" }` → `201 { "token": "...", "expiresAt": "..." }`
- `POST /organizations/join` `{ "token": "..." }` → `200 Organization`
- `GET /organizations/{id}/members` → `[{ "userId": "uuid", "email": "...", "firstName": "...", "lastName": "...", "role": "MEMBER", "consentScope": "AGGREGATED", "joinedAt": "..." }]`
- `DELETE /organizations/{id}/members/me` → `204`
- `GET /organizations/{id}/consent` → `200 { "scope": "AGGREGATED" | null, "active": true | false }`
- `PUT /organizations/{id}/consent` `{ "scope": "AGGREGATED" }` → `200 { "scope": "AGGREGATED", "active": true }`
- `DELETE /organizations/{id}/consent` → `204`
- `GET /organizations/{id}/dashboard?groupId=uuid` → snapshot agrégé
```json
{
  "members": 12,
  "membersWithConsent": 8,
  "applications": 45,
  "interviews": 9,
  "offers": 2,
  "responseRate": 31.1,
  "interviewRate": 20.0,
  "groups": [{ "id": "uuid", "name": "Promo 2026", "members": 8, "applications": 30, "interviews": 6 }],
  "memberDetails": [{ "userId": "uuid", "displayName": "Ada L.", "applications": 12, "interviews": 3, "consentScope": "APPLICATION_DETAILS" }]
}
```
- `GET /organizations/{id}/members/{userId}/applications` → accessible uniquement si consentement `APPLICATION_DETAILS` actif et rôle `ORG_ADMIN` ou `ORG_COACH`.

Scopes : `AGGREGATED`, `APPLICATION_DETAILS`.
Rôles organisation : `ORG_ADMIN`, `ORG_COACH`, `MEMBER`.

## 11. Plan et quota

`GET /plan` → `{ "plan": "FREE", "used": 2, "limit": 10, "resetAt": "2026-02-01", "features": ["10 analyses CV / mois"] }`

## 12. RGPD

- `GET /privacy/export` → export JSON complet (profil, candidatures, contacts, interactions, rappels, CV, organisations, consentements).
- `DELETE /privacy/account` `{ "confirm": true }` → `204`, suppression en cascade.

## 13. Santé

- `GET /health` → `{ "status": "UP", "service": "JobTrack API", "timestamp": "..." }` (public)

## 14. Administration plateforme

Tous les endpoints `/admin/**` exigent un JWT dont l'utilisateur porte le rôle
`PLATFORM_ADMIN` ; sinon `403` (RFC 7807). Les listes sont paginées
(`page`, `size` ≤ 100, tri `createdAt` décroissant) et renvoient le format Spring
`{ content, totalElements, totalPages, number, size }`.

- `GET /admin/overview` → indicateurs globaux
```json
{
  "totalUsers": 42,
  "platformAdmins": 2,
  "newUsersLast30Days": 7,
  "totalOrganizations": 3,
  "totalMemberships": 15,
  "totalApplications": 120,
  "usersByPlan": [
    { "plan": "FREE", "users": 30 },
    { "plan": "PRO", "users": 10 },
    { "plan": "ORGANIZATION", "users": 2 }
  ]
}
```
- `GET /admin/users?query=&plan=&role=&page=&size=` → `Page<AdminUser>`
  (`query` filtre sur l'e-mail, insensible à la casse).
- `POST /admin/users` `{ "email": "...", "password": "...", "role": "JOB_SEEKER" | "PLATFORM_ADMIN" }`
  → `201 AdminUser` (profil vide + plan `FREE` initialisés ; `409` si l'e-mail
  existe, `400` si le mot de passe fait moins de 12 caractères).
- `GET /admin/users/{id}` → détail :
```json
{
  "id": "uuid", "email": "ada@example.com", "role": "JOB_SEEKER", "plan": "FREE",
  "firstName": "Ada", "lastName": "Lovelace", "headline": "Développeuse Java",
  "location": "Paris", "cvAnalysisUsed": 2, "cvAnalysisLimit": 10,
  "cvAnalysisResetAt": "2026-12-01", "applicationCount": 12,
  "organizations": ["École 42"], "createdAt": "..."
}
```
- `PATCH /admin/users/{id}/plan` `{ "plan": "FREE" | "PRO" | "ORGANIZATION" }` → `AdminUser`
- `PATCH /admin/users/{id}/role` `{ "role": "JOB_SEEKER" | "PLATFORM_ADMIN" }` → `AdminUser`
  - `400` si l'administrateur courant tente de retirer son propre rôle ;
  - `409` si l'opération supprimerait le dernier `PLATFORM_ADMIN`.
- `GET /admin/organizations?query=&page=&size=` → `Page<{ id, name, members, createdAt }>`

Bootstrap : les adresses de `JOBTRACK_ADMIN_EMAILS` (liste séparée par des
virgules, insensible à la casse) deviennent `PLATFORM_ADMIN` à l'inscription ;
`AdminBootstrap` promeut aussi les comptes existants au démarrage.
