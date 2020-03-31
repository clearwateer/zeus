Feature: Sherpa Workflow

  Background:
    Given Create Notification using Notification API
    Then Open Chrome and launch gmail
    When Login in Gmail using '' and ''
    Then search email from admin@ciitizen.com
    Then Click on the email to open
    Then Click and Copy Invitation Link



  Scenario: Verify if Email Notification to join ciitizen is sent


    Given Create Notification using Notification API
    Then Open chrome and launch gmail
    When Login in Gmail using username and password
    Then search email from admin@ciitizen.com
    Then Click on the email to open
    Then Click and Copy the Link


