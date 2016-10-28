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

import de.root1.knxprojparser.project.AbstractKnxParser;
import de.root1.knxprojparser.project.ParseException;
import de.root1.knxprojparser.project.Project11;
import de.root1.knxprojparser.project.Project12;
import de.root1.knxprojparser.project.Project13;
import de.root1.schema.knxproj._1.EtsDefined;
import de.root1.schema.knxproj._1.KnxProj;
import de.root1.schema.knxproj._1.ObjectFactory;
import de.root1.schema.knxproj._1.UserDefined;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Thread.interrupted;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author achristian
 */
public class KnxProjParser {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Class[] AVAILABLE_PARSERS = {
        Project11.class,
        Project12.class,
        Project13.class
    };

    private enum ExportProcess {
        /**
         * file does not exist and needs to be created
         */
        NEW, /**
         * file exists, but is not up2date
         */
        UPDATE
    }

    private AbstractKnxParser parser;
    private final File knxprojFile;

    public KnxProjParser(File knxprojFile) throws FileNotFoundException {
        if (!knxprojFile.exists()) {
            throw new FileNotFoundException("File does not exist: " + knxprojFile.getAbsolutePath());
        }
        this.knxprojFile = knxprojFile;
    }

    /**
     * parses the project. This might take some time ...
     *
     * @throws IOException
     * @throws JDOMException
     */
    public void parse() throws IOException, FileNotSupportedException, ParseException {

        File knxProjFolder;

        // check if it's required to extract
        if (knxprojFile.isFile()) {
            knxProjFolder = Utils.createTempDirectory();
            log.debug("Extracting to {}", knxProjFolder.getCanonicalPath());
            Utils.extract(knxprojFile, knxProjFolder);
        } else {
            knxProjFolder = knxprojFile;
            log.debug("Using already extracted project file:  {}", knxProjFolder.getAbsolutePath());
        }

        // search for matching parser
        for (int i = 0; i < AVAILABLE_PARSERS.length; i++) {
            try {
                Constructor constructor = AVAILABLE_PARSERS[i].getConstructor(File.class);
                parser = (AbstractKnxParser) constructor.newInstance(knxProjFolder);
                if (parser.parserMatch()) {
                    break;
                } else {
                    parser = null;
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.error("Error instantiating parser", ex);
            }
        }

        if (parser != null) {
            log.debug("parser found: {}", parser.getClass().getName());
            parser.parse();
        } else {
            throw new FileNotSupportedException("The given knx project is not supported: " + knxprojFile.getAbsolutePath());
        }

        if (knxprojFile.isFile()) {
            log.debug("Deleting temp files {}", knxProjFolder.getAbsolutePath());
            Path directory = Paths.get(knxProjFolder.toURI());
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    log.debug("delete file: {}", file);
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    log.debug("del direte: {}", dir);
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
            log.debug("Deleting temp files *DONE*");
        }
    }

    public Project getProject() {
        if (parser == null || !parser.isParsed()) {
            return null;
        }
        return parser.getProject();
    }

    public void exportXml(File outfile) throws ExportException {
        String newChecksum = "notAvailable";
        if (knxprojFile.isFile()) {
            try {
                newChecksum = Utils.createChecksum(knxprojFile);
            } catch (NoSuchAlgorithmException | IOException ex) {
                log.warn("Cannot create checksum for file " + knxprojFile.getAbsolutePath(), ex);
            }
        }
        log.debug("newChecksum={}", newChecksum);

        KnxProj knxproj = null;

        if (outfile.exists() && outfile.length() > 0) {

            log.info("outfile exists.");
            try {
                // read old file
                knxproj = KnxProjXmlService.read(outfile);
                String oldChecksum = knxproj.getEtsDefined().getChecksum();
                log.debug("oldChecksum={}", oldChecksum);
                if (!oldChecksum.equals(newChecksum)) {
                    log.info("Existing outfile has DIFFERENT checksum. Update required.");
                } else {
                    log.info("Existing outfile has SAME checksum. No operation required.");
                    /* !!! RETURN due to no operation required !!! */
                    return;
                }

            } catch (JAXBException | SAXException ex) {
                log.warn("Error reading file " + outfile.getAbsolutePath() + ". Forcing new file.", ex);
            }

        } else {
            log.debug("outfile does not exist or is empty. Creating one ..");
            knxproj = createNewKnxProj();
        }

        // at this stage it's clear, that project has to be parsed, either due to update or new
        if (parser == null || !parser.isParsed()) {
            log.debug("Parsing ...");
            try {
                parse();
            } catch (ParseException | IOException | FileNotSupportedException ex) {
                throw new ExportException("Error parsing project", ex);
            }
            log.debug("Parsing ... *DONE*");
        }

        Project parsed = getProject();
        EtsDefined etsDefined = knxproj.getEtsDefined();
        UserDefined userDefined = knxproj.getUserDefined();
        etsDefined.setChecksum(newChecksum);
        de.root1.schema.knxproj._1.Project project = etsDefined.getProject();

        // setting project information
        project.setCreatedBy(parsed.getCreatedBy());
        project.setToolVersion(parsed.getToolVersion());
        project.setName(parsed.getName());
        try {
            if (parsed.getLastModified() != null) {
                project.setLastModified(Utils.dateToXmlDateTime(parsed.getLastModified()));
            }
            if (parsed.getProjectStart() != null) {
                project.setProjectStarted(Utils.dateToXmlDateTime(parsed.getProjectStart()));
            }
        } catch (DatatypeConfigurationException ex) {
            log.warn("Cannot convert XmlDateTime", ex);
        }

        // setting GAs
        List<de.root1.schema.knxproj._1.GroupAddress> gaList = etsDefined.getGroupAddresses().getGroupAddress();

        List<de.root1.schema.knxproj._1.GroupAddress> gaListIncomplete = etsDefined.getIncompleteAddresses().getGroupAddress();

        gaList.clear();
        gaListIncomplete.clear();
        boolean etsKonnektingGaFound = false;
        for (GroupAddress ga : parsed.getGroupaddressList()) {

            de.root1.schema.knxproj._1.GroupAddress insertGa = new de.root1.schema.knxproj._1.GroupAddress();

            if (ga.getAddress().equals("15/7/255")) {
                etsKonnektingGaFound = true;
            }
            
            insertGa.setAddress(ga.getAddress());
            insertGa.setName(ga.getName());
            insertGa.setDPT(ga.getDPT());

            if (insertGa.getDPT() == null || insertGa.getDPT().isEmpty() || insertGa.getDPT().equals("0.000")) {
                insertGa.setDPT("");
                gaListIncomplete.add(insertGa);
            } else {
                gaList.add(insertGa);
            }
        }
        
        boolean userKonnektingGaFound = false;
        for(de.root1.schema.knxproj._1.GroupAddress ga : userDefined.getGroupAddresses().getGroupAddress()) {
            if (ga.getAddress().equals("15/7/255")) {
                userKonnektingGaFound =true;
                break;
            }
        }
        
        if (!etsKonnektingGaFound && !userKonnektingGaFound) {
            de.root1.schema.knxproj._1.GroupAddress konnektingGa = new de.root1.schema.knxproj._1.GroupAddress();
            konnektingGa.setName("KONNEKTING.Programming");
            konnektingGa.setDPT("60000.60000");
            konnektingGa.setAddress("15/7/255");
            konnektingGa.setComment("created by "+props.getProperty("name","KnxProjParser"));
            userDefined.getGroupAddresses().getGroupAddress().add(konnektingGa);
        }

        try {
            KnxProjXmlService.write(outfile, knxproj);
            log.debug("Exported to {}", outfile.getAbsolutePath());
        } catch (JAXBException | SAXException ex) {
            throw new ExportException("Error writing file " + outfile.getAbsolutePath(), ex);
        }

    }

    private KnxProj createNewKnxProj() {
        ObjectFactory factory = new ObjectFactory();
        KnxProj knxproj = factory.createKnxProj();

        EtsDefined etsdefined = factory.createEtsDefined();
        etsdefined.setGroupAddresses(factory.createGroupAddresses());
        etsdefined.setIncompleteAddresses(factory.createIncompleteAddresses());
        etsdefined.setProject(factory.createProject());

        UserDefined userdefined = factory.createUserDefined();
        userdefined.setGroupAddresses(factory.createGroupAddresses());

        knxproj.setEtsDefined(etsdefined);
        knxproj.setUserDefined(userdefined);
        return knxproj;
    }

    private static final Properties props;

    static {
        InputStream resourceAsStream = KnxProjParser.class.getResourceAsStream("/application.properties");
        props = new Properties();
        try {
            props.load(resourceAsStream);
        } catch (IOException ex) {
        }
    }
    
    static class Dots implements Runnable {

        @Override
            public void run() {
                while (!interrupted()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.print(".");
                }
            }
        
    }

    public static void main(String[] args) throws FileNotFoundException, ExportException, IOException, FileNotSupportedException, ParseException {
        System.out.println("["+props.getProperty("name", "KnxProjParser")+"]");
        File f = new File(args[0]);
        
        if (Boolean.getBoolean("supppressFilePath")) {
            System.out.print("Reading " + f.getName());
        } else {
            System.out.print("Reading " + f.getAbsolutePath());
        }
        Thread t0 = new Thread(new Dots());
        t0.start();
        KnxProjParser parser = new KnxProjParser(new File(args[0]));
        t0.interrupt();
        System.out.println(" OK");
        
        System.out.print("Parsing ");
        Thread t1 = new Thread(new Dots());
        t1.start();
        parser.parse();
        t1.interrupt();
        System.out.println(" OK");
        
        System.out.print("Exporting ");
        Thread t2 = new Thread(new Dots());
        t2.start();
        parser.exportXml(new File(args[0] + ".parsed.xml"));
        t2.interrupt();
        System.out.println(" OK");
        
        System.out.println("DONE!");
        System.out.println("");
    }

}
