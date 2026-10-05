package pl.pabilo8.ctmb.common.storage;

import pl.pabilo8.immersiveintelligence.api.DustTank;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

/**
 * Fixes bounded filling in the released II tank without altering II's classes.
 */
public final class CTMBDustTank extends DustTank
{
	private final Runnable changed;

	public CTMBDustTank(int capacity, Runnable changed)
	{
		super(capacity);
		this.changed = changed;
	}

	@Override
	public int fill(DustStack resource, boolean doFill)
	{
		if(resource==null||resource.isEmpty()||resource.amount <= 0||!dustStack.canMergeWith(resource)) return 0;
		int accepted = Math.min(resource.amount, Math.max(0, capacity-dustStack.amount));
		if(doFill&&accepted > 0)
		{
			dustStack = new DustStack(dustStack.isEmpty()?resource.name: dustStack.name, dustStack.amount+accepted);
			changed.run();
		}
		return accepted;
	}

	@Override
	public DustStack drain(int amount, boolean doDrain)
	{
		DustStack result = super.drain(amount, doDrain);
		if(doDrain&&!result.isEmpty()) changed.run();
		return result;
	}

	@Override
	public DustStack drain(DustStack resource, boolean doDrain)
	{
		if(resource==null||resource.isEmpty()||!dustStack.name.equals(resource.name)) return DustStack.getEmptyStack();
		return drain(resource.amount, doDrain);
	}

	public void restore(DustStack stack)
	{
		dustStack = stack==null||stack.amount <= 0||stack.name.isEmpty()?DustStack.getEmptyStack(): new DustStack(stack.name, Math.min(capacity, stack.amount));
	}
}
