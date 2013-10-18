
ALTER TABLE workspace DROP CONSTRAINT workspace_node;
ALTER TABLE node DROP CONSTRAINT node_link_child;
ALTER TABLE NodeLink DROP CONSTRAINT node_link_parent;
DROP TABLE workspace CASCADE;
DROP TABLE node CASCADE;
DROP TABLE node_link CASCADE;
