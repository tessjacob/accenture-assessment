Feature: Count public holidays not falling on weekends
  As a consumer of the Holidays API
  I want to retrieve the number of public holidays per country
  that do not fall on weekends for a given year
  So that I can compare working-day holidays across countries

Background:
  Given the public holidays data is retrieved from the Nager public holidays API

Scenario: Retrieve holiday counts for multiple countries in a given year
  Given the year is 2024
  And the country codes are:
    | country |
    | DE      |
    | FR      |
    | IT      |
  When I request the holiday counts excluding weekends
  Then the response status should be 200
  And the response should contain 3 countries
  And each country should contain a holiday count
  And all holiday counts should be greater than or equal to 0
  And the countries should be sorted by holiday count in descending order

Scenario: Countries with no public holidays in the given year
  Given the year is 2024
  And the country codes are:
    | country |
    | AQ      |
  When I request the holiday counts excluding weekends
  Then the response status should be 200
  And the response should contain:
    | country | count |
    | AQ      | 0     |

Scenario: Holidays falling only on weekends are excluded
  Given the year is 2021
  And the country codes are:
    | country |
    | AE      |
  When I request the holiday counts excluding weekends
  Then the response status should be 200
  And all counted holidays should not fall on Saturday or Sunday

Scenario: Single country request
  Given the year is 2024
  And the country codes are:
    | country |
    | DE      |
  When I request the holiday counts excluding weekends
  Then the response status should be 200
  And the response should contain:
    | country | count |
    | DE      | > 0   |

Scenario: Invalid country code
  Given the year is 2024
  And the country codes are:
    | country |
    | X1      |
  When I request the holiday counts excluding weekends
  Then the response status should be 400
  And an error message should be returned

Scenario: Mixed valid and invalid country codes
  Given the year is 2024
  And the country codes are:
    | country |
    | DE      |
    | X1      |
    | FR      |
  When I request the holiday counts excluding weekends
  Then the response status should be 400
  And an error message should indicate invalid country codes

Scenario: Year is in the future
  Given the year is 2100
  And the country codes are:
    | country |
    | DE      |
  When I request the holiday counts excluding weekends
  Then the response status should be 400
  And an error message should be returned

Scenario: External holiday service is unavailable
  Given the year is 2024
  And the country codes are:
    | country |
    | DE      |
    | FR      |
  And the Nager API is unavailable
  When I request the holiday counts excluding weekends
  Then the response status should be 503
  And a service unavailable error should be returned

Scenario: Unexpected internal error occurs
  Given the year is 2024
  And the country codes are:
    | country |
    | DE      |
  And an unexpected internal error occurs
  When I request the holiday counts excluding weekends
  Then the response status should be 500
  And the error code should be "INTERNAL_SERVER_ERROR"