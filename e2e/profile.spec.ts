import { test, expect } from "./fixtures";

test.describe("Profile Page", () => {
  test("shows username and role badge", async ({ page, loginUser }) => {
    const creds = await loginUser();

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.getByText(creds.username)).toBeVisible();
    await expect(page.getByTestId("role-badge")).toBeVisible();
  });

  test("displays logout button in navigation", async ({ page, loginUser }) => {
    await loginUser();

    await expect(page.getByRole("button", { name: "Logout" })).toBeVisible();
  });

  test("shows member since date", async ({ page, loginUser }) => {
    await loginUser();

    await expect(page.getByText(/Member since/)).toBeVisible();
  });
});
