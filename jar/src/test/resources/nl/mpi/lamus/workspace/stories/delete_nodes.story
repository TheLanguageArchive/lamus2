Narrative:
Deleting nodes from the workspace tree


Scenario: delete metadata node

Given an archive
And a metadata node with ID 2 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 2
And a workspace with ID 2 created by testUser in node 2
When that user chooses to delete a metadata node from the workspace tree
Then that node in no longer linked to the former parent node in the database
And that node is no longer included in the former parent node, as a reference
And that node is marked as deleted in the database

Scenario: delete resource node

Given an archive
And a metadata node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by testUser in node 1
When that user chooses to delete a resource node from the workspace tree
Then that node in no longer linked to the former parent node in the database
And that node is no longer included in the former parent node, as a reference
And that node is marked as deleted in the database