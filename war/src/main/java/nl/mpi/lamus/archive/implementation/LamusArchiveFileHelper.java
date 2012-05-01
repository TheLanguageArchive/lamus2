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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusArchiveFileHelper implements ArchiveFileHelper {
    
    //TODO based on the class 'ArchiveUtils' from the old Lamus
    
    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveFileHelper.class);

    /** Used by correctPathElement to truncate unreasonably long names */
    private final int maxDirectoryNameLength = 100;
    
    /** Name of the directories where unlinked files of a corpus are stored */
    private final String orphansDirName = "sessions";
    
    /**
     * 
     * @param fullname
     * @return the offset of the last slash in fullname
     */
    private int lastSlashPos(String fullname) {
        int lastSlash = fullname.lastIndexOf('/');
        int lastBackslash = fullname.lastIndexOf('\\');
        if (lastSlash > lastBackslash) return lastSlash;
        return lastBackslash;
    }
    
    /**
     * @see ArchiveFileHelper#getFileBasename(java.lang.String) 
     */
    public String getFileBasename(String fullname) {
        int sp = lastSlashPos(fullname);
        if ((sp+1)==fullname.length()) logger.warn("getFileBasename: None for "+fullname);
        return fullname.substring(sp+1);
    }

    /**
     * @see ArchiveFileHelper#getFileTitle(java.lang.String) 
     */
    public String getFileTitle(String fullname) {
        String name = "";
        if (fullname.length()!=(lastSlashPos(fullname)+1)) name = getFileBasename(fullname);
        if (name.length()==0) {
            name = fullname.replaceAll("^[a-zA-Z0-9]+:[/]+", ""); // remove protocol / schema part
            int preSlash = name.indexOf('/'); // if easy, clip to domain name
            if (preSlash > 0) name = name.substring(0, preSlash);
            name = correctPathElement(name, "getFileTitle"); // remove slashes etc
            name = name.replaceAll("[.]", "_"); // avoid the impression of having a file name extension
        }
        return name;
    }
    
    /**
     * @see ArchiveFileHelper#correctPathElement(java.lang.String, java.lang.String) 
     */
    public String correctPathElement(String pathElement, String reason) {
        String temp = pathElement.replaceAll("\\&[^;]+;","_"); // replace xml variables
        // 20..2c: space ! " # $ % &amp;  ' ( ) * +   3a..40: : ; &lt; = > ? @
        // 5b..60: [ \\ ] ^ _    7b..7f: { | } ~
        // temp=temp.replaceAll("[\\x00-\\x2C\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\xFF]", "_"); // replace special chars
        // temp=temp.replaceAll("[\\u0100-\\uffff]", "_"); // replace special chars
        // safe minimal MPI names may only contain [A-Za-z0-9._-], but URI can also contain
        // ! ~ * ' ( ) "unreserved" and : @ &amp; = + $ , "reserved minus ; / ?" at many
        // places. Reserved ; / ? : @ &amp; = + $ , have special meaning, see RFC2396.
        // Whitespace  is never allowed in URI, but %nn hex escapes can often be used.
        temp = temp.replaceAll("[^A-Za-z0-9._-]", "_"); // replace all except known good chars
        
        String result = temp;
        
        // in case the pathElement already contained "__" (without any invalid characters), it should stay unchanged
        if(!temp.equals(pathElement)) {
	        Pattern pat = Pattern.compile("__");  // shorten double replacements
	        Matcher mat = pat.matcher(temp);
	        while (mat.find(0)) mat.reset(mat.replaceFirst("_"));
	        result = mat.replaceFirst("_");
        }
        
        if (result.length()>maxDirectoryNameLength) { // truncate but try to keep extension
            int dot = result.lastIndexOf('.');
            String suffix = "...";
            if (dot>=0 && (result.length()-dot)<=7) // at most '.123456'
                suffix += result.substring(dot); // otherwise: no extension to preserve!
            // archivable files all have (2) / 3 / 4 char extensions, '.class' has 5 chars
            result = result.substring(0, maxDirectoryNameLength-suffix.length()) + suffix; // suffix.length: 3..10
        }
        if (!result.equals(pathElement)) {
            if ("getFileTitle".equals(reason)) { // log noise reduction ;-)
                logger.info("correctPathElement: "+reason+": "+pathElement+" -> "+result);
            } else {
                logger.warn("correctPathElement: "+reason+": "+pathElement+" -> "+result);
            }
        }
        return result;

    }

    /**
     * @see ArchiveFileHelper#getOrphansDirectory(nl.mpi.util.OurURL) 
     */
    public File getOrphansDirectory(OurURL topNodeURL) {
        String topNodePath=topNodeURL.getPath();
        int index=topNodePath.indexOf("/Corpusstructure/");
        File orphansFolder = null;
        if(index>-1) 
            orphansFolder=new File(topNodePath.substring(0,index+1)+orphansDirName);
        else {
            File temp=new File(topNodePath);
            while((orphansFolder==null) && (temp!=null)) {
                File cs=new File (temp,"Corpusstructure");
                if(cs.exists() && cs.isDirectory()) 
                    orphansFolder=new File(temp,orphansDirName);
                temp=temp.getParentFile();
            }
        }
        return orphansFolder; 
    }
    
    /**
     * @see ArchiveFileHelper#getOrphansDirectoryName() 
     */
    public String getOrphansDirectoryName() {
        return this.orphansDirName;
    }
    
}
