package com.sam.dbmAuto

/**
 * Unified Assurance (tm)
 * Copyright (C) 2014,  Tektronix Communications, Inc.
 * All rights reserved.
 */
class Constant {

    public static final String PREFIX = 'prefix'
    public static final String SUFFIX = 'suffix'
    public static final String DIMENSION = 'Dimension'
    public static final String MEASURE = 'Measure'

    public static final String dimensionFlagRegExp = "_ID"
    public static final def listIgnoreColumn = [
            "TIME_ID",
            "COUNTER",
            "ERRORCOUNT"
    ]
}
