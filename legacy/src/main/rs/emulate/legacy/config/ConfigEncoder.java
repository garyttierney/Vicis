package rs.emulate.legacy.config;

import rs.emulate.legacy.archive.Archive;
import rs.emulate.legacy.archive.ArchiveEntry;
import rs.emulate.legacy.archive.ArchiveUtils;
import rs.emulate.shared.util.DataBuffer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Encodes data from {@link ArchiveEntry} objects in the {@code config} {@link Archive}.
 *
 * @param <T> The type the data is encoded from.
 * @author Major
 */
public final class ConfigEncoder<T extends MutableConfigDefinition> {

	/**
	 * The List of definitions to encode.
	 */
	protected final List<T> definitions;

	/**
	 * The name of the ArchiveEntry.
	 */
	protected final String name;

	/**
	 * Creates the ConfigEncoder.
	 *
	 * @param name The name of the {@link ArchiveEntry}.
	 * @param definitions The {@link List} of definitions to encode.
	 */
	public ConfigEncoder(String name, List<T> definitions) {
		this.name = name;
		this.definitions = definitions;
	}

	/**
	 * Encodes the {@link List} of {@code T}s into an {@link ArchiveEntry}.
	 *
	 * @return The archive entry.
	 */
	public ArchiveEntry[] encode() {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ByteArrayOutputStream index = new ByteArrayOutputStream();

		int size = definitions.size(), last = 2;
		writeShort(data, size);
		writeShort(index, size);

		for (T definition : definitions) {
			definition.getProperties().stream().filter(this::validProperty).forEach(entry -> write(entry, data));
			data.write(ConfigConstants.DEFINITION_TERMINATOR);

			int change = data.size() - last;
			writeShort(index, change);
			last = data.size();
		}

		int hash = ArchiveUtils.hash(name + ConfigConstants.DATA_EXTENSION);
		ArchiveEntry dataEntry = new ArchiveEntry(hash, DataBuffer.wrap(data.toByteArray()));

		hash = ArchiveUtils.hash(name + ConfigConstants.INDEX_EXTENSION);
		ArchiveEntry indexEntry = new ArchiveEntry(hash, DataBuffer.wrap(index.toByteArray()));

		return new ArchiveEntry[]{ dataEntry, indexEntry };
	}

	/**
	 * Encodes the {@link List} of {@code T}s into an {@link ArchiveEntry}, and returns a new {@link Archive}
	 * containing
	 * the entries in the specified Archive, and the newly created one.
	 *
	 * @param archive The Archive to use as a base.
	 * @return The new Archive.
	 */
	public Archive encodeInto(Archive archive) {
		return archive.addEntries(encode());
	}

	/**
	 * Returns whether or not the specified {@link Map} entry contains a {@link SerializableProperty} that should be
	 * encoded.
	 *
	 * @param entry The Map entry.
	 * @return {@code true} if the property should be encoded, {@code false} if not.
	 */
	private boolean validProperty(Map.Entry<Integer, SerializableProperty<?>> entry) {
		return entry.getValue().valuePresent();
	}

	/**
	 * Writes the specified {@link Map} entry containing the opcode and {@link SerializableProperty} to the specified
	 * {@link ByteArrayOutputStream}.
	 *
	 * @param entry The map entry.
	 * @param os The byte array output stream.
	 */
	private void write(Map.Entry<Integer, SerializableProperty<?>> entry, ByteArrayOutputStream os) {
		DataBuffer buffer = entry.getValue().encode();
		byte[] bytes = buffer.getRemainingBytes();

		os.write(entry.getKey());
		os.write(bytes, 0, bytes.length); // write(byte[]) throws IOException unnecessarily
	}

	/**
	 * Writes a {@code short} (i.e. a 16-bit value) to the specified {@link ByteArrayOutputStream}.
	 *
	 * @param stream The ByteArrayOutputStream.
	 * @param value The value to write.
	 */
	private void writeShort(ByteArrayOutputStream stream, int value) {
		stream.write(value >> 8);
		stream.write(value);
	}

}