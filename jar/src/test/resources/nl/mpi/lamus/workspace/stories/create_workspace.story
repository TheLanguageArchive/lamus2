Narrative:
Creating workspaces containing part of an existing archive


Scenario: create a workspace containing part of the archive

Given an archive
And a node with ID 1
And a user with ID testUser that has read and write access to the node with ID 1
When that user chooses to create a workspace in that node
Then a workspace is created in that node for that user
