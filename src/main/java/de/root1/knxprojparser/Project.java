/*
 * Copyright (C) 2016 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of KnxProjParser.
 *
 *   KnxProjParser is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   KnxProjParser is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with KnxProjParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.knxprojparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class Project {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private List<GroupAddress> groupaddressList = new ArrayList<>();
    private String name;
    private Date lastModified;
    private Date projectStart;
    private String createdBy;
    private String toolVersion;

    public List<GroupAddress> getGroupaddressList() {
        return groupaddressList;
    }

    public void setGroupaddressList(List<GroupAddress> groupaddressList) {
        this.groupaddressList = groupaddressList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getProjectStart() {
        return projectStart;
    }

    public void setProjectStart(Date projectStart) {
        this.projectStart = projectStart;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }
    
    

}
