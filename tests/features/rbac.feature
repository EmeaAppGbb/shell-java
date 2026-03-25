Feature: Role-Based Access Control
  As an admin user
  I want to manage other users
  And as a regular user I should not have admin access

  Scenario: First registered user becomes admin
    Given the user store is empty
    When I register with username "firstuser" and password "Secret12!"
    Then the response status should be 201
    And the response should contain role "admin"

  Scenario: Admin can list all users
    Given the user store is empty
    And a user "admin1" with password "Secret12!" is registered
    And a user "regular1" with password "Secret34!" is registered
    And I am logged in as "admin1" with password "Secret12!"
    When I request the admin user list
    Then the response status should be 200
    And the user list should contain "admin1"
    And the user list should contain "regular1"

  Scenario: Non-admin gets 403 on admin endpoint
    Given the user store is empty
    And a user "admin1" with password "Secret12!" is registered
    And a user "regular1" with password "Secret34!" is registered
    And I am logged in as "regular1" with password "Secret34!"
    When I request the admin user list
    Then the response status should be 403

  Scenario: Unauthenticated request to admin endpoint returns 401
    Given the user store is empty
    When I request the admin user list without authentication
    Then the response status should be 401
