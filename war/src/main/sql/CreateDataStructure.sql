CREATE TABLE workspace (
	workspace_id                SERIAL                          NOT NULL,
	user_id                     varchar(30)                     NOT NULL,
	top_node_id                 integer,
	start_date                  timestamp(6) with time zone     NOT NULL,
	end_date                    timestamp(6) with time zone,
	session_start_date          timestamp(6) with time zone     NOT NULL,
	session_end_date            timestamp(6) with time zone,
	used_storage_space          bigint,
	max_storage_space           bigint,
	status                      varchar(255)                    NOT NULL,
        message                     varchar(255)                    NOT NULL,
        archive_info                varchar(255),
	PRIMARY KEY (workspace_id));
	
CREATE TABLE node (
	workspace_node_id           SERIAL                          NOT NULL,
	workspace_id                integer                         NOT NULL,
	archive_node_id             integer,
	profile_schema_uri          varchar(255),
	name                        varchar(255)                    NOT NULL,
	title                       varchar(255),
        type                        varchar(255),                   NOT NULL,
	workspace_url               varchar(255),
	archive_url                 varchar(255),
	origin_url                  varchar(255),
	status                      varchar(255)                    NOT NULL,
	pid                         varchar(255),
	format                      varchar(255)                    NOT NULL,
	PRIMARY KEY (workspace_node_id));
	
CREATE TABLE node_link (
	parent_workspace_node_id    integer                         NOT NULL,
	workspace_node_id           integer                         NOT NULL,
	resource_proxy_id           varchar(255)                    NOT NULL),
        PRIMARY KEY (parent_workspace_node_id, workspace_node_id));


ALTER TABLE node
    ADD CONSTRAINT workspace_node
        FOREIGN KEY (workspace_id)
        REFERENCES workspace (workspace_id);

ALTER TABLE node_link
    ADD CONSTRAINT node_link_parent
        FOREIGN KEY (parent_workspace_node_id)
        REFERENCES node (workspace_node_id);
ALTER TABLE node_link
    ADD CONSTRAINT node_link_child
        FOREIGN KEY (workspace_node_id)
        REFERENCES node (workspace_node_id);
