Narrative:
Deleting workspaces


Scenario: delete an existing workspace

Given a workspace with ID 1 created by user with ID testUser
When that user chooses to delete the workspace
Then the workspace is deleted
