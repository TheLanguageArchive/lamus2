INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (3, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (5, 3, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi', 'node:003',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        'collection3_CHANGED', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/3/collection3.cmdi', 'NODE_ISCOPY',
        'text/cmdi');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (6, 3, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg', 'node:004',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg',
        'phist_crookedmagic', 'RESOURCE_MR', null, 'NODE_VIRTUAL', 'image/jpeg');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (7, 3, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg', 'node:005',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg',
        'thematrixsoundtrack', 'RESOURCE_MR', null, 'NODE_VIRTUAL', 'image/jpeg');


UPDATE workspace SET top_node_id = 5,
        top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        top_node_archive_uri = 'node:003' WHERE workspace_id = 3;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (5, 6, 'node:004');
INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (5, 7, 'node:005');

