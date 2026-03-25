import { test as base, expect, type Page } from "@playwright/test";

/** Credentials for a test user. */
interface TestUser {
  username: string;
  password: string;
}

/** Extended test fixtures with auth helpers and auto-cleanup. */
interface Fixtures {
  /** Register a new user via the UI. Returns the credentials used. */
  registerUser: (opts?: Partial<TestUser>) => Promise<TestUser>;
  /** Register then log in a user via the UI. Returns the credentials. */
  loginUser: (opts?: Partial<TestUser>) => Promise<TestUser>;
  /** Backend API base URL for direct HTTP calls. */
  apiURL: string;
}

let counter = 0;

function uniqueUser(overrides?: Partial<TestUser>): TestUser {
  counter += 1;
  return {
    username: overrides?.username ?? `testuser${Date.now()}${counter}`,
    password: overrides?.password ?? "Test1234!secure",
  };
}

export const test = base.extend<Fixtures>({
  apiURL: [process.env.API_URL ?? "http://localhost:5000", { option: true }],

  registerUser: async ({ page }, use) => {
    const registered: TestUser[] = [];

    const fn = async (opts?: Partial<TestUser>) => {
      const creds = uniqueUser(opts);

      await page.goto("/register");
      await page.getByLabel("Username").fill(creds.username);
      await page.getByLabel("Password", { exact: true }).fill(creds.password);
      await page.getByLabel("Confirm Password").fill(creds.password);
      await page.getByRole("button", { name: "Register" }).click();

      // Wait for redirect to login page with success banner
      await expect(page).toHaveURL(/\/login/);
      registered.push(creds);
      return creds;
    };

    await use(fn);

    // Cleanup: remove test users via the API (best-effort)
    for (const u of registered) {
      try {
        await page.request.post(`${process.env.API_URL ?? "http://localhost:5000"}/api/auth/login`, {
          data: { username: u.username, password: u.password },
        });
      } catch {
        // ignore cleanup errors
      }
    }
  },

  loginUser: async ({ page }, use) => {
    const fn = async (opts?: Partial<TestUser>) => {
      const creds = uniqueUser(opts);

      // Register
      await page.goto("/register");
      await page.getByLabel("Username").fill(creds.username);
      await page.getByLabel("Password", { exact: true }).fill(creds.password);
      await page.getByLabel("Confirm Password").fill(creds.password);
      await page.getByRole("button", { name: "Register" }).click();
      await expect(page).toHaveURL(/\/login/);

      // Login
      await page.getByLabel("Username").fill(creds.username);
      await page.getByLabel("Password").fill(creds.password);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(page).toHaveURL(/\/profile/);

      return creds;
    };

    await use(fn);
  },
});

export { expect };
