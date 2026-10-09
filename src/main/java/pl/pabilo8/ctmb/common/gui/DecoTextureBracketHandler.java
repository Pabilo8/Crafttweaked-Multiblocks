package pl.pabilo8.ctmb.common.gui;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
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
public class DecoTextureBracketHandler implements IBracketHandler
{
	private final IJavaMethod method;

	public DecoTextureBracketHandler()
	{
		method = CraftTweakerAPI.getJavaMethod(DecoTextureBracketHandler.class, "getTexture", String.class);
	}

	@Override
	@Nullable
	public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens)
	{
		// <deco:name> or <deco:namespace:name>, resolved when the script runs.
		if(tokens.size() >= 3&&":".equals(tokens.get(1).getValue())&&"deco".equalsIgnoreCase(tokens.get(0).getValue()))
		{
			String name = tokens.subList(2, tokens.size()).stream().map(Token::getValue).collect(java.util.stream.Collectors.joining());
			return new BlockReferenceSymbol(environment, name);
		}

		return null;
	}

	/**
	 * <b>DO NOT REMOVE</b><br>
	 * Used in constructor via string reference
	 */
	@SuppressWarnings("unused")
	public static DecoTexture getTexture(String name)
	{
		return DecoTextures.find(name);
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
