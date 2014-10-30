package com.sam.dbmAuto.impl

import java.util.regex.Matcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.sam.dbmAuto.Constant


class DatabaseModelTxtUtils {

    private final static Logger logger = LoggerFactory.getLogger(DatabaseModelTxtUtils.class)

    private static final int na = 0
    private static final int getTables = 1
    private static final int getTablesDone = 2
    private static final int getTable = 3
    private static final int getTableDone = 4
    private static final int getTableException = -1
    private static final int getDimension = 5
    private static final int getDimensionDone = 6
    private static final int getMeasure = 7
    private static final int getMeasureDone = 8
    private static final int getDistribution = 9
    private static final int getDistributionDone = 10
    private static final int ignoreMeasure = 11
    private static final int getKpi = 20
    private static final int getKpiDone = 21
    private static final int knowDone = 100

    private static final String table = "TABLE"
    private static final String dimension = "DIMENSION"
    private static final String measure = "MEASURE"
    private static final String kpi = "KPI"
    private static final String distribution = "DISTRIBUTION"

    private static final String beginRegExp = ".*<TABLE(\\s|>)"//\s.*<DIMENSION(\s|>)
    private static final String endRegExp = "</TABLE>.*";
    private static final String endXml = '</TABLE>';
    private static final String startXml = '<TABLE>';
    private static final String startXmlBlank = '<TABLE ';
    private static final String columnNumberExp = 'column-number\\s*=\\s*".*?"'//
    private static final String nameExp = '\\sname\\s*=\\s*".*?"'//
    private static final String annotationExp = '\\s*<!--(\\s|.)*?-->'

    private static final String outputColumnNumber = 'column-number="columnNumber"'
    private static final String replaceColumnNumber = 'columnNumber'

    private static final String outputMeasureName = ' name="measureName"'

    private static final String measureContent = '<MEASURE name="measureName" cognos-agg-type="sum" column-number="columnNumber"/>'
    private static final String measureName = 'measureName'
    private static final String columnNumber = 'columnNumber'
    private static final String cognosAggType = 'cognos-agg-type=".*?"'
    private static final String max = 'max'
    private static final String min = 'min'
    private static final String cognosAggTypeMax = 'cognos-agg-type="max"'
    private static final String cognosAggTypeMin = 'cognos-agg-type="min"'

    private boolean refreshOnce = false;
    private String tableName
    private String fileName

    private String outputPath = ''
    private File outputFile

    private Map tableMap
    private def newAddMeasure = [:]
    private int status = na
    StringBuilder sb = new StringBuilder()
    private int currentNumber = 0
    private String key;
    private boolean useDefaultMeasurePattern = true
    private boolean genOTableMeasure = false
    private boolean doGenMeasuer = false
    private String ctlFilePath = ''
    private Map TablePathMap = new HashMap()
    private String suffix = '.gen'

    void initTablePathMap(File file) {
        def tt = getTablePath(file)
        if (tt)
            TablePathMap.putAll(tt)
    }

    Map getTablePath(File file) {
        Map mapTablePath = new HashMap()
        if (file.isDirectory()) {
            file.eachFile {
                Map mapTemp = getTablePath(it)
                if (mapTemp)
                    mapTablePath.putAll(mapTemp)
            }
        } else {
            String pathName = file.name
            int index = file.name.indexOf("_")
            logger.debug file.name + ' = ' + file.path
            mapTablePath.put(pathName, file.path)
        }
        return mapTablePath
    }

    DatabaseModelTxtUtils(String outputPath, String ctlFilePath, String suffix) {
        this.outputPath = outputPath
        this.ctlFilePath = ctlFilePath
        this.suffix = suffix
        this.ctlFilePath.split(",").each {
            logger.debug it
            def file = new File(it)
            initTablePathMap(file)
        }
    }

    private void showTablePathMap() {
        TablePathMap.each {
            logger.debug it.key + ' = ' + it.value
        }
    }

    //write the content to the new file
    private void writeFile(File file = outputFile, String content) {
        file.withWriter { writer -> writer.write content }
    }

    //get the current read status
    private static int getStatus(String content, int status) {
        //        if(isMatch(content, regExp, ))
    }

    //eg. content = '<MEASURE name="measureName" cognos-agg-type="sum" column-number="columnNumber"/>'
    private String genMeasure(HashMap measureMap, String content = measureContent) {
        def newMap = measureMap
        StringBuilder sbMeasure = new StringBuilder();
        Constant.listIgnoreColumn.every() { newMap?.remove(it) }
        newMap?.sort() {a, b ->
            a.index.value.compareTo(b.index..value)
        }.each {
            if (!it.key.endsWith(Constant.dimensionFlagRegExp)) {
                String newContent = content.replaceFirst(columnNumber, it.value.index + '').replaceFirst(measureName, it.key) + '\n'
                if (it.key.toUpperCase().startsWith(max.toUpperCase())) {
                    newContent = newContent.replaceFirst(cognosAggType, cognosAggTypeMax)
                } else if (it.key.toUpperCase().startsWith(min.toUpperCase())) {
                    newContent = newContent.replaceFirst(cognosAggType, cognosAggTypeMin)
                }
                sbMeasure.append(newContent)
            }
        }
        return sbMeasure.toString();
    }

    /**
     * @param databaseModel the File of databaseModel
     * @param doGenMeasure auto generate the measure
     * @param useDefaultMeasurePattern use the default measure pattern see @DatabaseModelUtils.measureContent
     *             otherwise, will get the content from the database model file.
     * @param genOTbaleMeasure generate the o table measure
     */
    private void deal(File databaseModel, boolean doGenMeasure = false, boolean useDefMeasurePattern = true, boolean genOTbMeasure = false, boolean autoAddNewMeasure = false, boolean autoAddNewMeasureOUserTable = false, newDimensionDefinedMap) {
        this.doGenMeasuer = doGenMeasure
        this.useDefaultMeasurePattern = useDefMeasurePattern
        this.genOTableMeasure = genOTbMeasure
        this.fileName = databaseModel.getAbsolutePath()
        StringBuilder sbOut = new StringBuilder();
        def databaseModelMap
        if(autoAddNewMeasure || autoAddNewMeasureOUserTable || newDimensionDefinedMap){
            databaseModelMap = DatabaseModelXmlUtils.getMeasureFromDatabaseModel(databaseModel)
        }
        databaseModel.eachLine {
            def content = it
            if (needDeal(content)) {
                convertContentGenMeasure(content, databaseModelMap, autoAddNewMeasure, autoAddNewMeasureOUserTable, newDimensionDefinedMap)
            } else {
                if (sb.length() > 0) {
                    sbOut.append(sb.toString())
                    sb = new StringBuilder()
                    sbOut.append(content + '\n')
                } else {
                    sbOut.append(content + '\n')
                }
            }
            if (status == getTableDone || status == getDimensionDone || status == getMeasureDone || status == getKpi || status == knowDone || refreshOnce) {
                refreshOnce = false
                sbOut.append(sb.toString())
                sb = new StringBuilder()
            }
        }
        writeFile(new File(outputPath + databaseModel.getName() + suffix), sbOut.toString())
        logger.info getInfo() + "Deal done!"
    }

    private Map getTableMapNew(String tableName) {
        tableName = tableName.toUpperCase()
        if (TablePathMap.containsKey(tableName)) {
            return CtlFileUtils.getTableMapByFilePath(TablePathMap.get(tableName))
        } else {
            logger.error "Not get the ctl file named $tableName"
        }
    }


    private Map getTableMap(String tableName) {
        Map tableMapNew = new HashMap()
        tableName = tableName.toUpperCase()
        if (TablePathMap.containsKey(tableName + CtlFileUtils.userTableNameSuffix)) {
            tableMapNew.put(CtlFileUtils.userTableNameSuffix, CtlFileUtils.getTableMapByFilePath(TablePathMap.get(tableName + CtlFileUtils.userTableNameSuffix)))
        } else {
            logger.error "Not get the table map $tableName" + CtlFileUtils.userTableNameSuffix
        }
        if (TablePathMap.containsKey(tableName + CtlFileUtils.oTableNameSuffix)) {
            tableMapNew.put(CtlFileUtils.oTableNameSuffix, CtlFileUtils.getTableMapByFilePath(TablePathMap.get(tableName + CtlFileUtils.oTableNameSuffix)))
        } else {
            logger.error "Not get the table map $tableName" + CtlFileUtils.oTableNameSuffix
        }
        return tableMapNew
    }

    private String getInfo() {
        return "File: $fileName TableName: $tableName$key "
    }

    private convertContentGenMeasure(String contentInput, databaseModelMap, autoAddNewMeasure, autoAddNewMeasureOUserTable, newDimensionDefinedMap) {
        def content = sb.toString() + contentInput
        def contentMeasureGenerate = ""

        if (isMatch(content, beginRegExp, table) || status == getTable) {//need to get table name
            def tableName = getName(content)
            if (tableName) {
                this.tableName = tableName
                tableMap = getTableMap(tableName)
                if (!tableMap) {
                    status = getTableException
                    refreshOnce = true;
                    logger.error getInfo() + ", not get the table map $tableName"
                } else {
                    status = getTableDone
                    key = CtlFileUtils.userTableNameSuffix
                    logger.debug "Done get tableName $tableName"
                    if(autoAddNewMeasure || autoAddNewMeasureOUserTable || newDimensionDefinedMap) {
                        newAddMeasure = DatabaseModelXmlUtils.getNewAddMeasure(databaseModelMap, tableMap, tableName, autoAddNewMeasure, autoAddNewMeasureOUserTable, newDimensionDefinedMap)
                    }
                }
            } else {
                status = getTable
                logger.debug "Not get tableName $tableName, will continue"
            }
        } else if (status == getTableException) {//if not got the Table, don't do anything.
            refreshOnce = true;
        } else if (isMatch(content, beginRegExp, dimension) || status == getDimension) {//is dimension, and need to get dimension name and the column-number
            def dimensionName = getName(content)
            String columnNumberOld = getColumnNumber(content)
            if (dimensionName && columnNumberOld) {
                status = getDimensionDone
                if (!tableMap.get(key)?.get(dimensionName)?.index) {
                    logger.error(getInfo() + "Not get the table map")
                } else {
                    String columnNumberNew = tableMap.get(key).get(dimensionName)?.index

                    String newContent = ''
                    String addPosition = Constant.PREFIX
                    def tempDimensionName = dimensionName
                    while (true) {
                        if (newAddMeasure.containsKey(tempDimensionName)) {//newDimensionDefinedMap have this key
                            if (newDimensionDefinedMap?.containsKey(newAddMeasure.get(tempDimensionName).get('name'))) {
                                String newGenerateMeasure = newDimensionDefinedMap.get(newAddMeasure.get(tempDimensionName).get('name')).replaceFirst(columnNumberExp, outputColumnNumber.replace(replaceColumnNumber, newAddMeasure.get(tempDimensionName).get('index') + ""))
                                addPosition = newAddMeasure.get(tempDimensionName).get('position')
                                if (Constant.PREFIX.equals(addPosition)) {
                                    newContent = newGenerateMeasure + newContent + '\n'
                                } else if (Constant.SUFFIX.equals(addPosition)) {
                                    newContent += '\n' + newGenerateMeasure
                                }
                                logger.debug("Add new measure $tempDimensionName into the data files!")
                            }
                            tempDimensionName = newAddMeasure.get(tempDimensionName).get('name')
                        } else {
                            break
                        }
                    }

                    if (!columnNumberNew || "".equals(columnNumberNew)) {
                        logger.warn(getInfo() + "dimensionName '$dimensionName' not get the index, content $content")
                    } else if (!columnNumberOld.equals(columnNumberNew) && null != columnNumberNew) {
                        logger.info(getInfo() + "Dimension $dimensionName, columnNumber not match will auto adjust change $columnNumberOld to $columnNumberNew")
                        content = content.replaceFirst(columnNumberExp, outputColumnNumber.replace(replaceColumnNumber, columnNumberNew))
                    } else {
                        logger.debug getInfo() + "Done get dimension $dimensionName"
                    }

                    if (newContent) {
                        if (Constant.PREFIX.equals(addPosition)) {
                            content = newContent + content
                        } else if (Constant.SUFFIX.equals(addPosition)) {
                            content += newContent
                        }
                    }
                }
            } else {
                status = getDimension
                logger.debug getInfo() + "Not get tableName $dimensionName, will continue"
            }
        } else if ((isMatch(content, beginRegExp, measure) || status == getMeasure) && status != ignoreMeasure) {//is measure, and need to get dimension name and the column-number
            boolean doFlag = false;

            if (!doGenMeasuer || key.equals(CtlFileUtils.oTableNameSuffix) && !genOTableMeasure) {//update the Measure
                def dimensionName = getName(content)
                String columnNumberOld = getColumnNumber(content)
                if (dimensionName && columnNumberOld) {
                    status = getDimensionDone
                    if (!tableMap.get(key)?.get(dimensionName)?.index) {
                        logger.warn(getInfo() + "Not get the table map")
                    } else {
                        String columnNumberNew = tableMap.get(key).get(dimensionName).index;
                        String newContent = ''
                        String addPosition = Constant.PREFIX
                        def tempDimensionName = dimensionName
                        while (true) {
                            if ((autoAddNewMeasure || autoAddNewMeasureOUserTable) && newAddMeasure.containsKey(tempDimensionName)) {
                                String newGenerateMeasure = content.replaceFirst(columnNumberExp, outputColumnNumber.replace(replaceColumnNumber, newAddMeasure.get(tempDimensionName).get('index') + "")).replace(tempDimensionName, newAddMeasure.get(tempDimensionName).get('name'))
                                addPosition = newAddMeasure.get(tempDimensionName).get('position')
                                if (Constant.PREFIX.equals(addPosition)) {
                                    newContent = newGenerateMeasure + newContent + '\n'
                                } else if (Constant.SUFFIX.equals(addPosition)) {
                                    newContent += '\n' + newGenerateMeasure
                                }
                                tempDimensionName = newAddMeasure.get(tempDimensionName).get('name')
                                logger.debug("Add new measure $tempDimensionName into the data files!")
                            } else {
                                break
                            }
                        }

                        if (!columnNumberNew || "".equals(columnNumberNew)) {
                            logger.warn(getInfo() + "Measure '$dimensionName' not get the index, content $content")
                        } else if (!columnNumberOld.equals(columnNumberNew)) {
                            logger.info(getInfo() + "Measure $dimensionName, columnNumber not match will auto adjust change $columnNumberOld to $columnNumberNew");
                            content = content.replaceFirst(columnNumberExp, outputColumnNumber.replace(replaceColumnNumber, tableMap.get(key).get(dimensionName).index + ""))
                        } else {
                            logger.debug getInfo() + "Done get dimension $dimensionName"
                        }

                        if (newContent) {
                            if (Constant.PREFIX.equals(addPosition)) {
                                content = newContent + content
                            } else if (Constant.SUFFIX.equals(addPosition)) {
                                content += newContent
                            }
                        }
                    }
                } else {
                    status = getDimension
                    logger.debug getInfo() + "Not get tableName $dimensionName, will continue"
                }
            } else {
                if (useDefaultMeasurePattern) {
                    content = measureContent
                    doFlag = true
                } else {
                    def measureName = getName(content)
                    def columnNumberOld = getColumnNumber(content)
                    if (measureName && columnNumberOld) {
                        content = content.replaceFirst(columnNumberExp, outputColumnNumber).replaceFirst(nameExp, outputMeasureName)
                        doFlag = true
                    }
                }
                if (doFlag) {
                    contentMeasureGenerate = genMeasure(tableMap.get(key), content);
                    refreshOnce = true;
                    status = ignoreMeasure
                } else {
                    status = getMeasure
                }
            }
        } else if (isMatch(content, beginRegExp, distribution) || status == getDistribution) {
            key = CtlFileUtils.oTableNameSuffix
            status = getDistribution
        } else if ((isStartWith(content, startXmlBlank, kpi) || (isStartWith(content, startXml, kpi))) || status == getKpi) {
            status = getKpi
        } else if (isStartWith(content, endXml, distribution)) {
            status = knowDone
        }
        if (status != ignoreMeasure) {
            sb = new StringBuilder(content + "\n")
        } else {
            sb = new StringBuilder(contentMeasureGenerate)
        }

        if (status == ignoreMeasure) {// end element write with the ignore the same line ,need substring the end content
            if (isMatch(content, endRegExp, distribution)) {
                status = knowDone
                sb.append(getMatch(content, endRegExp, distribution));
            }
            if (isMatch(content, endRegExp, kpi)) {
                status = knowDone
                sb.append(getMatch(content, endRegExp, kpi));
            }
        }

    }

    private String getName(String content) {
        return getValue(content, nameExp)
    }

    private String getColumnNumber(String content) {
        return getValue(content, columnNumberExp)
    }

    private String getValue(String content, String nameExp) {
        def pattern = ~nameExp
        Matcher matcher = pattern.matcher(content)
        while (matcher.find()) {
            return matcher.group().split('\\"')[1]
        }
        return null
    }

    private boolean isDone() {
        return status == getTablesDone || status == getTableDone || status == getDimensionDone || status == getMeasureDone || status == getDistributionDone || status == na
    }

    private Boolean needDeal(String content) {
        return isMatch(content, beginRegExp, table) || isMatch(content, beginRegExp, dimension) || isMatch(content, beginRegExp, measure) || isMatch(content, beginRegExp, distribution) || !isDone()
    }

    private Boolean isStartWith(String content, String regExp, String type) {
        String temp = regExp.replace(table, type)
        return content.trim().startsWith(temp)
    }

    private String getMatch(String content, String regExp, String type) {
        String temp = regExp.replace(table, type)
        def pattern = ~temp
        Matcher matcher = pattern.matcher(content)
        while (matcher.find()) {
            return matcher.group();
        }
        return ""
    }

    private Boolean isMatch(String content, String regExp, String type) {
        String temp = regExp.replace(table, type)
        def pattern = ~temp
        Matcher matcher = pattern.matcher(content)
        while (matcher.find()) {
            return true
        }
        return false
    }

}
