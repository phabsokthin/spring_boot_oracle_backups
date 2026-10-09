package com.spring_oracle.spring_oracle_tutorial.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oracle.backup")
public class BackupProperties {

    private boolean enabled = true;
    private String schema = "SYSTEM";
    private String cron = "0 * * * * *";
    private String directory = "DATA_PUMP_DIR";
    private String localFolder = "./backups";
    private int maxBackups = 5;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }

    public String getLocalFolder() { return localFolder; }
    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public int getMaxBackups() { return maxBackups; }
    public void setMaxBackups(int maxBackups) { this.maxBackups = maxBackups; }
}