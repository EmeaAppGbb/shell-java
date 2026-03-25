import { test, expect } from "./fixtures";

test.describe("Admin Page", () => {
  test("first user (admin) can see user table", async ({ page, loginUser }) => {
    // First registered user automatically becomes admin
    const admin = await loginUser();

    await page.goto("/admin");
    await expect(page.getByRole("heading", { name: "Users" })).toBeVisible();
    await expect(page.getByRole("table")).toBeVisible();
    await expect(page.getByRole("cell", { name: admin.username })).toBeVisible();
  });

  test("non-admin user sees access denied", async ({ page, loginUser }) => {
    // Register first user (becomes admin)
    const admin = await loginUser();

    // Logout admin
    await page.getByRole("button", { name: "Logout" }).click();
    await expect(page).toHaveURL(/\/login/);

    // Register and login second user (non-admin)
    await page.goto("/register");
    const username = `regular${Date.now()}`;
    const password = "Test1234!secure";
    await page.getByLabel("Username").fill(username);
    await page.getByLabel("Password", { exact: true }).fill(password);
    await page.getByLabel("Confirm Password").fill(password);
    await page.getByRole("button", { name: "Register" }).click();
    await expect(page).toHaveURL(/\/login/);

    await page.getByLabel("Username").fill(username);
    await page.getByLabel("Password").fill(password);
    await page.getByRole("button", { name: "Log in" }).click();
    await expect(page).toHaveURL(/\/profile/);

    // Try to access admin page
    await page.goto("/admin");
    await expect(page.getByText("Access Denied")).toBeVisible();
  });
});
