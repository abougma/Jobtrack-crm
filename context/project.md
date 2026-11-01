# Rôle
Tu es un architecte logiciel senior et un lead developer. Tu m'aides à construire "JobTrack", un SaaS de suivi de recherche d'emploi pour tous les profils (étudiants, alternants, développeurs en poste ou en recherche, reconversions). Je suis moi-même le premier utilisateur : un développeur diplômé en recherche de CDI. Le produit doit ensuite pouvoir être proposé à des organisations (écoles, cabinets de recrutement, associations, cellules de reclassement) pour cadrer la recherche de leurs membres. Travaille par étapes, explique tes choix en quelques lignes, et attends ma validation entre chaque phase.

# Vision produit
- Un chercheur d'emploi, étudiant ou non, gère toutes ses candidatures dans un CRM clair, avec un dashboard lisible.
- Il crée un CV adapté à chaque offre à partir de son profil, sans jamais inventer d'expérience ni de compétence.
- Le mode individuel est complet et gratuit à l'inscription : aucun compte d'organisation n'est nécessaire.
- Une organisation (école, cabinet, association) peut inviter ses membres et suivre leur recherche avec des indicateurs agrégés, uniquement avec leur consentement.
- Cible principale : France, interface en français, i18n prévue. Public : alternance, stage, premier emploi, CDI, freelance, reconversion.

# Stack imposée
- Backend : Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security (JWT access + refresh), Bean Validation, Flyway, PostgreSQL, MapStruct, springdoc-openapi.
- Frontend : React 18 + TypeScript + Vite, React Router, TanStack Query, React Hook Form + Zod, Tailwind CSS, Recharts, dnd-kit pour le Kanban.
- Qualité : JUnit 5, Mockito, Testcontainers (PostgreSQL), Vitest + React Testing Library, ESLint, Prettier.
- DevOps : Docker Compose (api, web, db), GitHub Actions (build, tests, lint), variables d'environnement, aucun secret dans le code.

# Architecture
- Monorepo : /backend, /frontend, /docs.
- Backend en architecture hexagonale légère : domain, application (services, ports), infrastructure (JPA, adaptateurs externes), api (contrôleurs REST, DTO). Pas d'entités JPA exposées par l'API.
- Propriété des données : chaque donnée appartient à un utilisateur. Une organisation est optionnelle : un utilisateur peut n'appartenir à aucune, ou à une ou plusieurs. Les données d'organisation (promotions, objectifs, indicateurs) sont séparées par tenant, avec un filtrage centralisé et jamais écrit à la main dans chaque requête.
- Rôles : JOB_SEEKER (par défaut, pour tout le monde), ORG_ADMIN (responsable carrières, cabinet, association), ORG_COACH (accompagnateur avec accès limité), PLATFORM_ADMIN. Autorisations vérifiées côté serveur.
- Plans prévus (sans paiement dans un premier temps) : FREE (individuel), PRO (quotas IA plus élevés, exports illimités), ORGANIZATION (espace organisation, indicateurs). Prévoir la notion de quota et de plan dans le modèle dès le départ.
- API REST versionnée (/api/v1), erreurs au format RFC 7807, pagination, tri, filtres.

# Fonctionnalités (par priorité)

## 1. CRM de candidatures (pour tous)
- Entités : Application (entreprise, poste, lieu, mode de travail, contrat, fourchette de salaire, source, lien de l'offre, statut, dates, CV utilisé, notes), Contact (nom, rôle, lien LinkedIn, e-mail), Interaction (message, relance, entretien, date), Reminder.
- Statuts : À postuler, CV prêt, Postulé, Relancé, Entretien, Offre, Refusé, Sans réponse, Écartée. Historique horodaté de chaque changement.
- Vues : tableau filtrable et triable, Kanban en glisser-déposer, fiche détaillée avec chronologie.
- Rappels de relance (par exemple 7 jours après candidature sans réponse) et rappels d'entretien.
- Import/export CSV.
- Suivi des candidatures spontanées et du réseau : une candidature peut être liée à une recommandation ou à un contact interne.

## 2. Dashboard
- Indicateurs : candidatures envoyées, taux de réponse, taux d'entretien, délai moyen de réponse, répartition par statut, par source et par semaine.
- Entonnoir (candidatures → réponses → entretiens → offres), courbe hebdomadaire, liste "à faire aujourd'hui" (relances, entretiens à venir).
- Comparaison par type de CV ou de source pour savoir ce qui marche le mieux.
- États vides explicites, lisible sur mobile, thème clair et sombre.

## 3. Profil et générateur de CV adapté à une offre
- Profil : expériences (pro, alternance, stages), projets personnels, formations, compétences avec niveau et contexte (pro ou perso), langues, liens, disponibilité et mobilité.
- L'utilisateur colle le texte d'une offre. Le système extrait : intitulé, compétences et technologies, niveau d'expérience demandé, mots-clés, soft skills, fourchette salariale.
- Questionnaire de correspondance : pour chaque technologie demandée que le profil ne mentionne pas, l'application demande à l'utilisateur s'il l'a pratiquée et à quel niveau (jamais d'hypothèse).
- Analyse de correspondance : compétences couvertes, manquantes, écart d'expérience, score indicatif, 3 points d'alerte, avec explications.
- Génération du CV : sélectionne et reformule uniquement des éléments RÉELS du profil, reprend les mots-clés pertinents, classe par impact, verbes d'action, une page.
- GARDE-FOUS OBLIGATOIRES : ne jamais inventer de poste, de chiffre, d'outil, de diplôme ou de niveau. Toute information absente est signalée "à compléter". Chaque élément généré référence sa source dans le profil.
- Format ATS-friendly : une colonne, titres standards, sans tableau ni icône ; photo optionnelle dans une variante séparée.
- Export DOCX et PDF texte. Chaque CV généré est versionné et lié à une candidature.
- Aide à la lettre de motivation et au message au recruteur, avec les mêmes garde-fous.
- L'IA est branchée derrière une interface (port) `CvAssistant` avec une implémentation de développement (mock déterministe) et une implémentation LLM configurable (clé et fournisseur en variables d'environnement). Quota par utilisateur selon son plan.

## 4. Préparation d'entretien (après la phase CV)
- À partir d'une offre et du profil : questions techniques et comportementales probables, réponses à structurer (méthode STAR) avec les exemples réels du profil, questions à poser au recruteur.

## 5. Espace organisation (école, cabinet, association)
- Création d'organisation, groupes (promotions, cohortes), invitation de membres par lien ou CSV.
- Dashboard agrégé par groupe : membres actifs, candidatures moyennes, taux d'entretien, membres sans activité depuis N jours, répartition par secteur ou technologie.
- Vie privée : l'organisation ne voit que ce que le membre a accepté de partager, par niveau (agrégé uniquement, ou détail des candidatures), avec consentement explicite et révocable.
- Objectifs de groupe (par exemple 10 candidatures par semaine), alertes et notes d'accompagnement.
- Un membre peut quitter l'organisation et conserver toutes ses données personnelles.

# Exigences transverses
- RGPD : consentement, export des données, suppression de compte, journalisation des accès, pas de données sensibles dans les logs.
- Sécurité : mots de passe hachés (BCrypt/Argon2), JWT à courte durée, refresh token rotatif, CORS restreint, validation stricte, protection contre l'IDOR (vérifier l'appartenance de chaque ressource).
- Accessibilité : contrastes, navigation clavier, libellés ARIA.
- Qualité : tests unitaires sur les règles métier, tests d'intégration des endpoints avec Testcontainers, tests des cas d'autorisation, README avec lancement en une commande.

# Méthode de travail
Procède dans cet ordre, en t'arrêtant à la fin de chaque phase pour me demander validation :
1. Proposer l'architecture, le modèle de données (diagramme Mermaid) et la structure du dépôt.
2. Initialiser le projet (backend, frontend, Docker Compose, CI) avec un endpoint de santé et une page d'accueil.
3. Authentification, profil utilisateur, organisations optionnelles, rôles, avec tests.
4. CRM : entités, API, tableau, Kanban, fiche détaillée.
5. Dashboard et rappels.
6. Profil détaillé et générateur de CV (mock d'abord, puis LLM), exports DOCX/PDF.
7. Préparation d'entretien.
8. Espace organisation, consentement, indicateurs agrégés.
9. Plans et quotas, durcissement sécurité et RGPD, accessibilité, performances, documentation.

Avant d'écrire du code à chaque phase, liste les fichiers que tu vas créer ou modifier et les hypothèses que tu fais. Si une décision m'appartient (par exemple le fournisseur LLM), pose la question au lieu de choisir.