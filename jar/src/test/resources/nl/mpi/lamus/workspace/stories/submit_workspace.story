Narrative:
Submitting workspaces



!-- Scenario: submit workspace without changes

!-- Scenario: submit workspace with a deleted node
  !--  ADD ALSO STORIES FOR DELETING NODE

Scenario: submit workspace with an added node

Given an archive
And a node with ID 0 which is the top node
And a node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by user with ID testUser in node with ID 1 to which a new node has been linked
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace with ID 1 is marked as successfully submitted
And the end date of the workspace with ID 1 is set
And the new node, with the new ID 2, is properly linked in the database, from parent node with ID 1
And the new node, with ID 2, is properly linked from the parent file (node with ID 1)
And the file corresponding to the node with ID 2 is present in the proper location in the filesystem, under the directory of the parent node 1
!-- And an email notification is sent to the user
!-- ...

  !--  ADD ALSO STORIES FOR ADDED NODE

!-- Scenario: submit workspace with a changed node
  !--  ADD ALSO STORIES FOR CHANGED NODE




!-- Scenario: submit an existing workspace

!-- Given an archive
!-- And a node with ID 1

!-- And a node with ID 2 which is a child of node with ID 1

!-- And a user with ID testUser that has read and write access to the node with ID 1
!-- And a workspace with ID 1 created by user with ID testUser in node with ID 1

!-- And that user deletes node with ID 2

!-- When that user chooses to submit the workspace
!-- Then the workspace is successfully submitted
!-- And node with ID 2 was successfully deleted
