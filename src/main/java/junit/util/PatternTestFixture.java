package junit.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PatternTestFixture<I, O> {

    private final String description;

    private final I Input;

    private final O output;

    private PatternTestFixture(String description, I input, O output) {
        this.description = description;
        Input = input;
        this.output = output;
    }

    public I getInput() {
        return Input;
    }

    public O getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return description;
    }

    public static class Builder<I, O> implements FirstStep<I, O>, SecondStep<I, O>, ThirdStep<O> {

        private final Class<I> inputClass;

        private final Class<O> outputClass;

        private final List<PatternTestFixture<I, O>> patternList = new ArrayList<>();

        private String description;

        private I input;

        public Builder(Class<I> inputClass, Class<O> outputClass) {
            this.inputClass = inputClass;
            this.outputClass = outputClass;
        }

        public List<PatternTestFixture<I, O>> getResult() {
            return Collections.unmodifiableList(patternList);
        }

        @Override
        public SecondStep<I, O> def(String description) {
            this.description = description;
            return this;
        }

        @Override
        public ThirdStep<O> when(Consumer<I> consumer) {
            I input = newInstanceOfInput();
            consumer.accept(input);
            this.input = input;
            return this;
        }

        @Override
        public void then(Consumer<O> consumer) {
            O output = newInstanceOfOutput();
            consumer.accept(output);
            patternList.add(new PatternTestFixture<>(description, input, output));
        }

        @Override
        public void thenEmptyOutput() {
            patternList.add(new PatternTestFixture<>(description, input, newInstanceOfOutput()));
        }

        private I newInstanceOfInput() {
            try {
                return inputClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Default constructor not found of " + inputClass.getSimpleName(), e);
            }
        }

        private O newInstanceOfOutput() {
            try {
                return outputClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Default constructor not found of " + outputClass.getSimpleName(), e);
            }
        }
    }

    public interface FirstStep<I, O> {

        SecondStep<I, O> def(String description);
    }

    public interface SecondStep<I, O> {

        ThirdStep<O> when(Consumer<I> consumer);
    }

    public interface ThirdStep<O> {

        void then(Consumer<O> consumer);

        void thenEmptyOutput();
    }
}
