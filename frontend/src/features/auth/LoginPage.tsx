import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { z } from "zod";
import { ApiError } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { useToast } from "../../components/ui/toast-context";
import { AuthLayout } from "./AuthLayout";
import { useAuth } from "./AuthProvider";

export function LoginPage() {
  const { t } = useI18n();
  const { login } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const schema = useMemo(
    () =>
      z.object({
        email: z
          .string()
          .min(1, { message: t("validation.required") })
          .email({ message: t("validation.email") }),
        password: z.string().min(12, { message: t("validation.passwordMin") }),
      }),
    [t],
  );

  type FormValues = z.infer<typeof schema>;

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", password: "" },
  });

  const from = (location.state as { from?: string } | null)?.from ?? "/";

  const onSubmit = handleSubmit(async (values) => {
    try {
      await login(values);
      push(t("auth.loginSuccess"), "success");
      navigate(from, { replace: true });
    } catch (error) {
      const message = error instanceof ApiError ? error.message : t("auth.error");
      setError("root", { message });
      push(message, "error");
    }
  });

  return (
    <AuthLayout
      title={t("auth.loginTitle")}
      subtitle={t("auth.loginSubtitle")}
      footer={
        <span>
          {t("auth.noAccount")}{" "}
          <Link to="/register" className="font-semibold text-primary-600 hover:underline dark:text-primary-300">
            {t("auth.createAccount")}
          </Link>
        </span>
      }
    >
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        <Input
          label={t("auth.email")}
          type="email"
          autoComplete="email"
          required
          placeholder="ada@example.com"
          error={errors.email?.message}
          {...register("email")}
        />
        <Input
          label={t("auth.password")}
          type="password"
          autoComplete="current-password"
          required
          hint={t("auth.passwordHint")}
          error={errors.password?.message}
          {...register("password")}
        />
        {errors.root?.message && (
          <p role="alert" className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700 dark:bg-rose-950/40 dark:text-rose-200">
            {errors.root.message}
          </p>
        )}
        <Button type="submit" fullWidth size="lg" loading={isSubmitting}>
          {isSubmitting ? t("auth.loggingIn") : t("auth.login")}
        </Button>
      </form>
    </AuthLayout>
  );
}
