Narrative:
Submitting workspaces into an archive


Scenario: submit a workspace into an archive

Given an archive
And an existing workspace in node with ID 1 and name 'testNode' created by a user with ID 'testUser'
And the user changed the name of the top node to 'testNodeChanged'
When that user chooses to submit the workspace into the archive
Then the workspace is set as submitted in the database
And the workspace files/folders are moved into the submitted workspaces folder
And the changes made in the workspace are reflected in the archive