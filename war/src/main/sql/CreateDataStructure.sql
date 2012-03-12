
CREATE TABLE Workspace (
	WorkspaceID			SERIAL							NOT NULL,
	UserID				varchar(30)	NOT NULL,
	TopNodeID			integer							NOT NULL,
	StartDate			timestamp(6) with time zone		NOT NULL,
	EndDate				timestamp(6) with time zone		NOT NULL,
	SessionStartDate	timestamp(6) with time zone		NOT NULL,
	SessionEndDate		timestamp(6) with time zone		NOT NULL,
	UsedStorageSpace	bigint,
	MaxStorageSpace		bigint,
	Status				integer,
	ArchiveInfo			varchar(255),
	NodeWorkspaceID		integer,
	PRIMARY KEY (WorkspaceID));
	
CREATE TABLE Node (
	NodeID				integer				NOT NULL,
	WorkspaceID			integer				NOT NULL,
	ArchiveNodeID		integer,
	ProfileSchemaURI	varchar(255),
	Name				varchar(255)		NOT NULL,
	Title				varchar(255),
	WorkspaceURL		varchar(255),
	ArchiveURL			varchar(255),
	OriginURL			varchar(255),
	Status				integer				NOT NULL,
	PID					varchar(255),
	Format				varchar(255),
	PRIMARY KEY (NodeID, WorkspaceID));
	
CREATE TABLE NodeLink (
	ParentNodeID		integer			NOT NULL,
	NodeWorkspaceID		integer			NOT NULL,
	ResourceProxyID		integer			NOT NULL);
	
ALTER TABLE Workspace ADD CONSTRAINT TopNode FOREIGN KEY (TopNodeID, NodeWorkspaceID) REFERENCES Node (NodeID, WorkspaceID);

ALTER TABLE Node ADD CONSTRAINT FKNode812606 FOREIGN KEY (WorkspaceID) REFERENCES Workspace (WorkspaceID);

ALTER TABLE NodeLink ADD CONSTRAINT FKNodeLink495772 FOREIGN KEY (ParentNodeID, NodeWorkspaceID) REFERENCES Node (NodeID, WorkspaceID);
