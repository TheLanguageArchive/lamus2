Narrative:
Creating workspaces in an archive


Scenario: create a workspace in an archive

Given an archive
And a node with ID 1
And a user with ID 'testUser'
And that user has write access to that node
When that user chooses to create a workspace in that node
Then a workspace is created in that node for that user
And the workspace is successfully initialised