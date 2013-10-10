Narrative:
Submitting workspaces with different stories.
These should cover different possibilities:
- the workspace has added node(s)
- the workspace has changed node(s)
- the workspace has deleted node(s)
- the workspace has replaced node(s)


Scenario: submit workspace with an added resource node

Given an archive
And a metadata node with URI node:001 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:001
And a workspace with ID 1 created by testUser in node node:001
And eclipse_shortcuts.pdf has been linked to the workspace
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And eclipse_shortcuts.pdf is present in the proper location in the filesystem, under the directory of the parent node node:001
And the new node is properly linked from the parent file (node with URI node:001) and was assigned an archive handle
And the new node exists in the corpusstructure database and is properly linked there
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with an added metadata node

Given an archive
And a metadata node with URI node:002 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:002
And a workspace with ID 2 created by testUser in node node:002
And Zorro.cmdi has been linked to the workspace
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And Zorro.cmdi is present in the proper location in the filesystem, under the directory of the parent node node:002
And the new node is properly linked from the parent file (node with URI node:002) and was assigned an archive handle
And the children of Zorro.cmdi are also present in the filesystem and were assigned archive handles
And the new node exists in the corpusstructure database and is properly linked there
And the children of Zorro.cmdi are also present in the database
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with a changed node

Given an archive
And a metadata node with URI node:003 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:003
And a workspace with ID 3 created by testUser in node node:003
And the top node has had some metadata added
!-- ...
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And the metadata of the node with URI node:003 has changed in the filesystem
And the metadata of the node with URI node:003 has changed in the database
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace without changes
Given an archive
And a metadata node with URI node:003 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:003
And a workspace with ID 4 created by testUser in node node:003
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And no changes were made to the archive
!-- And an email notification is sent to the user
!-- ...


Scenario: submit workspace with a deleted node
Given an archive
And a metadata node with URI node:003 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:003
And a workspace with ID 5 created by testUser in node node:003
And one of the resource nodes, phist_crookedmagic.jpg, is deleted
When that user chooses to submit the workspace
Then the status of the workspace is marked as successfully submitted
And the end date of the workspace is set
And the deleted node has been moved to the trash folder in the filesystem
And the deleted node has been updated in the database
!-- And an email notification is sent to the user
!-- ...


!-- Scenario: submit workspace with a replaced node

