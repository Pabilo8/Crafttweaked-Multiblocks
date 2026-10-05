package pl.pabilo8.ctmb.common.storage;

import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

/**
 * Sided dust transport for II DustStacks; amounts use II's mB convention.
 */
public interface IDustHandler
{
	int fill(DustStack stack, boolean doFill);

	DustStack drain(DustStack stack, boolean doDrain);

	DustStack drainDust(int amount, boolean doDrain);
}
