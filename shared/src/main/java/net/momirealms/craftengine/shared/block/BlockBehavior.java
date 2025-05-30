package net.momirealms.craftengine.shared.block;

import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class BlockBehavior {

    @SuppressWarnings("unchecked")
    public <T extends BlockBehavior> Optional<T> getAs(Class<T> tClass) {
        if (tClass.isInstance(this)) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    public Object rotate(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return superMethod.call();
    }

    public Object mirror(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return superMethod.call();
    }

    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return args[0];
    }

    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    public void onPlace(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return (boolean) superMethod.call();
    }

    public void onBrokenAfterFall(Object thisBlock, Object[] args) throws Exception {
    }

    public void onLand(Object thisBlock, Object[] args) throws Exception {
    }

    public boolean isValidBoneMealTarget(Object thisBlock, Object[] args) throws Exception {
        return false;
    }

    public boolean isBoneMealSuccess(Object thisBlock, Object[] args) throws Exception {
        return false;
    }

    public void performBoneMeal(Object thisBlock, Object[] args) throws Exception {
    }
}
