package com.pcbje.ahbm;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 *
 * @author pcbje
 */
public class AhbmJobSettings implements IngestModuleIngestJobSettings {
    private final boolean skipKnownGood;
    private final boolean againstExisting;
    private final int readBufferSize;
    private final int maxFileSize;

    public AhbmJobSettings(boolean againstExisting, boolean skipKnownGood, int maxFileSize, int readBufferSize) {
        this.againstExisting = againstExisting;
        this.skipKnownGood = skipKnownGood;
        this.maxFileSize = maxFileSize;
        this.readBufferSize = readBufferSize;
        
    }

    public AhbmJobSettings() {
        againstExisting = false;
        skipKnownGood = true;
        maxFileSize = 64;
        readBufferSize = 1024;
    }
    
    @Override
    public long getVersionNumber() {
        return 2;
    }

    /**
     * @return the skipKnownGood
     */
    public boolean isSkipKnownGood() {
        return skipKnownGood;
    }

    /**
     * @return the againstExisting
     */
    public boolean isAgainstExisting() {
        return againstExisting;
    }

    /**
     * @return the readBufferSize
     */
    public int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * @return the maxFileSize
     */
    public int getMaxFileSize() {
        return maxFileSize;
    }
    
    public int getMaxFileSizeInBytes() {
        return maxFileSize * 1024 * 1024;
    }
}
