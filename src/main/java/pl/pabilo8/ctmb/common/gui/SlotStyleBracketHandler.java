package pl.pabilo8.ctmb.common.gui;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;
import java.util.List;
import java.util.stream.Collectors;

/** Resolves <slotstyle:vanilla> to a common-side handle without loading native Deco classes. */
@BracketHandler
@ZenRegister
public final class SlotStyleBracketHandler implements IBracketHandler
{
	private final IJavaMethod method = CraftTweakerAPI.getJavaMethod(SlotStyleBracketHandler.class, "getStyle", String.class);

	@Override
	public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens)
	{
		if(tokens.size()<3||!"slotstyle".equalsIgnoreCase(tokens.get(0).getValue())||!":".equals(tokens.get(1).getValue())) return null;
		String name = tokens.subList(2, tokens.size()).stream().map(Token::getValue).collect(Collectors.joining());
		SlotStyle.find(name);
		return position -> new ExpressionCallStatic(position, environment, method, new ExpressionString(position, name));
	}
	/** Used by the compiled bracket expression. */

	public static SlotStyle getStyle(String name)
	{
		return SlotStyle.find(name);
	}
}
