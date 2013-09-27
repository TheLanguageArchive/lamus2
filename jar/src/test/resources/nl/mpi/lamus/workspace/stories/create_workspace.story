Narrative:
Creating workspaces containing part of an existing archive


Scenario: create a workspace containing part of the archive

Given an archive
And a user with ID testUser that has read and write access to the node with URI node:003
When that user chooses to create a workspace in the node with URI node:003
Then the workspace is created in the database
And the workspace files are present in the proper location in the filesystem
