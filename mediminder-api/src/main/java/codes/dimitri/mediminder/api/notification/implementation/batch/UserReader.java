package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class UserReader extends AbstractPagingItemReader<UserDTO> {
    private final UserManager userManager;

    @Override
    protected void doReadPage() {
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();
        var pageRequest = PageRequest.of(getPage(), getPageSize());
        Page<UserDTO> page = userManager.findAll(pageRequest);
        results.addAll(page.getContent());
    }
}
