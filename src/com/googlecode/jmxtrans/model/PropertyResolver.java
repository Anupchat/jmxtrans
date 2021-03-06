package com.googlecode.jmxtrans.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

/***
 *
 * Property Resolver
 *
 * @author henri
 *
 */
public class PropertyResolver {

	private static PropertyResolverFunc RESOLVE_PROPERTIES = new PropertyResolverFunc();

	/**
	 * Resolve a property from System Properties (aka ${key}) key:defval is
	 * supported and if key not found on SysProps, defval will be returned
	 *
	 * @param s
	 * @return resolved string or null if not found in System Properties and no
	 *         defval
	 */
	private static String resolveString(String s) {

		int pos = s.indexOf(":", 0);

		if (pos == -1)
			return (System.getProperty(s));

		String key = s.substring(0, pos);
		String defval = s.substring(pos + 1);

		String val = System.getProperty(key);

		if (val != null)
			return val;
		else
			return defval;
	}

	/**
	 * Parse a String and replace vars a la ant (${key} from System Properties
	 * Support complex Strings like :
	 *
	 * "${myhost}" "${myhost:w2}" "${mybean:defbean}.${mybean2:defbean2}"
	 *
	 * @param s
	 * @return resolved String
	 */
	public static String resolveProps(@Nullable String s) {
		if (s == null) {
			return null;
		}

		int ipos = 0;
		int pos = s.indexOf("${", ipos);

		if (pos == -1)
			return s;

		StringBuilder sb = new StringBuilder();

		while (ipos < s.length()) {
			pos = s.indexOf("${", ipos);

			if (pos < 0) {
				sb.append(s.substring(ipos));
				break;
			}

			if (pos != ipos)
				sb.append(s.substring(ipos, pos));

			int end = s.indexOf('}', pos);

			if (end < 0)
				break;

			int start = pos + 2;
			pos = end + 1;

			String key = s.substring(start, end);
			String val = resolveString(key);

			if (val != null)
				sb.append(val);
			else
				sb.append("${").append(key).append("}");

			ipos = end + 1;
		}

		return (sb.toString());
	}

	/**
	 * Parse Map and resolve Strings value with resolveProps
	 */
	@CheckReturnValue
	public static void resolveMap(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof String)
				map.put(entry.getKey(), resolveProps((String) entry.getValue()));
		}
	}

	/**
	 * Parse List and resolve Strings value with resolveProps
	 */
	@CheckReturnValue
	public static ImmutableList<String> resolveList(List<String> list) {
		return from(list)
				.transform(RESOLVE_PROPERTIES)
				.toList();
	}

	private static class PropertyResolverFunc implements Function<String, String> {
		@Nullable
		@Override
		public String apply(@Nullable String input) {
			return resolveProps(input);
		}
	}
}
