/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public enum WorkspaceStatus {

    /**
     * The request is unitialised, the object was just created. IR / workspaces
     * only return to this state if DataMoverIn cannot complete the init
     * sequence. The IR should then be deleted manually in most cases, after
     * reading log4j logs. Newly created IR have this state as default state.
     */
    UNINITIALISED,
    // NOTE: IR is in a limbo between creation and initialiseWSFIR?
    /**
     * The request is initialising, workspace not yet finished: Set by
     * LAMSController doImportArchiveNode shortly after creating a new IR
     * (initialiseWorkSpaceForIngestRequest) LAMSController loops in
     * lcWaitForImport until state is changed to INITIALISED by DataMoverIn.
     */
    INITIALISING,
    /**
     * The request is initialised, workspace is finished The IR / WS is now in
     * use. Can change to SLEEPING in IngestRequestDBImpl and can change back in
     * 'select existing workspace' (LAMSController/WorkSpaceManager
     * checkWorkSpaceForIngestRequest changes to INITIALISED)
     * ingestrequestchoice.jsp shows only INITIALISED and SLEEPING IR.
     */
    INITIALISED,
    /**
     * The owner is not connected, the request is dormant The SLEEPING state is
     * only reached from INITIALISED state in IngestRequestDBImpl, but corpman
     * can put IR in this state manually to flag error conditions as solved,
     * using RequestEdit. ingestrequestchoice.jsp shows only INITIALISED and
     * SLEEPING IR.
     */
    SLEEPING,
    /**
     * The request was submitted by the user (will move workspace to archive)
     * Workspaces will move to PENDING_ARCHIVE_DB_UPDATE and DATA_MOVED_SUCCESS
     * or DATA_MOVED_ERROR from this state. The latter two are final states. If
     * a workspace / IR is stuck in SUBMITTED state, corpman can try to reset
     * the state (for example to SLEEPING) and try to submit again, after
     * checking the log4j logs for the reason of the submit failure.
     */
    SUBMITTED,
    /**
     * The request timed out: If SLEEPING and had a non-null end date which was
     * too long ago, the regular CheckIngestRequestTimeOut will block the
     * request in question and set it to CLOSED_TIMEOUT: corpman can unblock or
     * delete the affected request after that, using RequestEdit. Changing state
     * from timed out to SLEEPING in RequestEdit updates the end date.
     */
    CLOSED_TIMEOUT,
    /**
     * The request was refused, see the message field for a reason Can be set
     * manually via RequestEdit or by DataMoverIn. The latter can happen if the
     * user cannot write the top node. IR in this state should be deleted after
     * a while.
     */
    REFUSED,
    /**
     * actually means that the ingest / submit is ready. Before AMS2, this
     * status only meant that the filesystem was up to date (not the databases).
     * Requests in this state are not shown as active requests in the corpman
     * console request list (Requests.jsp). This is a final IR state.
     */
    DATA_MOVED_SUCCESS,
    /**
     * there was an error moving data to the archive: Set by DataMoverOut when a
     * workspace / IR cannot move on from SUBMITTED into DATA_MOVED_SUCCESS
     * Leave IR state manually, or delete the stuck IR.
     */
    DATA_MOVED_ERROR,
    /**
     * data was successfully moved from the workspace to the archive BUT the
     * archive crawler and AMS2 still have to recalculate the archive database
     * contents which describe the updated part...! If IR gets stuck here, use
     * ArchiveCrawler and AMS2 manually and leave this IR state or delete the IR
     * afterwards.
     */
    PENDING_ARCHIVE_DB_UPDATE;
}