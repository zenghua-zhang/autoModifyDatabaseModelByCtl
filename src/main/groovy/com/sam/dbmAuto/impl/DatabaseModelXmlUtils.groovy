package com.sam.dbmAuto.impl

import com.sam.dbmAuto.Constant

class DatabaseModelXmlUtils {


    static void main(String[] args) {
        String databaseModelFile = 'D:/workspace/source_code/iris/releases/V7_13_2_2/server/ivModels/ipi/VoLteSigCallDatabaseModel.xml'
        File file = new File(databaseModelFile)
        def parser = new XmlParser()
        def databaseModel = parser.parse(file)

        databaseModel.value().find {table ->
            println "${table.'@name'}, ${table.MEASURE.size()}, ${table.DISTRIBUTION.MEASURE.size()}"
        }
    }

    public static Map getMeasureFromDatabaseModel(databaseModelFile) {
        def map = [:]
        def parser = new XmlParser()
        def databaseModel = parser.parse(databaseModelFile)
        databaseModel.value().find {table ->
            String tableName = (table.'@name').toUpperCase()
            def measureMap = [:]
            def distributionMeasure = [:]
            (table.DIMENSION + table.MEASURE).each {
                measureMap[it.'@name'] = it.'column-number'
            }
            map[tableName + CtlFileUtils.userTableNameSuffix] = measureMap
            (table.DIMENSION + table.DISTRIBUTION.MEASURE).each {
                distributionMeasure[it.'@name'] = it.'column-number'
            }

            map[tableName + CtlFileUtils.oTableNameSuffix] = distributionMeasure
        }
        return map
    }

    /**
     *
     * @param databaseModelMap
     * @param tableMap
     * @return Map < k , v > , k action column name + key(@see userTableNameSuffix & oTableNameSuffix); value: position(prefix, suffix), name, index
     */
    public static Map getNewAddMeasure(databaseModelMap, tableMap, tableName, autoAddNewMeasure, autoAddNewMeasureOUserTable, newDimensionDefinedMap) {
        def newAddMeasure = [:]
        databaseModelMap.findAll {k, v -> k.contains(tableName)}.each {k, v ->
            if ((autoAddNewMeasure && k.contains(CtlFileUtils.userTableNameSuffix)) || (autoAddNewMeasureOUserTable && k.contains(CtlFileUtils.oTableNameSuffix)) || newDimensionDefinedMap) {
                def tableMeasure = tableMap.get(k.replaceAll(tableName, ""))
                def preKey
                boolean addSuffix = false

                tableMeasure.groupBy { it.key.endsWith('_ID') ? Constant.DIMENSION : Constant.MEASURE }.each {objK, objV ->
                    objV.each {k1, v1 ->
                        if (addSuffix) {
                            newAddMeasure[k1] = [position: Constant.PREFIX, name: preKey, index: v1.index - 1, type: v1.type]
                            addSuffix = false
                        }

                        if (!v.containsKey(k1) && !isIgnoreColumn(k1)) {
                            if (preKey == k1 || !preKey) {
                                addSuffix = true
                            } else {
                                addSuffix = false
                                newAddMeasure[preKey] = [position: Constant.SUFFIX, name: k1, index: v1.index, type: v1.type]
                            }
                            preKey = k1
                        } else if (!addSuffix && !isIgnoreColumn(k1)) {
                            preKey = k1
                        } else {
                            preKey = null
                        }
                    }

                    /*tableMeasure.each {k1, v1 ->
                        if (addSuffix) {
                            newAddMeasure[k1] = [position: Constant.PREFIX, name: preKey, index: v1.index - 1, type: v1.type]
                            addSuffix = false
                        }

                        if (!v.containsKey(k1) && !isIgnoreColumn(k1)) {
                            if (preKey == k1 || !preKey) {
                                addSuffix = true
                            } else {
                                addSuffix = false
                                newAddMeasure[preKey] = [position: Constant.SUFFIX, name: k1, index: v1.index, type: v1.type]
                            }
                            preKey = k1
                        } else if (!addSuffix && !isIgnoreColumn(k1)) {
                            preKey = k1
                        } else {
                            preKey = null
                        }
                    }*/
                }
            }
        }
        return newAddMeasure
    }

    private static boolean isIgnoreColumn(k) {
        return /* k.endsWith(Constant.dimensionFlagRegExp) || */ Constant.listIgnoreColumn.contains(k)
    }
}
