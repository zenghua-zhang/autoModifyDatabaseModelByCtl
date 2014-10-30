package com.sam.dbmAuto.impl

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang3.StringUtils

class Config {

    private static volatile Config instance
    private ConfigObject general
    private Pattern p = Pattern.compile('\\s*|\\t|\\r|\\n')

    private Config() {
    }

    /**
     * Obtain the config singleton.
     */
    static Config getInstance() {
        if (!instance) {
            instance = new Config()
            instance.load(null)
        }
        return instance
    }

    static Config getInstance(String configFileName) {
        if (!instance) {
            instance = new Config()
            instance.load(configFileName)
        }
        return instance
    }

    private void load(String configFileName) {
        if (!configFileName) {
            configFileName = 'conf/databasemodel-config-samples.groovy'
        }

        general = new ConfigSlurper().parse(new File(configFileName).toURL())
        if (!general) {
            throw new Exception("configuration file was not loaded ${configFileName}")
        }
    }

    /**
     * Trim the space and line break
     * @param str
     * @return
     */
    private String trim(def str) {
        if (str instanceof String) {
            Matcher m = p.matcher(str)
            return m.replaceAll('')
        } else {
            if (!str) {
                return null
            } else {
                return str
            }
        }
    }

    private String showConfigurations() {
        StringBuilder sb = new StringBuilder()
        this.general.toString()
    }

    public String getDatabaseModels() { return this.trim(general.dmCheck.files.databaseModels) }

    public def getDatabaseModelsPattern() { return this.trim(general.dmCheck.files.databaseModelsPattern) }

    public String getDatabaseModelPath() { return this.trim(general.dmCheck.paths.databaseModelPath) }

    public String getGenOutputPath() {
        String genOutPutPath = this.trim(general.dmCheck.paths.genOutputPath)
        return StringUtils.isBlank(genOutPutPath) ? getDatabaseModels() : genOutPutPath
    }

    public Map getTablePathMap() { return general.dmCheck.paths.tablePathMap }

    public String getCtlFilePath() { return this.trim(general.dmCheck.paths.ctlFilePath) }

    public String getSuffix() { return trim(general.dmCheck.paths.suffix) }

    public boolean getUseDefaultMeasurePattern() { return general.dmCheck.parameter.useDefaultMeasurePattern }

    public boolean getDoGenMeasure() { return general.dmCheck.parameter.doGenMeasuer }

    public boolean getGenOTableMeasure() { return general.dmCheck.parameter.genOTableMeasure }

    public boolean getAutoAddNewMeasureUserTable() { return general.dmCheck.parameter.autoAddNewMeasureUserTable }

    public boolean getAutoAddNewMeasureOUserTable() { return general.dmCheck.parameter.autoAddNewMeasureOUserTable }

    public Map getNewDimensionMap() { return general.dmCheck.parameter.newDimensionMap }
}
