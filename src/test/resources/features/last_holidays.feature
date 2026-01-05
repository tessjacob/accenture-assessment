Feature: Last three celebrated holidays
    As a consumer of the Holidays API
    I want to retrieve the last celebrated public holidays for a country
    So that I can display recent holidays to users

Background:
    Given today is the current system date
    And holidays are retrieved from the Nager public holidays API

Scenario: Retrieve last three celebrated holidays for a valid country
    Given the country code is "DE"
    When I request the last three holidays
    Then the response status should be 200
    And the response should contain at most 3 holidays
    And all holiday dates should be before today
    And each holiday should contain a date and a localName

Scenario: Invalid country code
    Given the country code is "X1"
    When I request the last three holidays
    Then the response status should be 400
    And an error message should be returned

Scenario: Lowercase country code is accepted
    Given the country code is "de"
    When I request the last three holidays
    Then the response status should be 200
    And the response should be the same as for country "DE"

Scenario: Holidays are sorted by most recent date first
    Given the country code is "DE"
    When I request the last three holidays
    Then the response status should be 200
    And the holidays should be sorted by date descending

Scenario: External holiday service is unavailable
    Given the country code is "DE"
    And the Nager API is unavailable
    When I request the last three holidays
    Then the response status should be 503
    And a service unavailable error should be returned

Scenario: Country is not supported
    Given the country code is "AQ"
    When I request the last three holidays
    Then the response status should be 404
    And the error code should be "COUNTRY_NOT_SUPPORTED"

Scenario: Internal server error occurs
    Given the country code is "DE"
    And an unexpected internal error occurs
    When I request the last three holidays
    Then the response status should be 500
    And the error code should be "INTERNAL_SERVER_ERROR"