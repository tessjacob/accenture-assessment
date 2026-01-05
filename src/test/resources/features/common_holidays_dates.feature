Feature: Common public holiday dates between two countries
  As a consumer of the Holidays API
  I want to retrieve public holidays celebrated on the same date in two countries
  So that I can compare overlapping public holidays across countries

Scenario: Retrieve common public holiday dates for two countries
    Given the year is 2024
    And the first country code is "DE"
    And the second country code is "FR"
    When I request the common public holidays
    Then the response status should be 200
    And the response should contain common holiday dates
    And each result should contain:
        | field        |
        | date         |
        | localNames   |
    And the response should not contain duplicate dates

Scenario: No common public holidays between two countries
    Given the year is 2024
    And the first country code is "JP"
    And the second country code is "AE"
    When I request the common public holidays
    Then the response status should be 200
    And the response should be empty

Scenario: Countries with identical holiday calendars
  Given the year is 2024
  And the first country code is "NL"
  And the second country code is "NL"
  When I request the common public holidays
  Then the response status should be 200
  And all public holiday dates for the year should be returned
  And the response should not contain duplicate dates

Scenario: Deduplicate holidays falling on the same date
  Given the year is 2024
  And the first country code is "DE"
  And the second country code is "AT"
  When I request the common public holidays
  Then the response status should be 200
  And each date should appear only once
  And each date should include local names from both countries
  
Scenario: Invalid country code
  Given the year is 2024
  And the first country code is "DE"
  And the second country code is "X1"
  When I request the common public holidays
  Then the response status should be 400
  And an error message should be returned  

Scenario: Year is in the future
  Given the year is 2100
  And the first country code is "DE"
  And the second country code is "FR"
  When I request the common public holidays
  Then the response status should be 400
  And an error message should be returned

Scenario: External holiday service is unavailable
  Given the year is 2024
  And the first country code is "DE"
  And the second country code is "FR"
  And the Nager API is unavailable
  When I request the common public holidays
  Then the response status should be 503
  And a service unavailable error should be returned

Scenario: Local names are preserved per country
  Given the year is 2024
  And the first country code is "DE"
  And the second country code is "FR"
  When I request the common public holidays
  Then the response status should be 200
  And each holiday date should contain:
    | country | localName |
    | DE      | DE Holiday |
    | FR      | FR Holiday |  