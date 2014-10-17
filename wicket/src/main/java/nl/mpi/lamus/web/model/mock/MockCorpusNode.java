/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.web.model.mock;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.archiving.tree.corpusstructure.LinkedCorpusNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockCorpusNode implements LinkedCorpusNode, Serializable {

    private LinkedCorpusNode parent;
    private List<LinkedCorpusNode> children = Collections.emptyList();
    private String name = "";
    private URI nodeUri;
    
    private URI profile;
    private FileInfo fileInfo;
    private CorpusNodeType nodeType;
    private Date lastUpdate;
    private boolean isOnSite;
    private String format;
    private CorpusNode olderVersion;
    private CorpusNode newerVersion;

    public void setChildren(List<LinkedCorpusNode> children) {
	this.children = children;
    }

    @Override
    public LinkedCorpusNode getChild(int index) {
	return children.get(index);
    }

    @Override
    public int getChildCount() {
	return children.size();
    }

    @Override
    public int getIndexOfChild(LinkedTreeNode child) {
	return children.indexOf(child);
    }

    public void setName(String name) {
	this.name = name;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public LinkedCorpusNode getParent() {
	return parent;
    }

    public void setParent(LinkedCorpusNode parent) {
	this.parent = parent;
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public URI getNodeURI() {
	return nodeUri;
    }

    public void setNodeURI(URI nodeURI) {
	this.nodeUri = nodeURI;
    }

    @Override
    public URI getProfile() {
        return profile;
    }
    
    public void setProfile(URI profile) {
        this.profile = profile;
    }

    @Override
    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    @Override
    public CorpusNodeType getType() {
        return nodeType;
    }
    
    public void setType(CorpusNodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean isOnSite() {
        return isOnSite;
    }
    
    public void setIsOnSite(boolean isOnSite) {
        this.isOnSite = isOnSite;
    }

    @Override
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public CorpusNode getOlderVersion() {
        return olderVersion;
    }
    
    public void setOlderVersion(CorpusNode olderVersion) {
        this.olderVersion = olderVersion;
    }

    @Override
    public CorpusNode getNewerVersion() {
        return newerVersion;
    }
    
    public void setNewerVersion(CorpusNode newerVersion) {
        this.newerVersion = newerVersion;
    }
}
