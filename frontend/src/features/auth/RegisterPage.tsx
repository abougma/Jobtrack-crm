import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { z } from "zod";
import { ApiError } from "../../lib/api";
import { useI18n } from "../../lib/i18n";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { useToast } from "../../components/ui/toast-context";
import { AuthLayout } from "./AuthLayout";
import { useAuth } from "./AuthProvider";

export function RegisterPage() {
  const { t } = useI18n();
  const { register: registerUser } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();

  const schema = useMemo(
    () =>
      z
        .object({
          email: z
            .string()
            .min(1, { message: t("validation.required") })
            .email({ message: t("validation.email") }),
          password: z.string().min(12, { message: t("validation.passwordMin") }),
          confirmPassword: z.string().min(1, { message: t("validation.required") }),
        })
        .refine((values) => values.password === values.confirmPassword, {
          path: ["confirmPassword"],
          message: t("validation.passwordMismatch"),
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
    defaultValues: { email: "", password: "", confirmPassword: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await registerUser({ email: values.email, password: values.password });
      push(t("auth.registerSuccess"), "success");
      navigate("/profile", { replace: true });
    } catch (error) {
      const message = error instanceof ApiError ? error.message : t("auth.error");
      setError("root", { message });
      push(message, "error");
    }
  });

  return (
    <AuthLayout
      title={t("auth.registerTitle")}
      subtitle={t("auth.registerSubtitle")}
      footer={
        <span>
          {t("auth.hasAccount")}{" "}
          <Link to="/login" className="font-semibold text-primary-600 hover:underline dark:text-primary-300">
            {t("auth.loginLink")}
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
          autoComplete="new-password"
          required
          hint={t("auth.passwordHint")}
          error={errors.password?.message}
          {...register("password")}
        />
        <Input
          label={t("auth.confirmPassword")}
          type="password"
          autoComplete="new-password"
          required
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />
        {errors.root?.message && (
          <p role="alert" className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700 dark:bg-rose-950/40 dark:text-rose-200">
            {errors.root.message}
          </p>
        )}
        <Button type="submit" fullWidth size="lg" loading={isSubmitting}>
          {isSubmitting ? t("auth.registering") : t("auth.register")}
        </Button>
      </form>
    </AuthLayout>
  );
}
