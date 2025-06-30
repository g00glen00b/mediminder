package codes.dimitri.mediminder.api.notification.implementation.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FlatteningItemProcessor<T, U> implements ItemProcessor<List<T>, List<U>> {
    private final ItemProcessor<T, U> processor;

    @Override
    public List<U> process(List<T> items) throws Exception {
        List<U> list = new ArrayList<>();
        for (T item : items) {
            U process = processor.process(item);
            if (process != null) list.add(process);
        }
        return list;
    }
}
