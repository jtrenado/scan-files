package jtrenado.oldscanfiles.application.consumers;

import jtrenado.oldscanfiles.application.dto.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.function.Consumer;

@Slf4j
@Component
public class ChecksumConsumer implements Consumer<Task> {
    @Override
    public void accept(Task task) {
        log.debug("Processing hash {}", task.getPath());

        String hash = DigestUtils.md5DigestAsHex(task.getContents());
        task.setHash(hash);
        task.setSize(task.getContents().length);

        log.debug("Completing hash {}: {}", task.getPath(), task.getHash());
    }
}
