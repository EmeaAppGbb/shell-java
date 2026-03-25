import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for spec2cloud Java shell E2E tests.
 * The Aspire apphost orchestrates both the Spring Boot backend (port 8080)
 * and Next.js frontend (port 3001).
 */
export default defineConfig({
  testDir: ".",
  testMatch: "**/*.spec.ts",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? "github" : "html",
  timeout: 30_000,
  expect: { timeout: 5_000 },

  use: {
    baseURL: process.env.BASE_URL ?? "http://localhost:3001",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],

  webServer: {
    command: "dotnet run --project apphost.cs",
    cwd: "..",
    url: "http://localhost:3001",
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    stdout: "pipe",
    stderr: "pipe",
  },
});
