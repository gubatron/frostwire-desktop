package com.frostwire.search.extractors.js;

import static com.frostwire.search.extractors.js.JavaFunctions.isalpha;
import static com.frostwire.search.extractors.js.JavaFunctions.isdigit;
import static com.frostwire.search.extractors.js.JavaFunctions.join;
import static com.frostwire.search.extractors.js.JavaFunctions.len;
import static com.frostwire.search.extractors.js.JavaFunctions.list;
import static com.frostwire.search.extractors.js.JavaFunctions.reverse;
import static com.frostwire.search.extractors.js.JavaFunctions.splice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsFunction<T> {

    private final String jscode;
    private final Map<String, LambdaN> functions;
    private final LambdaN initial_function;

    public JsFunction(String jscode, String funcname) {
        this.jscode = jscode;
        this.functions = new HashMap<String, LambdaN>();
        this.initial_function = extract_function(funcname);
    }

    @SuppressWarnings("unchecked")
    public T eval(Object[] args) {
        return (T) initial_function.run(args);
    }

    public T eval(Object s) {
        return eval(new Object[] { s });
    }

    private Object interpret_statement(String stmt, final Map<String, Object> local_vars, final int allow_recursion) {
        if (allow_recursion < 0) {
            throw new JsError("Recursion limit reached");
        }

        if (stmt.startsWith("var ")) {
            stmt = stmt.substring("var ".length());
        }

        final Matcher ass_m = Pattern.compile("^(?<out>[a-z]+)(\\[(?<index>.+?)\\])?=(?<expr>.*)$").matcher(stmt);
        Lambda1 assign;
        String expr;
        if (ass_m.find()) {
            if (ass_m.group("index") != null) {
                assign = new Lambda1() {
                    @Override
                    public Object run(Object val) {
                        Object lvar = local_vars.get(ass_m.group("out"));
                        Object idx = interpret_expression(ass_m.group("index"), local_vars, allow_recursion);
                        assert idx instanceof Integer;
                        ((Object[]) lvar)[(Integer) idx] = val;
                        return val;
                    }
                };
                expr = ass_m.group("expr");
            } else {
                assign = new Lambda1() {
                    @Override
                    public Object run(Object val) {
                        local_vars.put(ass_m.group("out"), val);
                        return val;
                    }
                };
                expr = ass_m.group("expr");
            }
        } else if (stmt.startsWith("return ")) {
            assign = new Lambda1() {
                @Override
                public Object run(Object v) {
                    return v;
                }
            };
            expr = stmt.substring("return ".length());
        } else {
            throw new JsError(String.format("Cannot determine left side of statement in %s", stmt));
        }

        Object v = interpret_expression(expr, local_vars, allow_recursion);
        return assign.run(v);
    }

    private Object interpret_expression(String expr, Map<String, Object> local_vars, int allow_recursion) {
        if (isdigit(expr)) {
            return Integer.valueOf(expr);
        }

        if (isalpha(expr)) {
            return local_vars.get(expr);
        }

        Matcher m = Pattern.compile("^(?<in>[a-z]+)\\.(?<member>.*)$").matcher(expr);
        if (m.find()) {
            String member = m.group("member");
            Object val = local_vars.get(m.group("in"));
            if (member.equals("split(\"\")")) {
                return list((String) val);
            }
            if (member.equals("join(\"\")")) {
                return join((Object[]) val);
            }
            if (member.equals("length")) {
                return len(val);
            }
            if (member.equals("reverse()")) {
                return reverse(val);
            }
            Matcher slice_m = Pattern.compile("slice\\((?<idx>.*)\\)").matcher(member);
            if (slice_m.find()) {
                Object idx = interpret_expression(slice_m.group("idx"), local_vars, allow_recursion - 1);
                return splice(val, (Integer) idx);
            }
        }

        m = Pattern.compile("^(?<in>[a-z]+)\\[(?<idx>.+)\\]$").matcher(expr);
        if (m.find()) {
            Object val = local_vars.get(m.group("in"));
            Object idx = interpret_expression(m.group("idx"), local_vars, allow_recursion - 1);
            return ((Object[]) val)[(Integer) idx];
        }

        m = Pattern.compile("^(?<a>.+?)(?<op>[%])(?<b>.+?)$").matcher(expr);
        if (m.find()) {
            Object a = interpret_expression(m.group("a"), local_vars, allow_recursion);
            Object b = interpret_expression(m.group("b"), local_vars, allow_recursion);
            return (Integer) a % (Integer) b;
        }

        m = Pattern.compile("^(?<func>[a-zA-Z]+)\\((?<args>[a-z0-9,]+)\\)$").matcher(expr);
        if (m.find()) {
            String fname = m.group("func");
            if (!functions.containsKey(fname)) {
                functions.put(fname, extract_function(fname));
            }
            List<Object> argvals = new ArrayList<Object>();
            for (String v : m.group("args").split(",")) {
                if (isdigit(v)) {
                    argvals.add(Integer.valueOf(v));
                } else {
                    argvals.add(local_vars.get(v));
                }
            }
            return functions.get(fname).run(argvals.toArray());
        }
        throw new JsError(String.format("Unsupported JS expression %s", expr));
    }

    private LambdaN extract_function(String funcname) {
        final Matcher func_m = Pattern.compile("function " + Pattern.quote(funcname) + "\\((?<args>[a-z,]+)\\)\\{(?<code>[^\\}]+)\\}").matcher(jscode);
        func_m.find();
        final String[] argnames = func_m.group("args").split(",");

        LambdaN resf = new LambdaN() {
            @Override
            public Object run(Object[] args) {
                Map<String, Object> local_vars = new HashMap<String, Object>();
                for (int i = 0; i < argnames.length; i++) {
                    local_vars.put(argnames[i], args[i]);
                }
                Object res = null;
                for (String stmt : func_m.group("code").split(";")) {
                    res = interpret_statement(stmt, local_vars, 20);
                }
                return res;
            }
        };

        return resf;
    }
}
