
ALTER TABLE node DROP CONSTRAINT workspace_node;
ALTER TABLE node_lock DROP CONSTRAINT node_lock_workspace;
ALTER TABLE node_link DROP CONSTRAINT node_link_child;
ALTER TABLE node_link DROP CONSTRAINT node_link_parent;
ALTER TABLE node_replacement DROP CONSTRAINT node_replacement_old;
ALTER TABLE node_replacement DROP CONSTRAINT node_replacement_new;
DROP TABLE workspace CASCADE;
DROP TABLE node CASCADE;
DROP TABLE node_lock CASCADE;
DROP TABLE node_link CASCADE;
DROP TABLE node_replacement CASCADE;