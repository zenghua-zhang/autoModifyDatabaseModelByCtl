package com.sam.dbmAuto.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CtlFileUtils {

    private String ctlFilePath
    private final static String userTableNameSuffix = "_15_USER$fileSuffixes"
    private final static String oTableNameSuffix = "_15_O_USER$fileSuffixes"
    private final static String fileSuffixes = ".ctl"
    private final static Logger logger = LoggerFactory.getLogger(CtlFileUtils.class)

    CtlFileUtils(String ctlFilePath) {
        this.ctlFilePath = ctlFilePath
    }

    public static Map getTableMapByFilePath(String filePath) {
        File userTableFile = new File(filePath)
        if (!userTableFile.exists()) {
            logger.error("Table name ${userTableFile.getName()} not exists at ${userTableFile.getAbsolutePath()}")
        } else {
            return getMapByCtlFilePath(userTableFile)
        }
    }

    /**
     *
     * @param tableName
     * [oTableNameSuffix: o table map, userTableNameSuffix: user table map]
     * @return
     */
    private Map getTableMap(String tableName) {//two map
        tableName = tableName.toUpperCase()
        def map = new HashMap()
        File userTableFile = new File(ctlFilePath + tableName + userTableNameSuffix)
        if (!userTableFile.exists()) {
            logger.error("Table name ${userTableFile.getName()} not exists at ${userTableFile.getAbsolutePath()}")
        } else {
            map.put(userTableNameSuffix, getMapByCtlFilePath(userTableFile))
        }
        userTableFile = new File(ctlFilePath + tableName + oTableNameSuffix)
        if (!userTableFile.exists()) {
            logger.error("Table name $tableName$userTableNameSuffix not exists!")
        } else {
            map.put(oTableNameSuffix, getMapByCtlFilePath(userTableFile))
        }
        return map
    }

    private static Map getMapByCtlFilePath(File ctlFile) {
        def map = new HashMap()
        def beginFlag = 'TRAILING NULLCOLS'
        def i = 1
        def begin = false
        ctlFile.eachLine {
            if (begin && it.length() > 5) {
                def list = (it.replaceAll(",", "").split(" ") - '')
                if(list.length>2){
                    map.put(list[0], [index: i++, type: list[1], value: list[2]])
                } else if(list.length == 2){
                    map.put(list[0], [index: i++, type: list[1], value: ''])
                } else {
                    map.put(list[0], [index: i++, type: '', value: ''])
                }
            }
            if (begin || it.contains(beginFlag)) {
                begin = true
            }
        }

        return map.sort() { a, b ->
            a.value.index.compareTo(b.value.index)
        }
    }
}
