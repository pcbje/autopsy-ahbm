package com.pcbje.ahbm;

import com.pcbje.ahbm.config.AHBMConfig;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * An ingest module factory that creates file ingest modules that do keyword
 * searching.
 */
@ServiceProvider(service = IngestModuleFactory.class)
public class AhbmIngestModuleFactory extends IngestModuleFactoryAdapter {

    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }

    static String getModuleName() {
        return "Autopsy AHBM";
    }

    @Override
    public String getModuleDescription() {
        return "";
    }

    @Override
    public String getModuleVersionNumber() {
        return "2";
    }

    @Override
    public boolean hasIngestJobSettingsPanel() {
        return true;
    }


    @Override
    public boolean hasGlobalSettingsPanel() {
        return false;
    }

   
    @Override
    public boolean isFileIngestModuleFactory() {
        return true;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new AhbmJobSettings();
    }
    
     /**
     * @inheritDoc
     */
    @Override
    public AHBMConfig getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        assert settings instanceof AhbmJobSettings;

        return new AHBMConfig((AhbmJobSettings) settings);
    }

    @Override
    public FileIngestModule createFileIngestModule(IngestModuleIngestJobSettings settings) {
        assert settings instanceof AhbmJobSettings;
       
        return new AhbmIngestModule((AhbmJobSettings) settings);
    }
}