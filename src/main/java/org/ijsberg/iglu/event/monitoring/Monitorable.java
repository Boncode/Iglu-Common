package org.ijsberg.iglu.event.monitoring;

import java.util.List;

public interface Monitorable {
    List<MonitorStatusMessage> getMessages();
}
