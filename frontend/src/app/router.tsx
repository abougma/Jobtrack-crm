import { createBrowserRouter } from "react-router-dom";
import { AppShell } from "../components/layout/AppShell";
import { ProtectedRoute, PublicOnlyRoute, RequireRole } from "../components/layout/ProtectedRoute";
import { LoginPage } from "../features/auth/LoginPage";
import { RegisterPage } from "../features/auth/RegisterPage";
import { DashboardPage } from "../features/dashboard/DashboardPage";
import { ApplicationsPage } from "../features/applications/ApplicationsPage";
import { ApplicationDetailPage } from "../features/applications/ApplicationDetailPage";
import { ProfilePage } from "../features/profile/ProfilePage";
import { CvPage } from "../features/cv/CvPage";
import { CvDetailPage } from "../features/cv/CvDetailPage";
import { InterviewPage } from "../features/interview/InterviewPage";
import { OrganizationsPage } from "../features/organizations/OrganizationsPage";
import { SettingsPage } from "../features/settings/SettingsPage";
import { NotFoundPage } from "./NotFoundPage";
import { LandingPage } from "../features/landing/LandingPage";
import { AdminPage } from "../features/admin/AdminPage";

export const router = createBrowserRouter([
  { path: "/", element: <LandingPage /> },
  {
    element: <PublicOnlyRoute />,
    children: [
      { path: "/login", element: <LoginPage /> },
      { path: "/register", element: <RegisterPage /> },
    ],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: "dashboard", element: <DashboardPage /> },
          { path: "applications", element: <ApplicationsPage /> },
          { path: "applications/:id", element: <ApplicationDetailPage /> },
          { path: "profile", element: <ProfilePage /> },
          { path: "cv", element: <CvPage /> },
          { path: "cv/:id", element: <CvDetailPage /> },
          { path: "interview", element: <InterviewPage /> },
          { path: "organizations", element: <OrganizationsPage /> },
          { path: "settings", element: <SettingsPage /> },
          {
            element: <RequireRole role="PLATFORM_ADMIN" />,
            children: [{ path: "admin", element: <AdminPage /> }],
          },
          { path: "*", element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
