package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class SelfIncreaseIntTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final int min;
    private final int max;
    private int current;

    public SelfIncreaseIntTemplateArgument(int min, int max) {
        this.min = min;
        this.max = max;
        this.current = min;
    }

    @Override
    public String get(Map<String, TemplateArgument> arguments) {
        String value = String.valueOf(this.current);
        if (this.current < this.max) this.current += 1;
        return value;
    }

    @Override
    public Key type() {
        return TemplateArguments.SELF_INCREASE_INT;
    }

    public int current() {
        return this.current;
    }

    public int min() {
        return this.min;
    }

    public int max() {
        return this.max;
    }

    public static class Factory implements TemplateArgumentFactory {
        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            int from = ResourceConfigUtils.getAsInt(arguments.get("from"), "from");
            int to = ResourceConfigUtils.getAsInt(arguments.get("to"), "to");
            if (from > to) throw new LocalizedResourceConfigException("warning.config.template.argument.self_increase_int.invalid_range", String.valueOf(from), String.valueOf(to));
            return new SelfIncreaseIntTemplateArgument(from, to);
        }
    }
}
