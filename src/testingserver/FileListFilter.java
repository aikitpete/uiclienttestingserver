package testingserver;

/**
 * Created by IntelliJ IDEA.
 * User: Peter Gerhat
 * Date: May 4, 2010
 * Time: 11:02:27 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import java.io.FilenameFilter;

public class FileListFilter implements FilenameFilter {

    private String extension;

    public FileListFilter(String extension) {
        this.extension = extension;
    }

    public boolean accept(File directory, String filename) {
        boolean fileOK = true;

        if (extension != null) {
            fileOK &= filename.endsWith('.' + extension);
        }
        return fileOK;
    }
}


