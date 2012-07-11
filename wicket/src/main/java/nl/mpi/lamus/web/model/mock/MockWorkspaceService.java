package nl.mpi.lamus.web.model.mock;

import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWorkspaceService implements WorkspaceService {

    private static Logger logger = LoggerFactory.getLogger(MockWorkspaceService.class);
    private final Workspace workspace;

    public MockWorkspaceService(Workspace workspace) {
	logger.info("call to constructor MockWorkspaceService({})", workspace);
	this.workspace = workspace;
    }

    public Workspace createWorkspace(String userID, int archiveNodeID) {
	logger.info("call to createWorkspace({}, {})", userID, archiveNodeID);
	return workspace;
    }

    public void submitWorkspace(int workspaceID) {
	logger.info("call to submitWorkspace({})", workspaceID);
    }
}
