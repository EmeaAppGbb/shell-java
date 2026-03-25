Feature: Authentication
  As a user of the UserAuth application
  I want to register, log in, and log out
  So that I can securely access my account

  Scenario: Register with valid credentials
    Given the user store is empty
    When I register with username "alice" and password "Secret12!"
    Then the response status should be 201
    And the response should contain "registered"
    And the response should contain role "admin"

  Scenario: Register a second user gets user role
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    When I register with username "bob" and password "Secret34!"
    Then the response status should be 201
    And the response should contain role "user"

  Scenario: Register with duplicate username returns error
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    When I register with username "alice" and password "Other123!"
    Then the response status should be 409
    And the response should contain error "already exists"

  Scenario: Register with missing username returns error
    Given the user store is empty
    When I register with username "" and password "Secret12!"
    Then the response status should be 422

  Scenario: Register with short password returns error
    Given the user store is empty
    When I register with username "alice" and password "short"
    Then the response status should be 422

  Scenario: Login with valid credentials
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    When I login with username "alice" and password "Secret12!"
    Then the response status should be 200
    And the response should contain "Login successful"
    And the response should set a token cookie

  Scenario: Login with invalid password returns error
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    When I login with username "alice" and password "WrongPass1!"
    Then the response status should be 401
    And the response should contain error "Invalid"

  Scenario: Login with nonexistent user returns error
    Given the user store is empty
    When I login with username "ghost" and password "Secret12!"
    Then the response status should be 401

  Scenario: Logout clears the session
    Given the user store is empty
    And a user "alice" with password "Secret12!" is registered
    And I am logged in as "alice" with password "Secret12!"
    When I call the logout endpoint
    Then the response status should be 200
    And the token cookie should be cleared

  Scenario: Access protected route without authentication returns 401
    Given the user store is empty
    When I request my profile without authentication
    Then the response status should be 401
