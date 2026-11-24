Feature: Wallet lookup
  A player checks the current balance of a wallet using its identifier.

  @pending
  Scenario: An existing wallet is returned with its balance
    Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 25.00 EUR
    When the player requests the wallet 00000000-0000-7000-8000-000000000001
    Then the wallet is returned with:
      | id                                   | balance   |
      | 00000000-0000-7000-8000-000000000001 | 25.00 EUR |

  @pending
  Scenario: An unknown wallet is reported as not found
    Given no wallet 00000000-0000-7000-8000-000000000002 exists
    When the player requests the wallet 00000000-0000-7000-8000-000000000002
    Then the wallet is reported as not found
