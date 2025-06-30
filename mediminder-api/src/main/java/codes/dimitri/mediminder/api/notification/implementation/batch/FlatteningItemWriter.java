package codes.dimitri.mediminder.api.notification.implementation.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class FlatteningItemWriter<T> implements ItemWriter<List<T>> {
    private final ItemWriter<T> writer;

    @Override
    public void write(Chunk<? extends List<T>> chunk) throws Exception {
        for (List<T> ts : chunk) {
            writer.write(new Chunk<>(ts));
        }
    }
}
