
DROP INDEX sso_idx_uid IF EXISTS;
DROP INDEX sso_idx_sess IF EXISTS;
DROP INDEX pcplgrp_uqk_pc IF EXISTS;
DROP INDEX pcpl_uqk_uidnat IF EXISTS;
DROP INDEX pcpl_idx_uid IF EXISTS;
DROP INDEX pcpl_admin_uqk_pcpl IF EXISTS;
DROP INDEX ndpcpl_uqk_ndpcpl IF EXISTS;
DROP INDEX ndpcpl_idx_pcpl IF EXISTS;
DROP INDEX ndpcpl_idx_nd IF EXISTS;

DROP TABLE "user" IF EXISTS CASCADE;
DROP TABLE session IF EXISTS CASCADE;
DROP TABLE rule IF EXISTS CASCADE;
DROP TABLE principal_rule IF EXISTS CASCADE;
DROP TABLE principal IF EXISTS CASCADE;
DROP TABLE pcplgroup IF EXISTS CASCADE;
DROP TABLE pcpl_admin IF EXISTS CASCADE;
DROP TABLE nodepcpl_rule IF EXISTS CASCADE;
DROP TABLE nodepcpl_license IF EXISTS CASCADE;
DROP TABLE node_principal IF EXISTS CASCADE;
DROP TABLE node_license IF EXISTS CASCADE;
DROP TABLE license IF EXISTS CASCADE;

DROP SEQUENCE id_sequence_rule IF EXISTS;
DROP SEQUENCE id_sequence_principal IF EXISTS;
DROP SEQUENCE id_sequence_pcplrule IF EXISTS;
DROP SEQUENCE id_sequence_nodepcpl_license IF EXISTS;
DROP SEQUENCE id_sequence_nodepcpl IF EXISTS;
DROP SEQUENCE id_sequence_nodelics IF EXISTS;
DROP SEQUENCE id_sequence_ndpcpl_rule IF EXISTS;
DROP SEQUENCE id_sequence_license IF EXISTS;
