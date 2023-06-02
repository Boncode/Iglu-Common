package org.ijsberg.iglu.monitoring;

import java.util.List;

public interface Monitorable {
    List<StatusMessage> getMessages();
}
