package com.sam.dbmAuto.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class)

    static void main(String[] args) {
        logger.info '-' * 80
        logger.info "KPI Generator ${VersionInfo.getVersion()}"
        def cli = new CliBuilder(usage: '-[cmd] [filename]')
        cli.with {
            posix: true
            header: "Auto verify the database model and modify the column-number"
            h longOpt: 'help', 'Show usage information'
            c longOpt: 'config', required: false, args: 1, argName: 'fileName', 'The config file name path'
        }

        def options = cli.parse(args)
        if (!options) {
            logger.error('invalid command line')
            cli.usage()
            return
        }

        if (options.h || !options.c) {
            cli.usage()
            return
        }

        //        def totalElapsed = 0
        logger.info "Using config file: ${options.c}"
        logger.info '-' * 80

        logger.info(Config.getInstance(options.c).showConfigurations())

        DatabaseModelTxtUtils dmu = new DatabaseModelTxtUtils(Config.getInstance(options.c).getGenOutputPath(), Config.getInstance(options.c).getCtlFilePath(), Config.getInstance(options.c).getSuffix())
        dmu.showTablePathMap()

        Config.getInstance(options.c).getDatabaseModels()?.split(",").each {
            logger.info("start gening for specifical models $it")
            dmu.deal(new File(Config.getInstance(options.c).getDatabaseModelPath() + it), Config.getInstance(options.c).getDoGenMeasure(),
                    Config.getInstance(options.c).getUseDefaultMeasurePattern(), Config.getInstance(options.c).getGenOTableMeasure(),
                    Config.getInstance(options.c).getAutoAddNewMeasureUserTable(), Config.getInstance(options.c).getNewDimensionMap())
            logger.info("$it , gen done!")
        }

        def p = Config.getInstance(options.c).getDatabaseModelsPattern()
        if (p) {
            new File(Config.getInstance(options.c).getDatabaseModelPath()).eachFileMatch(~p) {
                logger.info("start gening for partten models $it")
                dmu.deal(it, Config.getInstance(options.c).getDoGenMeasure(),
                        Config.getInstance(options.c).getUseDefaultMeasurePattern(), Config.getInstance(options.c).getGenOTableMeasure(),
                        Config.getInstance(options.c).getAutoAddNewMeasureUserTable(), Config.getInstance(options.c).getNewDimensionMap())
                logger.info("$it , gen done!")
            }
        }
        //        logger.info "--- Total Time Elapsed: ${totalElapsed} seconds ---"
    }
}
