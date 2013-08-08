-- $Id$


-------------------------------------------------------------------------------
-- -== ams2 tables ==- ---------------------------------------------------------
-------------------------------------------------------------------------------

--\echo CREATING AMS2 TABLES...


-- PRINCIPALS :: user && groups  ==============================================
--\echo creating table "principal"...
--DROP TABLE "principal";
CREATE CACHED TABLE "principal" (
    id             integer PRIMARY KEY,
    uid            character varying(30) NOT NULL,
    name           character varying(120) NOT NULL,
    nature         character varying(15) NOT NULL,
    host_institute character varying(30) NOT NULL,
    host_srv       character varying(30) NOT NULL,
    stale          timestamp(6) DEFAULT NULL,
    imported       timestamp(6) DEFAULT NULL,
    exported       timestamp(6)  DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (creator) REFERENCES "principal"(id), 
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id)
);
CREATE UNIQUE INDEX pcpl_uqk_uidnat ON "principal" (uid, nature);
CREATE INDEX pcpl_idx_uid ON "principal" (uid);


--\echo creating table "user"...
--DROP TABLE "user";
CREATE CACHED TABLE "user" (
    id             integer PRIMARY KEY,
    firstname      character varying(120) DEFAULT NULL,
    email          character varying(255) DEFAULT NULL,
    organisation   character varying(255) NOT NULL,
    address        character varying(255) DEFAULT NULL,
    passwd         character varying(255) DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (id) REFERENCES "principal"(id),
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);


--\echo creating table "group"...
--	blank table as placeholder for the future
--	at the moment no further fields...
--DROP TABLE "group";
--CREATE CACHED TABLE "group" (
--    id             integer PRIMARY KEY,
--    creator        integer NOT NULL,
--    created_on     timestamp(6) NOT NULL,
--    lastmodifier   integer NOT NULL,
--    last_mod_on    timestamp(6) NOT NULL,
--FOREIGN KEY (id) REFERENCES "principal"(id),
--FOREIGN KEY (creator) REFERENCES "principal"(id),
--FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
--);



-- USERGROUPs - m:n ===========================================================
--\echo creating table "pcplgroup"...
--DROP TABLE "pcplgroup";
CREATE CACHED TABLE "pcplgroup" (
--  id             integer PRIMARY KEY,
    parent_id      integer NOT NULL,
    child_id       integer NOT NULL,
FOREIGN KEY (parent_id) REFERENCES "principal"(id),
FOREIGN KEY (child_id) REFERENCES "principal"(id)
);
CREATE UNIQUE INDEX pcplgrp_uqk_pc ON "pcplgroup" (parent_id,child_id);

-- GROUPOWNERS - m:n ===========================================================
--\echo creating table "pcpl_admin"...
--DROP TABLE "pcpl_admin;
CREATE TABLE "pcpl_admin" (
    admin_id      integer NOT NULL,
    pcpl_id       integer NOT NULL,
    FOREIGN KEY (admin_id) REFERENCES "principal"(id),
    FOREIGN KEY (pcpl_id) REFERENCES "principal"(id)
);
CREATE UNIQUE INDEX pcpl_admin_uqk_pcpl ON "pcpl_admin" (admin_id,pcpl_id);


-- RULE =======================================================================
--\echo creating table "rule"...
--DROP TABLE "rule";
CREATE CACHED TABLE "rule" (
    id             integer PRIMARY KEY,
    species        character varying(15) NOT NULL,
    realm          character varying(4) NOT NULL,
    competence     character varying(4) NOT NULL,
    disposition    integer NOT NULL,
    name           character varying(60) NOT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);



-- PRINCIPAL_RULE :: "roles" - m:n ============================================
--\echo creating table "principal_rule"...
--DROP TABLE "principal_rule";
CREATE CACHED TABLE "principal_rule" (
    id             integer PRIMARY KEY,
    pcpl_id        integer NOT NULL,
    rule_id        integer NOT NULL,
FOREIGN KEY (pcpl_id) REFERENCES "principal"(id),
FOREIGN KEY (rule_id) REFERENCES "rule"(id) 
);



-- NODE_PRINCIPAL-RULEs -  1(m:n):n ===========================================
--\echo creating table "node_principal"...
--DROP TABLE "node_principal";
CREATE CACHED TABLE "node_principal" (
    id             integer PRIMARY KEY,
    node_id        integer NOT NULL,
    pcpl_id        integer NOT NULL,
    stale          timestamp(6) DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (pcpl_id) REFERENCES "principal"(id),
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);
CREATE UNIQUE INDEX ndpcpl_uqk_ndpcpl ON "node_principal" (node_id, pcpl_id);
CREATE INDEX ndpcpl_idx_nd ON "node_principal" (node_id);
CREATE INDEX ndpcpl_idx_pcpl ON "node_principal" (pcpl_id);


--\echo creating table "nodepcpl_rule"...
--DROP TABLE "nodepcpl_rule";
CREATE CACHED TABLE "nodepcpl_rule" (
    id             integer PRIMARY KEY,
    node_pcpl_id   integer,
    rule_id        integer NOT NULL,
    nature         integer NOT NULL,
    priority       integer NOT NULL,
    expire_on      timestamp(6) DEFAULT NULL,
    max_storage_mb integer DEFAULT NULL,
    used_storage_mb integer DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (node_pcpl_id) REFERENCES "node_principal"(id), 
FOREIGN KEY (rule_id) REFERENCES "rule"(id), 
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);



-- LICENSEs ===================================================================
--\echo creating table  "license"...
--DROP TABLE "license";
CREATE CACHED TABLE "license" (
    id             integer PRIMARY KEY,
    name           character varying(120) NOT NULL,
    file           character varying(255) NOT NULL,
    sequence       integer NOT NULL,
    disabled       timestamp(6) DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);


--\echo creating table "node_license"...
--DROP TABLE "node_license";
CREATE CACHED TABLE "node_license" (
    id             integer PRIMARY KEY,
    node_id        integer NOT NULL,
    lics_id        integer NOT NULL,
    stale          timestamp(6) DEFAULT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (lics_id) REFERENCES "license"(id),
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);


--\echo creating table "nodepcpl_license"...
--DROP TABLE "nodepcpl_license";
CREATE CACHED TABLE "nodepcpl_license" (
    id             integer PRIMARY KEY,
    node_pcpl_id   integer NOT NULL,
    lics_id        integer NOT NULL,
    accepted_on    timestamp(6) NOT NULL,
    creator        integer NOT NULL,
    created_on     timestamp(6) NOT NULL,
    lastmodifier   integer NOT NULL,
    last_mod_on    timestamp(6) NOT NULL,
FOREIGN KEY (node_pcpl_id) REFERENCES "node_principal"(id), 
FOREIGN KEY (lics_id) REFERENCES "license"(id),
FOREIGN KEY (creator) REFERENCES "principal"(id),
FOREIGN KEY (lastmodifier) REFERENCES "principal"(id) 
);



-- SSO Sessions ===============================================================
--\echo creating table "session"...
--DROP TABLE "session";
CREATE CACHED TABLE "session" (
    session_id     character varying(255) NOT NULL,
    user_name      character varying(120) NOT NULL,
    application    character varying(120) DEFAULT NULL,
    host	           character varying(255) DEFAULT NULL,
    session_ts     timestamp DEFAULT 'now'
);
CREATE INDEX sso_idx_sess ON "session" (session_id);
CREATE INDEX sso_idx_uid  ON "session" (user_name);


--\echo AMS2 TABLES CREATED.

--end.