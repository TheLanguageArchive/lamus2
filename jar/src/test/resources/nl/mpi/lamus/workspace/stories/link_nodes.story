Narrative:
Linking nodes into the workspace tree


Scenario: link uploaded metadata node

Given an archive
And a metadata node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by testUser in node 1
And a metadata file was uploaded into the workspace
When that user chooses to link the uploaded node to the top node of the workspace
Then that node is linked to the parent node in the database
And that node is included in the parent node, as a reference


Scenario: link uploaded resource node

Given an archive
And a metadata node with ID 1 which is a child of node with ID 0
And a user with ID testUser that has read and write access to the node with ID 1
And a workspace with ID 1 created by testUser in node 1
And a resource file was uploaded into the workspace
When that user chooses to link the uploaded node to the top node of the workspace
Then that node is linked to the parent node in the database
And that node is included in the parent node, as a reference