INSERT INTO "principal" VALUES(0,'system.lat.ams2@mpi.nl','system','USR','MPINLA','ams2/ldap',NULL,NULL,NULL,0,NOW(),0,NOW());
INSERT INTO "user" VALUES(0,'system','latadmin@mpi.nl','mpi 4 psycholinguistics','wundtlaan 1\n6525 xd nijmegen\nthe netherlands','somepassword',0,NOW(),0,NOW());

INSERT INTO "principal" VALUES(-1,'everybody','-== EVERYBODY ==-','ALL','MPINLA','ams2/ldap',NULL,NULL,NULL,0,NOW(),0,NOW());
INSERT INTO "principal" VALUES(-2,'anyAuthenticatedUser','-== REGISTERED USERS ==-','ALL','MPINLA','ams2/ldap',NULL,NULL,NULL,0,NOW(),0,NOW());

INSERT INTO "rule" VALUES(000, 'AM',    '0001', '0011', -100, 'Archive Manager',  0, now(), 0, now());
INSERT INTO "rule" VALUES(100, 'DC',    '0110', '0001',  -80, 'Domain Curator',   0, now(), 0, now());
INSERT INTO "rule" VALUES(110, 'DM',    '0110', '0001',  -60, 'Domain Manager',   0, now(), 0, now());
INSERT INTO "rule" VALUES(120, 'DE',    '0110', '0010',  -40, 'Domain Editor',    0, now(), 0, now());
INSERT INTO "rule" VALUES(200, 'resrc', '1000', '0001',  100, 'Read Resource',     0, now(), 0, now());
INSERT INTO "rule" VALUES(210, 'info',  '0110', '0001',   10, 'Read Info Files',  0, now(), 0, now());
INSERT INTO "rule" VALUES(220, 'anno',  '0110', '0001',   20, 'Read Annotations', 0, now(), 0, now());
INSERT INTO "rule" VALUES(230, 'img',   '0110', '0001',   30, 'Read Images',      0, now(), 0, now());
INSERT INTO "rule" VALUES(240, 'audio', '0110', '0001',   40, 'Read Audio Files', 0, now(), 0, now());
INSERT INTO "rule" VALUES(250, 'video', '0110', '0001',   50, 'Read Video Files', 0, now(), 0, now());
INSERT INTO "rule" VALUES(270, 'forbid','1110', '0011',	  70, 'Forbidden Access', 0, now(), 0, now());
INSERT INTO "rule" VALUES(300, 'basic', '0000', '0011', -100, 'Accept License',   0, now(), 0, now());
INSERT INTO "rule" VALUES(310, 'dom',   '0000', '0001', -100, 'Read Domain Nodes',0, now(), 0, now());


INSERT INTO "nodepcpl_rule"  VALUES(-200, NULL, 200, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-210, NULL, 210, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-220, NULL, 220, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-230, NULL, 230, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-240, NULL, 240, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-250, NULL, 250, 1,    0, NULL, NULL, NULL,  0, now(), 0, now());

INSERT INTO "nodepcpl_rule"  VALUES(-270, NULL, 270, 0,  125, NULL, NULL, NULL,	 0, now(), 0, now());

INSERT INTO "nodepcpl_rule"  VALUES( 000, NULL, 000, 1, 1000, NULL, NULL, NULL,  0, now(), 0, now());

INSERT INTO "nodepcpl_rule"  VALUES(-300, NULL, 300, -1,  75, NULL, NULL, NULL,  0, now(), 0, now());

INSERT INTO "nodepcpl_rule"  VALUES(-310, NULL, 310, 1,  100, NULL, NULL, NULL,  0, now(), 0, now());

INSERT INTO "nodepcpl_rule"  VALUES(-100, NULL, 100, 1,   150, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-110, NULL, 110, 1,   150, NULL, NULL, NULL,  0, now(), 0, now());
INSERT INTO "nodepcpl_rule"  VALUES(-120, NULL, 120, 1,   150, NULL,    0,    0,  0, now(), 0, now());

INSERT INTO "license" VALUES(10, 'Dobes Code of Conduct v2', 'dobes_coc_v2.html', 10, NULL, 0, now(), 0, now());
INSERT INTO "license" VALUES(20, 'MPI Policy', 'mpiPolicy.html', 20, NULL, 0, now(), 0, now());
INSERT INTO "license" VALUES(30, 'Stivers Code of Conduct', 'stivers_coc.html', 30, NULL, 0, now(), 0, now());
INSERT INTO "license" VALUES(40, 'Hindi Child Language Data: Code of Conduct', 'hindi_coc.html', 40, NULL, 0, now(), 0, now());
