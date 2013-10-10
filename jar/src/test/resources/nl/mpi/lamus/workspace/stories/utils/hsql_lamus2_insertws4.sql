INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (4, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (8, 4, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi', 'node:003',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        'collection3', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/4/collection3.cmdi', 'NODE_ISCOPY',
        'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (9, 4, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg', 'node:004',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg',
        'phist_crookedmagic', 'RESOURCE_MR', null, 'NODE_VIRTUAL', 'image/jpeg');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (10, 4, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg', 'node:005',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg',
        'thematrixsoundtrack', 'RESOURCE_MR', null, 'NODE_VIRTUAL', 'image/jpeg');


UPDATE workspace SET top_node_id = 8,
        top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        top_node_archive_uri = 'node:003' WHERE workspace_id = 4;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (8, 9, 'node:004');
INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (8, 10, 'node:005');


