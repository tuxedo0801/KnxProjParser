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
package de.root1.knxprojparser.project;

import de.root1.knxprojparser.GroupAddress;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author achristian
 */
public abstract class AbstractKnxParser <T>{

    final File baseFolder;
    boolean parsed;

    static class GroupAddressContainer {

        private String ga;
        private String name;
        private String refId;

        public GroupAddressContainer(String ga, String name, String refId) {
            this.ga = ga;
            this.name = name;
            this.refId = refId;
        }

        public String getGa() {
            return ga;
        }

        public String getName() {
            return name;
        }

        public String getRefId() {
            return refId;
        }

        @Override
        public String toString() {
            return "GroupAddressContainer{" + "ga=" + ga + ", name=" + name + ", refId=" + refId + '}';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.refId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GroupAddressContainer other = (GroupAddressContainer) obj;
            if (!Objects.equals(this.refId, other.refId)) {
                return false;
            }
            return true;
        }

    }

    private static final Map<String, Unmarshaller> UNMARSHELLER_MAP = new HashMap<>();

    private final URL XSD_PROJECT;
    private final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private final Schema schema;

    final List<de.root1.knxprojparser.GroupAddress> gaList = new ArrayList<>();
    final de.root1.knxprojparser.Project project = new de.root1.knxprojparser.Project();

    AbstractKnxParser(String resource, File f) throws SAXException {
        XSD_PROJECT = AbstractKnxParser.class.getResource(resource);
        schema = SCHEMA_FACTORY.newSchema(XSD_PROJECT);
        this.baseFolder = f;
    }

    private Unmarshaller getCachedUnmarsheller(String name) throws JAXBException {
        Unmarshaller unmarshaller = UNMARSHELLER_MAP.get(name);
        if (unmarshaller == null) {
            unmarshaller = JAXBContext.newInstance(name).createUnmarshaller();
            UNMARSHELLER_MAP.put(name, unmarshaller);
        }
        return unmarshaller;
    }

    <T> T readXML(File xmlDatei, Class<T> clss)
            throws JAXBException, SAXException {
        Unmarshaller unmarshaller = getCachedUnmarsheller(clss.getPackage().getName());
        unmarshaller.setSchema(schema);
        return clss.cast(unmarshaller.unmarshal(xmlDatei));

    }
    
    public abstract void parse() throws ParseException;

    public de.root1.knxprojparser.Project getProject() {
        
        List<GroupAddress> groupaddressList = project.getGroupaddressList();
        groupaddressList.sort(new Comparator<GroupAddress>(){
            @Override
            public int compare(GroupAddress o1, GroupAddress o2) {
                
                String[] ga1 = o1.getAddress().split("/");
                String[] ga2 = o2.getAddress().split("/");
                
                int i1 = Integer.parseInt(ga1[0]) <<11 | Integer.parseInt(ga1[1]) <<8 | Integer.parseInt(ga1[2]);
                int i2 = Integer.parseInt(ga2[0]) <<11 | Integer.parseInt(ga2[1]) <<8 | Integer.parseInt(ga2[2]);
                
                if (i1<i2) {
                    return -1;
                }
                if (i1>i2) {
                    return 1;
                }
                return 0;
            }
            
        });
        return project;
    }
    
    public boolean isParsed() {
        return parsed;
    };
    
    public abstract boolean parserMatch();

}
