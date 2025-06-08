package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TemplateManager extends Manageable {

    ConfigParser parser();

    Object applyTemplates(Key id, Object input);

    interface ArgumentString {

        String rawValue();

        Object get(Map<String, TemplateArgument> arguments);
    }

    final class Literal implements ArgumentString {
        private final String value;

        public Literal(String value) {
            this.value = value;
        }

        public static Literal literal(String value) {
            return new Literal(value);
        }

        @Override
        public String rawValue() {
            return this.value;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Literal literal)) return false;
            return this.value.equals(literal.value);
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public String toString() {
            return "Literal(" + this.value + ")";
        }
    }

    final class Placeholder implements ArgumentString {
        private final String placeholder;
        private final String rawText;

        public Placeholder(String placeholder) {
            this.placeholder = placeholder;
            this.rawText = "{" + this.placeholder + "}";
        }

        public static Placeholder placeholder(String placeholder) {
            return new Placeholder(placeholder);
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            TemplateArgument replacement = arguments.get(this.placeholder);
            if (replacement != null) {
                return replacement.get(arguments);
            }
            return rawValue();
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Placeholder that)) return false;
            return this.placeholder.equals(that.placeholder);
        }

        @Override
        public int hashCode() {
            return this.placeholder.hashCode();
        }

        @Override
        public String toString() {
            return "Placeholder(" + this.placeholder + ")";
        }
    }

    final class Complex2 implements ArgumentString {
        private final String rawText;
        private final ArgumentString arg1;
        private final ArgumentString arg2;

        public Complex2(String rawText, ArgumentString arg1, ArgumentString arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.rawText = rawText;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            Object arg1 = this.arg1.get(arguments);
            Object arg2 = this.arg2.get(arguments);
            if (arg1 == null && arg2 == null) return null;
            if (arg1 == null) return String.valueOf(arg2);
            if (arg2 == null) return String.valueOf(arg1);
            return String.valueOf(arg1) + arg2;
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Complex that)) return false;
            return this.rawText.equals(that.rawText);
        }

        @Override
        public int hashCode() {
            return this.rawText.hashCode();
        }

        @Override
        public String toString() {
            return "Complex2(" + this.rawText + ")";
        }
    }

    final class Complex implements ArgumentString {
        private final List<ArgumentString> parts;
        private final String rawText;

        public Complex(String rawText, List<ArgumentString> parts) {
            this.parts = parts;
            this.rawText = rawText;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            StringBuilder result = new StringBuilder();
            boolean hasValue = false;
            for (ArgumentString part : this.parts) {
                Object arg = part.get(arguments);
                if (arg != null) {
                    result.append(arg);
                    hasValue = true;
                }
            }
            if (!hasValue) return null;
            return result.toString();
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Complex that)) return false;
            return this.rawText.equals(that.rawText);
        }

        @Override
        public int hashCode() {
            return this.rawText.hashCode();
        }

        @Override
        public String toString() {
            return "Complex(" + this.rawText + ")";
        }
    }

    static ArgumentString preParse(String input) {
        if (input == null || input.isEmpty()) {
            return Literal.literal("");
        }
        int n = input.length();
        int lastAppendPosition = 0; // 追踪上一次追加操作结束的位置
        int i = 0;

        List<ArgumentString> arguments = new ArrayList<>();
        while (i < n) {
            // 检查当前字符是否为未转义的 '{'
            int backslashes = 0;
            int temp_i = i - 1;
            while (temp_i >= 0 && input.charAt(temp_i) == '\\') {
                backslashes++;
                temp_i--;
            }
            if (input.charAt(i) == '{' && backslashes % 2 == 0) {
                // 发现占位符起点
                int placeholderStartIndex = i;
                // 追加从上一个位置到当前占位符之前的文本
                if (lastAppendPosition < i) {
                    arguments.add(Literal.literal(input.substring(lastAppendPosition, i)));
                }
                // --- 开始解析占位符内部 ---
                StringBuilder keyBuilder = new StringBuilder();
                int depth = 1;
                int j = i + 1;
                boolean foundMatch = false;
                while (j < n) {
                    char c = input.charAt(j);
                    if (c == '\\') { // 处理转义
                        if (j + 1 < n) {
                            keyBuilder.append(input.charAt(j + 1));
                            j += 2;
                        } else {
                            keyBuilder.append(c);
                            j++;
                        }
                    } else if (c == '{') {
                        depth++;
                        keyBuilder.append(c);
                        j++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) { // 找到匹配的结束括号
                            String key = keyBuilder.toString();
                            arguments.add(Placeholder.placeholder(key));

                            // 更新位置指针
                            i = j + 1;
                            lastAppendPosition = i;
                            foundMatch = true;
                            break;
                        }
                        keyBuilder.append(c); // 嵌套的 '}'
                        j++;
                    } else {
                        keyBuilder.append(c);
                        j++;
                    }
                }
                // --- 占位符解析结束 ---
                if (!foundMatch) {
                    // 如果内层循环结束仍未找到匹配的 '}'，则不进行任何特殊处理
                    // 外层循环的 i 会自然递增
                    i++;
                }
            } else {
                i++;
            }
        }
        // 追加最后一个占位符之后的所有剩余文本
        if (lastAppendPosition < n) {
            arguments.add(Literal.literal(input.substring(lastAppendPosition)));
        }
        return switch (arguments.size()) {
            case 1 -> arguments.getFirst();
            case 2 -> new Complex2(input, arguments.get(0), arguments.get(1));
            default -> new Complex(input, arguments);
        };
    }
}
