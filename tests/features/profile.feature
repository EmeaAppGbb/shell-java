Feature: User Profile
  As an authenticated user
  I want to view my profile
  So that I can see my account information

  Scenario: View own profile when authenticated
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    And I am logged in as "alice" with password "Secret12!"
    When I request my profile
    Then the response status should be 200
    And the profile username should be "alice"
    And the profile should include a role
    And the profile should include a createdAt timestamp

  Scenario: Cannot view profile when not authenticated
    Given the user store is empty
    When I request my profile without authentication
    Then the response status should be 401
