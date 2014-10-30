package com.sam.dbmAuto.impl

class VersionInfo {

	static final int    MAJOR   = 13
	static final int    MINOR   = 02
	static final int    UPDATE  = 25
	static final String PATCH   = 'P0'

	static final String VERSION = "${MAJOR}.${MINOR}.${UPDATE}_${PATCH}"

	/**
	 * Build the version string.
	 * @return version string
	 */
	static String getVersion( ) { VERSION }

	private VersionInfo( ) { }
}
