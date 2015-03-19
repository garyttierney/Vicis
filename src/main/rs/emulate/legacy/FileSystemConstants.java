package rs.emulate.legacy;

/**
 * Holds file system related constants.
 * 
 * @author Graham
 */
final class FileSystemConstants {

	/**
	 * The number of archives in cache 0.
	 */
	public static final int ARCHIVE_COUNT = 9;

	/**
	 * The size of a chunk.
	 */
	public static final int CHUNK_SIZE = 512;

	/**
	 * The size of a header.
	 */
	public static final int HEADER_SIZE = 8;

	/**
	 * The size of a block.
	 */
	public static final int BLOCK_SIZE = HEADER_SIZE + CHUNK_SIZE;

	/**
	 * Default private constructor to prevent instantiation.
	 */
	private FileSystemConstants() {

	}

}