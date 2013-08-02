Narrative:
Submitting workspaces with different stories.
These should cover different possibilities:
- the workspace has added node(s)
- the workspace has changed node(s)
- the workspace has deleted node(s)
- the workspace has replaced node(s)


Scenario: submit workspace with an added resource node

Given an archive
And a metadata node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by testUser in node 1
And eclipse_shortcuts.pdf has been linked to the workspace
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
!-- And the new node, with the new ID 2, is properly linked in the database, from parent node with ID 1 (done by the crawler?)
And the new node, eclipse_shortcuts.pdf, is properly linked from the parent file (node with ID 1) and exists in the corpusstructure database
And eclipse_shortcuts.pdf is present in the proper location in the filesystem, under the directory of the parent node 1
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with an added metadata node

Given an archive
And a metadata node with ID 2 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 2
And a workspace with ID 2 created by testUser in node 2
And Zorro.cmdi has been linked to the workspace
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
!-- And the new node, with the new ID 2, is properly linked in the database, from parent node with ID 1 (done by the crawler?)
And the new node, Zorro.cmdi, is properly linked from the parent file (node with ID 2) and exists in the corpusstructure database
And Zorro.cmdi is present in the proper location in the filesystem, under the directory of the parent node 2
And the children of Zorro.cmdi are also present in the database and in the filesystem
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with a changed node

Given an archive
And a metadata node with ID 3 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 3
And a workspace with ID 3 created by testUser in node 3
And the top node has had some metadata added
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And the name of the node with ID 3 has changed both in the database and in the filesystem
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace without changes
Given an archive
And a metadata node with ID 3 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 3
And a workspace with ID 4 created by testUser in node 3
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And no changes were made to the archive
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with a deleted node
Given an archive
And a metadata node with ID 3 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 3
And a workspace with ID 5 created by testUser in node 3
And one of the resource nodes, phist_crookedmagic.jpg, is deleted
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And the deleted node has been moved to the trash folder in the filesystem
!-- And an email notification is sent to the user
!-- ...


!-- Scenario: submit workspace with a replaced node

