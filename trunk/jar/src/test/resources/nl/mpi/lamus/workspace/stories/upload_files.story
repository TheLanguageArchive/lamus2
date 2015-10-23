Narrative:
Uploading files into a workspace


Scenario: upload metadata file

Given an archive
And a metadata node with URI node:001 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:001
And a workspace with ID 1 created by testUser in node node:001
When that user chooses to upload a metadata file into the workspace
Then the uploaded node is present in the workspace folder
And the uploaded node is present in the database
And the uploaded node has no parent node


Scenario: upload resource file

Given an archive
And a metadata node with URI node:001 which is a child of node with URI node:000
And a user with ID testUser that has read and write access to the node with URI node:001
And a workspace with ID 1 created by testUser in node node:001
When that user chooses to upload a resource file into the workspace
Then the uploaded node is present in the workspace folder
And the uploaded node is present in the database
And the uploaded node has no parent node
