import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import { useI18n } from "../lib/i18n";
import { Button } from "../components/ui/Button";
import { Card } from "../components/ui/Card";
import { EmptyState } from "../components/ui/EmptyState";

export function NotFoundPage() {
  const { t } = useI18n();

  return (
    <Card className="p-6">
      <EmptyState
        icon={<Compass className="h-5 w-5" aria-hidden="true" />}
        title={t("errors.notFound")}
        description={t("dashboard.empty.description")}
        action={
          <Link to="/dashboard">
            <Button variant="outline">{t("nav.dashboard")}</Button>
          </Link>
        }
      />
    </Card>
  );
}
