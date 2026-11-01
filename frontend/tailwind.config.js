/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        bg: "rgb(var(--bg) / <alpha-value>)",
        surface: "rgb(var(--surface) / <alpha-value>)",
        elevated: "rgb(var(--elevated) / <alpha-value>)",
        border: "rgb(var(--border) / <alpha-value>)",
        fg: "rgb(var(--fg) / <alpha-value>)",
        muted: "rgb(var(--muted) / <alpha-value>)",
        primary: {
          DEFAULT: "#4f5bd5",
          50: "#eef0ff",
          100: "#e0e3ff",
          200: "#c7ccff",
          300: "#a5adff",
          400: "#8389fa",
          500: "#4f5bd5",
          600: "#3f49b8",
          700: "#333c95",
          800: "#2b3378",
          900: "#252c60",
        },
        accent: {
          DEFAULT: "#f0a94b",
          50: "#fdf6e9",
          100: "#faecd0",
          200: "#f5d79f",
          300: "#efbd69",
          400: "#f0a94b",
          500: "#e08f2a",
          600: "#c06f1e",
          700: "#9c541c",
          800: "#7f441e",
          900: "#6a391c",
        },
      },
      fontFamily: {
        display: ["Space Grotesk", "system-ui", "sans-serif"],
        sans: ["DM Sans", "system-ui", "sans-serif"],
      },
      borderRadius: {
        xl: "0.9rem",
        "2xl": "1.25rem",
      },
      boxShadow: {
        card: "0 1px 2px rgb(15 23 42 / 0.04), 0 8px 24px -12px rgb(15 23 42 / 0.12)",
      },
      keyframes: {
        "fade-in": {
          from: { opacity: "0", transform: "translateY(4px)" },
          to: { opacity: "1", transform: "translateY(0)" },
        },
      },
      animation: {
        "fade-in": "fade-in 160ms ease-out",
      },
    },
  },
  plugins: [],
};
