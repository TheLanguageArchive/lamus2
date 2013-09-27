INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (2, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (2, 2, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi', 'node:002',
        'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi',
        'collection2', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2.cmdi', 'NODE_ISCOPY',
        'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (3, 2, '', 'file:/some/random/folder/Zorro.cmdi', '',
        'Zorro', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2/Zorro.cmdi', 'NODE_UPLOADED',
        'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (4, 2, '', 'file:/some/random/folder/Zorro/Zorro_07sep1967.pdf', '',
        'Zorro07sep1967', 'RESOURCE_WR', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2/Zorro/Zorro_07sep1967.pdf', 'NODE_UPLOADED',
        'application/pdf');


UPDATE workspace SET top_node_id = 2,
        top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi',
        top_node_archive_uri = 'node:002' WHERE workspace_id = 2;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (2, 3, '');
INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (3, 4, '');
