import { test, expect } from "./fixtures";

test.describe("Authentication", () => {
  test("register a new user and redirect to login", async ({ page, registerUser }) => {
    const creds = await registerUser();

    // Should land on login page with success message
    await expect(page).toHaveURL(/\/login\?registered=true/);
    await expect(page.getByText("Registration successful")).toBeVisible();
  });

  test("login with valid credentials redirects to profile", async ({ page, loginUser }) => {
    const creds = await loginUser();

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.getByText(creds.username)).toBeVisible();
  });

  test("login with invalid credentials shows error", async ({ page, registerUser }) => {
    const creds = await registerUser();

    await page.goto("/login");
    await page.getByLabel("Username").fill(creds.username);
    await page.getByLabel("Password").fill("WrongPassword123!");
    await page.getByRole("button", { name: "Log in" }).click();

    await expect(page.getByText(/invalid/i)).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test("logout redirects to login page", async ({ page, loginUser }) => {
    await loginUser();

    // Click logout button in NavBar
    await page.getByRole("button", { name: "Logout" }).click();

    // Should redirect away from profile
    await expect(page).toHaveURL(/\/login/);
  });

  test("unauthenticated access to /profile redirects to login", async ({ page }) => {
    await page.goto("/profile");

    await expect(page).toHaveURL(/\/login/);
  });
});
