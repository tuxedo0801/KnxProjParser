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

import java.util.Objects;


/**
 *
 * @author achristian
 */
public class GroupAddress {
    private final String ga;
    private final String name;
    private final String dpt;

    public GroupAddress(String ga, String name, String dpt) {
        this.ga = ga;
        this.name = name;
        this.dpt = dpt;
    }

    /**
     * Get textual representation of group address, f.i. 1/1/100
     * @return address
     */
    public String getAddress() {
        return ga;
    }

    /**
     * Get the name of this groupaddress as defined in ETS. If not defined, address is returned
     * @return ga name
     */
    public String getName() {
        return name!=null?name:getAddress();
    }

    public String getDPT() {
        return dpt;
    }

    @Override
    public String toString() {
        return "GroupAddress{" + "ga=" + ga + ", name=" + name + ", dpt=" + dpt + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.ga);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupAddress other = (GroupAddress) obj;
        if (!Objects.equals(this.ga, other.ga)) {
            return false;
        }
        return true;
    }
    
    

}
