Narrative:
Submitting workspaces


Scenario: submit an existing workspace

Given an archive
And a node with ID 1
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by user with ID testUser in node with ID 1
When that user chooses to submit the workspace
Then the workspace is successfully submitted
