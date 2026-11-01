# JobTrack — Architecture technique

## 1. Vue d'ensemble

```mermaid
flowchart LR
  U[Utilisateur] -->|HTTPS| W[Web React + Vite]
  W -->|REST /api/v1| A[API Spring Boot]
  A -->|JPA / Flyway| D[(PostgreSQL)]
  A -.->|port CvAssistant| M[Mock déterministe]
  A -.->|port CvAssistant| L[LLM configurable - phase ultérieure]
  A -->|DOCX / PDF| F[Exports CV]
```

## 2. Architecture backend (hexagonale légère)

```mermaid
flowchart TB
  subgraph API
    C[Contrôleurs REST]
    E[Gestion d'erreurs RFC 7807]
    DTO[DTO Request/Response]
  end
  subgraph Application
    S[Services / cas d'usage]
    P[Ports de sortie]
  end
  subgraph Domain
    DM[Modèles et règles métier]
  end
  subgraph Infrastructure
    JPA[Entités JPA + Spring Data]
    AD[Adaptateurs de repositories]
    SEC[JWT + Spring Security]
    EXP[Export DOCX/PDF/TXT]
  end
  C --> S
  S --> DM
  S --> P
  AD -.implémente.-> P
  AD --> JPA
  JPA --> DB[(PostgreSQL)]
  S --> EXP
  SEC --> C
```

**Règle de dépendance** : `api → application → domain`, `infrastructure → application/domain`. Le domaine ne connaît ni Spring ni JPA.

## 3. Modèle de données

```mermaid
erDiagram
  USERS ||--|| USER_PROFILES : possède
  USERS ||--|| USER_PLANS : possède
  USERS ||--o{ REFRESH_TOKENS : possède
  USERS ||--o{ APPLICATIONS : possède
  USERS ||--o{ CONTACTS : possède
  USERS ||--o{ PROFILE_EXPERIENCES : possède
  USERS ||--o{ PROFILE_EDUCATIONS : possède
  USERS ||--o{ PROFILE_PROJECTS : possède
  USERS ||--o{ PROFILE_SKILLS : possède
  USERS ||--o{ PROFILE_LANGUAGES : possède
  USERS ||--o{ GENERATED_CVS : possède
  USERS ||--o{ ORGANIZATION_MEMBERSHIPS : adhère
  ORGANIZATIONS ||--o{ ORGANIZATION_MEMBERSHIPS : contient
  ORGANIZATIONS ||--o{ ORGANIZATION_GROUPS : contient
  ORGANIZATIONS ||--o{ ORGANIZATION_CONSENTS : reçoit
  ORGANIZATIONS ||--o{ ORGANIZATION_INVITATIONS : émet
  ORGANIZATION_GROUPS ||--o{ ORGANIZATION_GROUP_MEMBERS : contient
  APPLICATIONS ||--o{ APPLICATION_STATUS_HISTORY : historise
  APPLICATIONS ||--o{ INTERACTIONS : contient
  APPLICATIONS ||--o{ REMINDERS : contient
  APPLICATIONS }o--|| CONTACTS : référence
  APPLICATIONS ||--o{ GENERATED_CVS : utilise
```

### Entités principales
- `applications` : entreprise, poste, lieu, mode de travail, contrat, salaire, source, URL, statut, dates, notes, contact, CV généré.
- `application_status_history` : historique horodaté de chaque changement de statut.
- `contacts` : nom, rôle, LinkedIn, e-mail.
- `interactions` : message, relance, entretien, appel, e-mail, note.
- `reminders` : titre, échéance, complété.
- `generated_cvs` : contenu JSONB, offre source, candidature liée, score.
- `organizations`, `organization_memberships`, `organization_groups`, `organization_group_members`, `organization_consents`, `organization_invitations`.
- `user_plans` : plan, quota, date de réinitialisation.

## 4. Flux d'authentification

```mermaid
sequenceDiagram
  participant W as Web
  participant A as API
  participant DB as PostgreSQL
  W->>A: POST /auth/login
  A->>DB: vérifie email + BCrypt
  A->>DB: crée refresh token (hash SHA-256)
  A-->>W: accessToken (15 min) + refreshToken (30 j)
  W->>A: requête avec Bearer
  A->>A: filtre JWT -> userId + rôle
  W->>A: POST /auth/refresh
  A->>DB: vérifie + révoque l'ancien refresh
  A-->>W: nouveaux tokens
```

## 5. Flux CV avec garde-fous

```mermaid
flowchart LR
  O[Texte de l'offre] --> X[Extraction mots-clés]
  P[Profil réel] --> C[Comparaison]
  X --> C
  C --> R[Score + couvertes + manquantes]
  C --> Q[Questions de correspondance]
  Q --> G[Génération]
  P --> G
  G --> V{Élément réel ?}
  V -->|oui| CV[CV ATS]
  V -->|non| M[missingInformation]
```

- Aucune expérience, compétence, date ou diplôme inventé.
- Chaque élément généré est issu du profil.
- Le mock est déterministe ; l'implémentation LLM est derrière le même port.

## 6. Espace organisation et consentement

```mermaid
stateDiagram-v2
  [*] --> Invité
  Invité --> Membre: accepte l'invitation
  Membre --> ConsentementAgrégé: PUT /consent AGGREGATED
  ConsentementAgrégé --> ConsentementDétail: PUT /consent APPLICATION_DETAILS
  ConsentementDétail --> ConsentementAgrégé: PUT /consent AGGREGATED
  ConsentementAgrégé --> Membre: DELETE /consent
  Membre --> [*]: quitte l'organisation
```

- L'organisation ne voit que les agrégats sans consentement.
- Le détail des candidatures exige `APPLICATION_DETAILS`.
- Le membre peut révoquer ou quitter l'organisation et conserver ses données.

## 7. Découpage frontend

```mermaid
flowchart TB
  APP[App / Router] --> AUTH[AuthProvider]
  APP --> Q[QueryClientProvider]
  APP --> I18N[I18nProvider]
  APP --> THEME[ThemeProvider]
  AUTH --> SHELL[AppShell]
  SHELL --> DASH[Dashboard]
  SHELL --> APPS[Candidatures + Kanban]
  SHELL --> PROF[Profil]
  SHELL --> CV[Assistant CV]
  SHELL --> INT[Entretien]
  SHELL --> ORG[Organisation]
  SHELL --> SET[Réglages]
  SHELL --> ADMIN[Administration plateforme]
```

Chaque feature contient : `api.ts` (appels typés), `hooks.ts` (TanStack Query), `types.ts`, pages et composants.

## 8. Administration plateforme

```mermaid
flowchart LR
  UI[AdminPage /admin] -->|RequireRole PLATFORM_ADMIN| API[PlatformAdminController]
  API -->|hasRole PLATFORM_ADMIN| SVC[PlatformAdminService]
  SVC --> U[UserRepository]
  SVC --> P[UserPlanRepository]
  SVC --> O[OrganizationRepository]
  SVC --> M[MembershipRepository]
  SVC --> A[ApplicationRepository]
  BOOT[AdminBootstrap] -->|JOBTRACK_ADMIN_EMAILS| U
  REG[AuthService.register] -->|allowlist| U
```

- Le contrôle d'accès est double : `SecurityConfig` protège `/api/v1/admin/**`
  et `RequireRole` protège la route frontend `/admin` (redirection sinon).
- `AdminAllowlist` est un port d'application implémenté par configuration
  (`ConfiguredAdminAllowlist`), sans e-mail codé en dur.
- Aucune entité JPA n'est exposée : le service agrège et renvoie des vues.
- La création de compte depuis la console réutilise `PasswordHasher` et initialise
  profil vide + plan `FREE`, comme `AuthService.register`.
- Garde-fous métier dans le service : auto-rétrogradation interdite (`400`),
  dernier administrateur protégé (`409`).

## 9. Décisions et limites
- LLM : non branché dans cette itération. Le port `CvAssistant` permet d'ajouter un fournisseur via variables d'environnement sans changer le domaine. Le fournisseur reste une décision produit.
- Paiement : hors scope. Les plans existent dans le modèle, sans facturation.
- Notifications : rappels visibles dans l'application ; pas d'envoi e-mail.
- Stockage des tokens : `localStorage` pour le MVP ; à remplacer par cookies httpOnly + CSRF en production.
