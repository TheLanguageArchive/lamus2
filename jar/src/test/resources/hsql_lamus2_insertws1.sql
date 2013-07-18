INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (1, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (0, 1, 1, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi', 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi',
        'collection1', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/1/collection1.cmdi', 'NODE_ISCOPY',
        'hdl:SOMETHING/3E9487E1-ED34-11E2-91E2-0800200C9A66', 'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (1, 1, 4, '', 'file:/some/random/folder/eclipse_shortcuts.pdf',
        'eclipse_shortcuts', 'RESOURCE_WR', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/1/collection1/eclipse_shortcuts.pdf', 'NODE_UPLOADED',
        'hdl:SOMETHING/3E9487E4-ED34-11E2-91E2-0800200C9A66', 'application/pdf');

UPDATE workspace SET top_node_id = 0, top_node_archive_id = 1, top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi' WHERE workspace_id = 1;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (0, 1, 'hdl:SOMETHING/3E9487E4-ED34-11E2-91E2-0800200C9A66');