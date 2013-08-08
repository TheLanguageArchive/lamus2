INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (2, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (2, 2, 2, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi', 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi',
        'collection2', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2.cmdi', 'NODE_ISCOPY',
        'hdl:SOMETHING/3E9487E2-ED34-11E2-91E2-0800200C9A66', 'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (3, 2, -1, '', 'file:/some/random/folder/Zorro.cmdi',
        'Zorro', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2/Zorro.cmdi', 'NODE_UPLOADED',
        'hdl:SOMETHING/3E9487E5-ED34-11E2-91E2-0800200C9A66', 'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (4, 2, -1, '', 'file:/some/random/folder/Zorro/Zorro_07sep1967.pdf',
        'Zorro07sep1967', 'RESOURCE_WR', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/2/collection2/Zorro/Zorro_07sep1967.pdf', 'NODE_UPLOADED',
        'hdl:SOMETHING/3E94AEF0-ED34-11E2-91E2-0800200C9A66', 'application/pdf');


UPDATE workspace SET top_node_id = 2, top_node_archive_id = 2, top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi' WHERE workspace_id = 2;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (2, 3, 'hdl:SOMETHING/3E9487E5-ED34-11E2-91E2-0800200C9A66');
INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (3, 4, 'hdl:SOMETHING/3E94AEF0-ED34-11E2-91E2-0800200C9A66');
