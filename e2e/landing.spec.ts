import { test, expect } from "./fixtures";

test.describe("Landing Page", () => {
  test("shows login and register links when not logged in", async ({ page }) => {
    await page.goto("/");

    await expect(page.getByRole("heading", { name: "UserAuth" })).toBeVisible();
    await expect(page.getByRole("link", { name: "Login" })).toBeVisible();
    await expect(page.getByRole("link", { name: "Register" })).toBeVisible();
  });

  test("shows profile link when logged in", async ({ page, loginUser }) => {
    await loginUser();

    await page.goto("/");
    await expect(page.getByRole("link", { name: "Go to Profile" })).toBeVisible();

    // Login/Register links should not be visible
    await expect(page.getByRole("link", { name: "Login" })).not.toBeVisible();
    await expect(page.getByRole("link", { name: "Register" })).not.toBeVisible();
  });
});
