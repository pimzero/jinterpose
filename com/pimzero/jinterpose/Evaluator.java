package com.pimzero.jinterpose;

import java.lang.IllegalArgumentException;
import java.util.regex.Pattern;
import com.pimzero.jinterpose.Proto;

public class Evaluator {
	static public boolean eval(Proto.Matcher_expr expr, Proto.Matcher val) {
		switch (expr.getExprCase()) {
			case MATCH:
				var cmp = expr.getMatch();
				boolean out = true;
				if (cmp.hasClassname())
				       out = out && cmp.getClassname().equals(val.getClassname());
				if (cmp.hasMethodname())
					out = out && cmp.getMethodname().equals(val.getMethodname());
				if (cmp.hasClassnameRegex())
					out = out && Pattern.compile(cmp.getClassnameRegex()).matcher(val.getClassname()).matches();
				return out;
			case OR:
				for (var i: expr.getOr().getExprList()) {
					if (eval(i, val))
						return true;
				}
				return false;
			case AND:
				for (var i: expr.getAnd().getExprList()) {
					if (!eval(i, val))
						return false;
				}
				return true;
			case NOT:
				return !eval(expr.getNot(), val);
		}
		return true;
	}
}
