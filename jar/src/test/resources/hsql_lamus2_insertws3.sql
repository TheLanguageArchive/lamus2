INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (3, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (5, 3, 3, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        'collection3_CHANGED', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/3/collection3.cmdi', 'NODE_ISCOPY',
        'hdl:SOMETHING/3E9487E3-ED34-11E2-91E2-0800200C9A66', 'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (6, 3, 4, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg',
        'phist_crookedmagic', 'RESOURCE_MR', '', 'NODE_VIRTUAL', 'hdl:SOMETHING/3E9487E6-ED34-11E2-91E2-0800200C9A66', 'image/jpeg');
INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) 
    VALUES (7, 3, 5, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg',
        'thematrixsoundtrack', 'RESOURCE_MR', '', 'NODE_VIRTUAL', 'hdl:SOMETHING/3E9487E7-ED34-11E2-91E2-0800200C9A66', 'image/jpeg');


UPDATE workspace SET top_node_id = 5, top_node_archive_id = 3, top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi' WHERE workspace_id = 3;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (5, 6, 'hdl:SOMETHING/3E9487E6-ED34-11E2-91E2-0800200C9A66');
INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (5, 7, 'hdl:SOMETHING/3E9487E7-ED34-11E2-91E2-0800200C9A66');
