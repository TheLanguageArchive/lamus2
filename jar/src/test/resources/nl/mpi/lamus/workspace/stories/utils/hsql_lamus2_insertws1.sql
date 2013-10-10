INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (1, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (0, 1, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi', 'node:001',
        'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi',
        'collection1', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/1/collection1.cmdi', 'NODE_ISCOPY',
        'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (1, 1, null, null, 'file:/some/random/folder/eclipse_shortcuts.pdf',
        'eclipse_shortcuts', 'RESOURCE_WR', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/1/collection1/eclipse_shortcuts.pdf',
        'NODE_UPLOADED', 'application/pdf');

UPDATE workspace SET top_node_id = 0,
        top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi',
        top_node_archive_uri = 'node:001' WHERE workspace_id = 1;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (0, 1, '');