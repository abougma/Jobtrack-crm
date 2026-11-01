# Architecture — JobTrack

## Style
Architecture hexagonale légère, en 4 couches :

```
api -> application -> domain
              ^
              |
        infrastructure (adaptateurs)
```

Règle de dépendance : `domain` ne dépend de rien. `application` dépend de `domain` et de ses ports. `infrastructure` implémente les ports. `api` dépend de `application` et expose des DTO, jamais des entités JPA.

## Packages backend
```
com.jobtrack
├── domain/            # modèles et règles métier purs (aucune annotation Spring/JPA)
├── application/
│   ├── port/          # interfaces de sortie (repositories, assistants, exporters)
│   └── service/       # cas d'usage transactionnels
├── infrastructure/
│   ├── persistence/   # entités JPA + Spring Data + adaptateurs
│   ├── security/      # JWT, filtre, configuration Spring Security
│   └── export/        # DOCX / PDF / TXT
└── api/               # contrôleurs REST, DTO, gestion d'erreurs RFC 7807
```

## Frontend
Découpage par feature :
```
src/app        # providers, router, layout
src/features   # auth, dashboard, applications, profile, cv, interview, organizations, settings
src/components # UI réutilisable, charts, layout
src/lib        # client API, auth, formatage, i18n
```

## Multi-tenant et propriété des données
- Toute donnée personnelle appartient à un `user_id`.
- Un utilisateur peut appartenir à 0..N organisations via `organization_memberships`.
- Les données d'organisation sont filtrées par `organization_id` et par le rôle de l'appelant.
- Les contrôleurs ne filtrent jamais à la main : les services applicatifs vérifient systématiquement l'appartenance (protection IDOR).
- Le consentement organisation est explicite, révocable, avec deux scopes : `AGGREGATED` et `APPLICATION_DETAILS`.

## Rôles
- `JOB_SEEKER` : rôle global par défaut.
- `ORG_ADMIN` : administration d'une organisation (membre avec droits étendus).
- `ORG_COACH` : accompagnateur, accès agrégé + détail consenti.
- `MEMBER` : membre simple d'une organisation.
- `PLATFORM_ADMIN` : administration plateforme.

## API
- Base `/api/v1`, erreurs RFC 7807 (`application/problem+json`).
- Pagination Spring (`page`, `size`, `sort`), tri whitelisté.
- Validation stricte des entrées, tailles maximales.
- OpenAPI sur `/api/v1/docs` et Swagger UI sur `/swagger-ui.html`.

## Sécurité
- BCrypt pour les mots de passe (12 caractères minimum).
- Access token JWT court, refresh token opaque haché SHA-256, rotation à chaque usage.
- CORS restreint à `CORS_ORIGIN`.
- Vérification d'appartenance sur chaque ressource.
- Aucune donnée sensible dans les logs.

## CV et IA
- Port `CvAssistant` avec implémentation déterministe `MockCvAssistant`.
- Toute information générée référence un élément réel du profil.
- Les informations absentes sont listées dans `missingInformation`, jamais inventées.
- Quota consommé une seule fois par analyse ou génération.
