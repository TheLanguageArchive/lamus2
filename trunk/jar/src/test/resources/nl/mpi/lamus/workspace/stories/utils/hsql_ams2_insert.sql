INSERT INTO "principal" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (1, 'testUser', 'TestUser', 'USR', 'MPI for Psycholinguistics', 'ams2/ldap', 0, now(), 0, now());
INSERT INTO "user" 
    VALUES (1, 'Test', 'testUser@mpi.nl', 'mpi 4 psycholinguistics', 'wundtlaan 1\n6525 xd nijmegen\nthe netherlands', '{CRYPT}.GDJxqz1Ipii6', 1, now(), 1, now());

INSERT INTO "node_principal" (id, node_id, pcpl_id, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (1, 1, 1, 0, now(), 0, now());
INSERT INTO "node_principal" (id, node_id, pcpl_id, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (2, 2, 1, 0, now(), 0, now());
INSERT INTO "node_principal" (id, node_id, pcpl_id, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (3, 3, 1, 0, now(), 0, now());

INSERT INTO "nodepcpl_rule" (id, node_pcpl_id, rule_id, nature, priority, max_storage_mb, used_storage_mb, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (1, 1, 120, 1, 150, 50000, 10000, 0, now(), 0, now());
INSERT INTO "nodepcpl_rule" (id, node_pcpl_id, rule_id, nature, priority, max_storage_mb, used_storage_mb, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (2, 2, 120, 1, 150, 50000, 10000, 0, now(), 0, now());
INSERT INTO "nodepcpl_rule" (id, node_pcpl_id, rule_id, nature, priority, max_storage_mb, used_storage_mb, creator, created_on, lastmodifier, last_mod_on) 
    VALUES (3, 3, 120, 1, 150, 50000, 10000, 0, now(), 0, now());
