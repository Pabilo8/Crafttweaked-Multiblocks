package pl.pabilo8.ctmb.common.block.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import pl.pabilo8.ctmb.common.CommonProxy;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.util.ZenPosition;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Based on <a href="https://github.com/CraftTweaker/ContentTweaker/blob/develop/1.12/src/main/java/com/teamacronymcoders/contenttweaker/modules/vanilla/resources/BlockBracketHandler.java">https://github.com/CraftTweaker/ContentTweaker/blob/develop/1.12/src/main/java/com/teamacronymcoders/contenttweaker/modules/vanilla/resources/BlockBracketHandler.java</a>
 *
 * @author Pabilo8
 * @since 16.02.2022
 */
@BracketHandler
@ZenRegister
public class MultiblockBracketHandler implements IBracketHandler
{
	private final IJavaMethod method;

	public MultiblockBracketHandler()
	{
		method = CraftTweakerAPI.getJavaMethod(MultiblockBracketHandler.class, "getMultiblock", String.class);
	}

	@Override
	@Nullable
	public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens)
	{
		//<multiblock:id:name>
		if(tokens.size() >= 4&&"multiblock".equalsIgnoreCase(tokens.get(0).getValue()))
		{
			String name = tokens.get(2).getValue()+":"+tokens.get(4).getValue();
			return new BlockReferenceSymbol(environment, name);
		}

		return null;
	}

	public static Multiblock getMultiblock(String name)
	{
		if(name==null)
			return null;

		return CommonProxy.MULTIBLOCKS.stream()
				.filter(mb -> mb.getUniqueName().equals(name))
				.findFirst()
				.orElse(null);
	}

	private class BlockReferenceSymbol implements IZenSymbol
	{
		private final IEnvironmentGlobal environment;
		private final String name;

		public BlockReferenceSymbol(IEnvironmentGlobal environment, String name)
		{
			this.environment = environment;
			this.name = name;
		}

		@Override
		public IPartialExpression instance(ZenPosition position)
		{
			return new ExpressionCallStatic(position, environment, method, new ExpressionString(position, name));
		}
	}
}
