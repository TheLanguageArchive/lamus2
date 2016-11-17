CREATE TABLE workspace (
	workspace_id                SERIAL                          NOT NULL,
	user_id                     varchar                         NOT NULL,
	top_node_id                 integer,
    top_node_archive_uri        varchar,
    top_node_archive_url        varchar,
	start_date                  timestamp(6) with time zone     NOT NULL,
	end_date                    timestamp(6) with time zone,
	session_start_date          timestamp(6) with time zone     NOT NULL,
	session_end_date            timestamp(6) with time zone,
	used_storage_space          bigint,
	max_storage_space           bigint,
	status                      varchar                    NOT NULL,
    message                     varchar                    NOT NULL,
    crawler_id                  varchar,
	PRIMARY KEY (workspace_id));

CREATE TABLE pre_lock (
        archive_uri                 varchar,
        PRIMARY KEY (archive_uri));
	
CREATE TABLE node (
	workspace_node_id           SERIAL                          NOT NULL,
	workspace_id                integer                         NOT NULL,
	profile_schema_uri          varchar,
	name                        varchar                    NOT NULL,
	title                       varchar,
    type                        varchar                    NOT NULL,
	workspace_url               varchar,
	archive_uri                 varchar,
    archive_url                 varchar,
	origin_url                  varchar,
	status                      varchar                    NOT NULL,
    protected                   boolean                         NOT NULL,
	format                      varchar,
	PRIMARY KEY (workspace_node_id));

CREATE TABLE node_lock (
        archive_uri                 varchar,
        workspace_id                integer                         NOT NULL,
        PRIMARY KEY (archive_uri));
	
CREATE TABLE node_link (
	parent_workspace_node_id    integer                         NOT NULL,
	child_workspace_node_id     integer                         NOT NULL,
        PRIMARY KEY (parent_workspace_node_id, child_workspace_node_id));

CREATE TABLE node_replacement (
        old_node_id         integer                         NOT NULL,
        new_node_id         integer                         NOT NULL,
        PRIMARY KEY (old_node_id, new_node_id));


ALTER TABLE node
    ADD CONSTRAINT workspace_node
        FOREIGN KEY (workspace_id)
        REFERENCES workspace (workspace_id);

ALTER TABLE node_lock
    ADD CONSTRAINT node_lock_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspace (workspace_id);

ALTER TABLE node_link
    ADD CONSTRAINT node_link_parent
        FOREIGN KEY (parent_workspace_node_id)
        REFERENCES node (workspace_node_id);
ALTER TABLE node_link
    ADD CONSTRAINT node_link_child
        FOREIGN KEY (child_workspace_node_id)
        REFERENCES node (workspace_node_id);

ALTER TABLE node_replacement
    ADD CONSTRAINT node_replacement_old
        FOREIGN KEY (old_node_id)
        REFERENCES node (workspace_node_id);
ALTER TABLE node_replacement
    ADD CONSTRAINT node_replacement_new
        FOREIGN KEY (new_node_id)
        REFERENCES node (workspace_node_id);
