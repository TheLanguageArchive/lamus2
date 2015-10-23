INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) 
    VALUES (5, 'testUser', now(), now(), 'INITIALISED', 'workspace initialised');

INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (11, 5, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi', 'node:003',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        'collection3', 'METADATA', 'file:/tmp/lamusStoriesTestDirectory/workspaceFolders/5/collection3.cmdi', 'NODE_ISCOPY',
        'text/x-cmdi+xml');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (12, 5, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg', 'node:004',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg',
        'phist_crookedmagic', 'RESOURCE_MR', null, 'NODE_DELETED', 'image/jpeg');
INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) 
    VALUES (13, 5, 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg', 'node:005',
                     'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg',
        'thematrixsoundtrack', 'RESOURCE_MR', null, 'NODE_VIRTUAL', 'image/jpeg');


UPDATE workspace SET top_node_id = 11,
        top_node_archive_url = 'file:/tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi',
        top_node_archive_uri = 'node:003' WHERE workspace_id = 5;

INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) 
    VALUES (11, 13, 'node:005');



