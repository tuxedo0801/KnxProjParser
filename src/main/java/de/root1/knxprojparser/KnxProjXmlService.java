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

import de.root1.schema.knxproj._1.KnxProj;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author achristian
 */
class KnxProjXmlService {
    
    private static final URL XSD_KONNEKTINGDEVICE_V0 = KnxProjXmlService.class.getResource("/xsd/KnxProj_1.xsd");
    
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static Schema schema;
    private static final SAXException SCHEMA_EXCEPTION;
    private static final Map<String, Unmarshaller> UNMARSHELLER_MAP = new HashMap<>(); 
    private static final Map<String, Marshaller> MARSHELLER_MAP = new HashMap<>(); 
    
    static {
        SAXException ex = null;
        try {
            schema = SCHEMA_FACTORY.newSchema(XSD_KONNEKTINGDEVICE_V0);
        } catch (SAXException e) {
            ex = e;
        } finally {
            SCHEMA_EXCEPTION = ex;
        }
         
    }
    
    private static Unmarshaller getCachedUnmarsheller(String name) throws JAXBException{
        Unmarshaller unmarshaller = UNMARSHELLER_MAP.get(name);
        if (unmarshaller==null) {
//            System.out.println("Return new unmarshaller: "+name);
            unmarshaller = JAXBContext.newInstance(name).createUnmarshaller();
            UNMARSHELLER_MAP.put(name, unmarshaller);
        }
        return unmarshaller;
    }
    
    private static Marshaller getCachedMarsheller(String name) throws JAXBException{
        Marshaller marshaller = MARSHELLER_MAP.get(name);
        if (marshaller==null) {
//            System.out.println("Return new unmarshaller: "+name);
            marshaller = JAXBContext.newInstance(name).createMarshaller();
            MARSHELLER_MAP.put(name, marshaller);
        }
        return marshaller;
    }

    private static <T> T unmarshal(String xmlDatei, Class<T> clss)
            throws JAXBException, SAXException {
        checkValidSchema();
        Unmarshaller unmarshaller = getCachedUnmarsheller(clss.getPackage().getName());
        unmarshaller.setSchema(schema);
        return clss.cast(unmarshaller.unmarshal(new File(xmlDatei)));

    }

    private static void marshal(String xmlDatei, Object jaxbElement)
            throws JAXBException, SAXException {
        checkValidSchema();
        Marshaller marshaller = getCachedMarsheller(jaxbElement.getClass().getPackage().getName());
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(jaxbElement, new File(xmlDatei));
    }

    public static synchronized KnxProj read(File f) throws JAXBException, SAXException {
        return unmarshal(f.getAbsolutePath(), KnxProj.class);
    }

    public static synchronized void write(File f, KnxProj konnekt) throws JAXBException, SAXException {
        marshal(f.getAbsolutePath(), konnekt);
    }

    public static synchronized void validateWrite(KnxProj jaxbElement) throws SAXException, JAXBException {
        checkValidSchema();
        Marshaller marshaller = getCachedMarsheller(jaxbElement.getClass().getPackage().getName());
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(jaxbElement, new DefaultHandler());

    }

    private static void checkValidSchema() throws JAXBException {
        if (schema==null || SCHEMA_EXCEPTION!=null) {
            throw new JAXBException("Cannot process due to preceding exception", SCHEMA_EXCEPTION);
        }
    }

    
}
