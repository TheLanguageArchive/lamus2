Narrative:
Deleting workspaces


Scenario: delete an existing workspace

Given an archive
And a metadata node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by testUser in node 1
When that user chooses to delete the workspace
Then the workspace is removed both from database and filesystem
