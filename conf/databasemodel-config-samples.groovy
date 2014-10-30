dmCheck {
    files {
        //option 1, specify the models name, could defined multi such as: MobileIuPSMMDatabaseModel.xml,GmMediaXrDatabaseModel.xml.example
        databaseModels = '''VoLteCmdDatabaseModel.xml'''//,GmMediaXrDatabaseModel.xml.example,GmSigCallDatabaseModel.xml.example,GmSigCmdDatabaseModel.xml.example'
        //Option 2, Specify the models name with pattern VoLteSig
        databaseModelsPattern = ~/.*?DatabaseModel\.xml.*/
    }
    
    paths {
        databaseModelPath = 'C:/gitSource/server/ivModels/ipi/'
        ctlFilePath = """C:/gitSource/ipi/preprocessor/etc/ctl,
                        C:/gitSource/ipi/voicePreprocessor/etc/ctl/
                    """//,
        //If this not defined, will generate the files with same path with $databaseModelPath
        genOutputPath = 'C:/gitSource/server/ivModels/ipi_gen/'
        //generate file suffix, if not defined will, change the files directly.
        suffix = ''
    }
    
    parameter {
        /*
        @Deprecated the following 3 parameters
        //Generate the measures which not include by the database models.
        doGenMeasuer = false
        //Generate the measures with the default measure pattern as: <MEASURE name="measureName" cognos-agg-type="sum" column-number="columnNumber"/>
        useDefaultMeasurePattern = false
        //Auto generate the distribute table measures.
        genOTableMeasure = false*/
        //Auto add new measure for user table
        autoAddNewMeasureUserTable = false
        //Auto add new measure for distribution table, need some more test.
        autoAddNewMeasureOUserTable = false
        //Auto add new dimension for all user table and distribution table
        /*newDimensionMap = ['DIMENSIONF_ID': '''        <DIMENSION name="DIMENSIONF_ID" key-name="ENODEB_ID" alarmable="true"
                   isElementDimension="true" keyCacheName="node" table-name="CONF_NODE_GROUP_VIEW"
                   column-name="NODE_ID" column-number="columnNumber" />
                   ''']*/
    }
}
