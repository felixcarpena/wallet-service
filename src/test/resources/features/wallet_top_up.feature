Feature: Wallet top-up
  A player adds money to a wallet with a credit card.

  Rule: A top-up is credited only after the payment provider accepts the charge

    Scenario Outline: A successful charge adds the amount to the balance
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of <balance>
      And the payment provider accepts the charge
      When the player tops up <amount> on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as completed
      And the payment provider was asked to charge <amount> on card 4242 4242 4242 4242
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is <new balance>

      Examples:
        | balance   | amount    | new balance |
        | 10.00 EUR | 15.00 EUR | 25.00 EUR   |
        | 0.00 EUR  | 10.00 EUR | 10.00 EUR   |
        | 90.00 EUR | 10.00 EUR | 100.00 EUR  |

    @pending
    Scenario: A declined charge leaves the balance unchanged
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      And the payment provider declines the charge
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as declined
      And the payment provider was asked to charge 15.00 EUR on card 4242 4242 4242 4242
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    @pending
    Scenario: An unreachable payment provider leaves the balance unchanged
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      And the payment provider cannot be reached
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as failed
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

  Rule: A top-up the payment provider may have charged is reported as not confirmed and not credited

    @pending
    Scenario: A server error from the payment provider is reported as not confirmed
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      And the payment provider answers with a server error
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as not confirmed
      And the payment provider was asked to charge 15.00 EUR on card 4242 4242 4242 4242
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    @pending
    Scenario: A charge accepted without a payment reference is reported as not confirmed
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      And the payment provider accepts the charge without a payment reference
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as not confirmed
      And the payment provider was asked to charge 15.00 EUR on card 4242 4242 4242 4242
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    @pending
    Scenario: A timed-out charge is reported as not confirmed
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      And the payment provider does not answer in time
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as not confirmed
      And the payment provider was asked to charge 15.00 EUR on card 4242 4242 4242 4242
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

  Rule: An invalid top-up never reaches the payment provider

    Scenario Outline: A top-up with a non-positive amount is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up <amount> on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

      Examples:
        | amount     |
        | 0.00 EUR   |
        | -10.00 EUR |

    Scenario Outline: A top-up with more decimals than allowed is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up <amount> on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

      Examples:
        | amount       |
        | 15.001 EUR   |
        | 15.00005 EUR |

    Scenario: A top-up in an unknown currency is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up 15.00 XYZ on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    Scenario: A top-up without an amount is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242 without an amount
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    Scenario: A top-up without a currency is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up 15.00 without a currency on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    Scenario: A top-up in a different currency from the wallet is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up 15.00 USD on wallet 00000000-0000-7000-8000-000000000001 with card 4242 4242 4242 4242
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

    Scenario: A top-up without a card number is rejected
      Given a wallet 00000000-0000-7000-8000-000000000001 with a balance of 10.00 EUR
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000001 without a card number
      Then the top-up is reported as invalid
      And the payment provider was not asked to charge anything
      And the balance of wallet 00000000-0000-7000-8000-000000000001 is 10.00 EUR

  Rule: A top-up needs an existing wallet

    Scenario: A top-up to an unknown wallet is reported as not found
      Given no wallet 00000000-0000-7000-8000-000000000002 exists
      When the player tops up 15.00 EUR on wallet 00000000-0000-7000-8000-000000000002 with card 4242 4242 4242 4242
      Then the wallet is reported as not found
      And the payment provider was not asked to charge anything
