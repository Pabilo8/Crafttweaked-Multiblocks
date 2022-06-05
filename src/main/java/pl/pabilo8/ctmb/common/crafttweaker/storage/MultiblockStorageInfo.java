package pl.pabilo8.ctmb.common.crafttweaker.storage;

/**
 * @author Pabilo8
 * @since 30.05.2022
 */
public abstract class MultiblockStorageInfo<T>
{
	public final int id, capacity;
	public final boolean isInput;

	public MultiblockStorageInfo(int id, int capacity, boolean isInput)
	{
		this.id = id;
		this.capacity = capacity;
		this.isInput = isInput;
	}
}
