# JobTrack

JobTrack est un CRM de recherche d'emploi : suivi des candidatures, tableau de
bord, profil professionnel, analyse et génération de CV, préparation
d'entretien, espaces organisation et conformité RGPD.

- Backend : Java 21, Spring Boot 3.4, PostgreSQL 17, Flyway, architecture
  hexagonale légère (`domain → application → infrastructure → api`).
- Frontend : React 18, TypeScript, Vite, TanStack Query, React Hook Form + Zod,
  Tailwind CSS, Recharts, dnd-kit. Interface en français avec bascule anglais.
- Contrat d'API : [`docs/api-contract.md`](docs/api-contract.md).
- Architecture et décisions : [`docs/architecture.md`](docs/architecture.md).
- Contexte produit et règles : [`context/`](context/).

## Démarrage rapide (Docker)

```bash
cp .env.example .env
docker compose up --build
```

| Service | URL |
| --- | --- |
| Interface | http://localhost:5173 |
| API | http://localhost:18080/api/v1/health |
| Swagger UI | http://localhost:18080/swagger-ui.html |
| Actuator | http://localhost:18080/actuator/health |
| PostgreSQL | `localhost:15432` |

Si le port `5173` est déjà utilisé par un serveur de développement local :

```bash
WEB_PORT=5174 docker compose up --build
```

Le frontend est servi par nginx, qui proxifie `/api/` vers l'API et renvoie
`index.html` pour les routes SPA. Le mot de passe de `.env.example` est réservé
au développement local.

## Développement local

Prérequis : JDK 21, Node.js 22, Docker (pour PostgreSQL).

```bash
# Base de données
docker compose up -d db

# API (http://localhost:18080)
cd backend
mvn spring-boot:run

# Frontend (http://localhost:5173, proxy /api → localhost:18080)
cd frontend
npm ci
npm run dev
```

### Commandes utiles

| Backend (`backend/`) | Frontend (`frontend/`) |
| --- | --- |
| `mvn verify` — compile + tests (unitaires et intégration Testcontainers) | `npm run typecheck` |
| `mvn -DskipTests package` | `npm run lint` |
| `mvn spring-boot:run` | `npm run test -- --run` |
| | `npm run build` |

Les tests d'intégration démarrent un PostgreSQL 17 jetable via Testcontainers ;
Docker doit donc être disponible.

## Parcours fonctionnels

| Écran | Route | API principale |
| --- | --- | --- |
| Connexion / inscription | `/login`, `/register` | `POST /auth/register`, `POST /auth/login` |
| Tableau de bord | `/` | `GET /dashboard`, `GET /plan` |
| Candidatures | `/applications`, `/applications/:id` | `/applications`, interactions, rappels, historique, export/import CSV |
| Profil | `/profile` | `GET/PUT /users/me/profile`, `/profile/*` |
| CV | `/cv`, `/cv/:id` | `POST /cv/analyze`, `POST /cv/generate`, exports DOCX/PDF/TXT |
| Entretien | `/interview` | `POST /interview/prepare` |
| Organisation | `/organizations` | `/organizations`, groupes, membres, consentement, invitations |
| Paramètres | `/settings` | `GET /plan`, `GET /privacy/export`, `DELETE /privacy/account` |
| Administration plateforme | `/admin` | `/admin/overview`, `/admin/users`, `/admin/organizations` |

Les statuts de candidature sont : `TO_APPLY`, `CV_READY`, `APPLIED`,
`FOLLOWED_UP`, `INTERVIEW`, `OFFER`, `REJECTED`, `NO_RESPONSE`, `WITHDRAWN`.

## Authentification

`POST /api/v1/auth/register`, `/auth/login`, `/auth/refresh` et `/auth/logout`.
Les routes protégées attendent `Authorization: Bearer <access-token>`. Un token
dont l'utilisateur a été supprimé est rejeté (`401`). En production, remplacez
`JWT_SECRET` par une valeur aléatoire d'au moins 32 octets.

## Assistants CV et entretien

Les assistants sont **déterministes et locaux** (aucun appel LLM, aucune donnée
envoyée à un tiers). Ils ne sélectionnent et ne reformulent que des éléments
présents dans le profil :

- compétences couvertes vs manquantes par rapport à l'offre ;
- informations de profil à compléter ;
- génération de CV à partir des expériences, projets, formations et compétences
  réels ;
- questions d'entretien techniques et comportementales, trame STAR, questions
  à poser au recruteur et preuves manquantes.

Aucune expérience ni compétence n'est inventée : les éléments absents sont
signalés dans `missingSkills` / `missingInformation`.

## Organisations et consentement

```text
POST   /api/v1/organizations
GET    /api/v1/organizations
GET    /api/v1/organizations/{id}
POST   /api/v1/organizations/{id}/groups
POST   /api/v1/organizations/{id}/invitations
POST   /api/v1/organizations/join
GET    /api/v1/organizations/{id}/members
DELETE /api/v1/organizations/{id}/members/me
GET    /api/v1/organizations/{id}/consent
PUT    /api/v1/organizations/{id}/consent
DELETE /api/v1/organizations/{id}/consent
GET    /api/v1/organizations/{id}/dashboard
```

Scopes de consentement : `AGGREGATED` (indicateurs agrégés) et
`APPLICATION_DETAILS` (détail par membre). Le détail d'un membre n'est visible
que si son consentement est actif et couvre ce scope. Une invitation est liée à
l'adresse e-mail invitée.

## Plans, quotas et RGPD

Chaque utilisateur démarre en `FREE` avec 10 analyses CV par mois ; les plans
`PRO` et `ORGANIZATION` sont prévus. Le quota courant est exposé par
`GET /api/v1/plan`.

```text
GET    /api/v1/privacy/export
DELETE /api/v1/privacy/account   # { "confirm": true }
```

L'export produit un JSON des données personnelles. La suppression est
définitive et s'appuie sur les suppressions en cascade PostgreSQL.

## Administration plateforme

Le rôle `PLATFORM_ADMIN` donne accès à la console `/admin` et aux endpoints
`/api/v1/admin/**` (tout autre rôle reçoit `403`). La console affiche les
indicateurs globaux (comptes, plans, organisations, adhésions, candidatures),
permet de rechercher un compte, de consulter son détail, d'ajuster son plan ou
son rôle, et de créer un compte candidat ou administrateur.

Le premier administrateur est désigné par configuration, jamais codé en dur :

```bash
# .env ou environnement du conteneur api
JOBTRACK_ADMIN_EMAILS=admin@example.com,second@example.com
```

- à l'inscription, une adresse listée devient immédiatement `PLATFORM_ADMIN` ;
- au démarrage, `AdminBootstrap` promeut les comptes existants listés
  (opération idempotente) ;
- la variable est une liste séparée par des virgules, insensible à la casse.

Garde-fous : un administrateur ne peut pas retirer son propre rôle (`400`) et le
dernier `PLATFORM_ADMIN` ne peut pas être rétrogradé (`409`). Les rôles
plateforme sont limités à `JOB_SEEKER` et `PLATFORM_ADMIN` ; les rôles
d'organisation restent portés par `OrganizationMembership`.

```text
GET   /api/v1/admin/overview
GET   /api/v1/admin/revenue
GET   /api/v1/admin/users?query=&plan=&role=&page=&size=
POST  /api/v1/admin/users             # { "email", "password", "role" } → 201
GET   /api/v1/admin/users/{id}
PATCH /api/v1/admin/users/{id}/plan   # { "plan": "FREE" | "PRO" | "ORGANIZATION" }
PATCH /api/v1/admin/users/{id}/role   # { "role": "JOB_SEEKER" | "PLATFORM_ADMIN" }
GET   /api/v1/admin/organizations?query=&page=&size=
POST  /api/v1/plan/checkout           # { "plan": "PRO" | "PREMIUM" } → Stripe Checkout URL
POST  /api/v1/payments/stripe/webhook # Stripe checkout.session.completed
```

## Variables d'environnement

| Variable | Défaut | Rôle |
| --- | --- | --- |
| `POSTGRES_DB` | `jobtrack` | Nom de la base |
| `POSTGRES_USER` | `jobtrack` | Utilisateur PostgreSQL |
| `POSTGRES_PASSWORD` | `jobtrack-dev-only` | Mot de passe PostgreSQL |
| `POSTGRES_PORT` | `15432` | Port PostgreSQL exposé |
| `API_PORT` | `18080` | Port de l'API exposé |
| `WEB_PORT` | `5173` | Port du frontend exposé |
| `JWT_SECRET` | valeur de dev | Clé de signature JWT (≥ 32 caractères) |
| `CORS_ORIGIN` | `http://localhost:5173` | Origines autorisées (séparées par des virgules) |
| `VITE_API_URL` | `/api/v1` | Base URL de l'API côté frontend |
| `JOBTRACK_ADMIN_EMAILS` | vide | E-mails promus `PLATFORM_ADMIN` (liste séparée par des virgules) |
| `STRIPE_SECRET_KEY` | vide | Clé secrète Stripe (`sk_test_...` en développement) |
| `STRIPE_WEBHOOK_SECRET` | vide | Secret de signature du webhook Stripe (`whsec_...`) |
| `MAIL_ENABLED` | `false` | Active les notifications e-mail |
| `MAIL_HOST` | vide | Hôte SMTP, par exemple `sandbox.smtp.mailtrap.io` |
| `MAIL_PORT` | `2525` | Port SMTP |
| `MAIL_USERNAME` | vide | Identifiant SMTP Mailtrap |
| `MAIL_PASSWORD` | vide | Mot de passe SMTP Mailtrap |
| `MAIL_FROM` | `noreply@jobtrack.local` | Adresse d'expédition |
| `MAIL_SMTP_AUTH` | `true` | Active l'authentification SMTP |
| `MAIL_SMTP_STARTTLS` | `true` | Active STARTTLS |

## Structure du dépôt

```text
backend/    API Spring Boot (domain, application, infrastructure, api)
frontend/   Application React (app, features, components, lib)
context/    Source de vérité produit, stack, architecture, règles de code
docs/       Contrat d'API et architecture
```

## Limites connues

- Les assistants CV/entretien sont des heuristiques locales, pas un LLM.
- Les upgrades `PRO` et `PREMIUM` passent par Stripe Checkout et ne sont activés
  qu'après réception du webhook signé `checkout.session.completed`.
- La console `PLATFORM_ADMIN` peut attribuer manuellement un plan à un compte et
  consulter les paiements confirmés, les paiements en attente et le chiffre
  d'affaires encaissé. Cette attribution manuelle est prévue pour le support et
  ne constitue pas une preuve de paiement.
- Les e-mails transactionnels sont désactivés par défaut. Lorsque `MAIL_ENABLED=true`
  et les paramètres SMTP sont renseignés, un e-mail est envoyé à la création du
  compte et après confirmation du paiement d'un abonnement par le webhook Stripe.
  Les erreurs SMTP sont journalisées sans annuler l'inscription ou le paiement.
