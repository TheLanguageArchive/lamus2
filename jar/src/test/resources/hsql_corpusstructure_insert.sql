INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (0, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection.cmdi', now(), true, '544f1eb12e683bb4abb817d80dbf44cd',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E0-ED34-11E2-91E2-0800200C9A66', 1);
INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (1, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection1.cmdi', now(), true, '26280f87ae37660ee4959b62515e67af',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E1-ED34-11E2-91E2-0800200C9A66', 1);
INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (2, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection2.cmdi', now(), true, 'a3d562e98e409aed169d350d29cc48b0',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E2-ED34-11E2-91E2-0800200C9A66', 1);
INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (3, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3.cmdi', now(), true, '53e27d27282b17a3b00cbd3bc76b316a',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E3-ED34-11E2-91E2-0800200C9A66', 1);

INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (4, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/phist_crookedmagic.jpg', now(), true, '7b6c40afc64b8e0a77b566b634d291c0',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E6-ED34-11E2-91E2-0800200C9A66', 1);
INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, checksum, readrights, writerights, pid, accesslevel) 
    VALUES (5, 'http://tmp/lamusStoriesTestDirectory/archiveFolder/parent_collection/collection3/thematrixsoundtrack.jpg', now(), true, '66160ec54155c2cd6f107a1075fa0dae',
        'everybody', 'everybody', 'hdl:SOMETHING/3E9487E7-ED34-11E2-91E2-0800200C9A66', 1);


-- change types (these values are still based on the old type definition for the corpusstructure - corpus, session, ...)
INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (0, 2, 'text/cmdi', 'parent_collection', 'parent collection');
INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (1, 2, 'text/cmdi', 'collection1', 'collection 1');
INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (2, 2, 'text/cmdi', 'collection2', 'collection 2');
INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (3, 2, 'text/cmdi', 'collection3', 'collection 3');

INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (4, 16, 'image/jpeg', 'phist_crookedmagic', 'phist - crooked magic');
INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) 
    VALUES (5, 16, 'image/jpeg', 'thematrixsoundtrack', 'the matrix soundtrack');


INSERT INTO corpusstructure (nodeid, canonical, valid) 
    VALUES (0, true, true);
INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath) 
    VALUES (1, true, true, 0, '/MPI0#');
INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath) 
    VALUES (2, true, true, 0, '/MPI0#');
INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath) 
    VALUES (3, true, true, 0, '/MPI0#');

INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath1, vpath) 
    VALUES (4, true, true, 0, 3, '/MPI0#/MPI3#');
INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath1, vpath) 
    VALUES (5, true, true, 0, 3, '/MPI0#/MPI3#');

