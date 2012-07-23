Narrative:
Creating workspaces in an archive


Scenario: create a workspace in an archive

Given an archive
And a node with ID 1
And a user with ID testUser that has read and write access to the node with ID 1
When that user chooses to create a workspace in that node
Then a workspace is created in that node for that user
