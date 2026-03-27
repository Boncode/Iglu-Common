package org.ijsberg.iglu.event.monitoring;

import org.ijsberg.iglu.event.model.BasicEventDto;
import org.ijsberg.iglu.event.model.BasicEvent;
import org.ijsberg.iglu.event.model.EventType;

public class MonitorEvent extends BasicEvent {

    enum MonitorEventType implements EventType {
        MONITOR_EVENT;

        @Override
        public String getId() {
            return name();
        }

        @Override
        public String getLabel() {
            return BasicEvent.getEventTypeLabel(this);
        }
    }

    private String remoteTopicId;
    private String remoteEventTypeId;
    private String remoteSystemId;
    private String remoteAssetId;
    private String remoteMessage;

    public MonitorEvent(String remoteTopicId, BasicEventDto sourceEventDto, String remoteSystemId, String monitorAssetId) {
        super(MonitorEventType.MONITOR_EVENT, sourceEventDto.getTimestampUtc(), monitorAssetId);
        this.remoteTopicId = remoteTopicId;
        this.remoteEventTypeId = sourceEventDto.getEventTypeId();
        this.remoteSystemId = remoteSystemId;
        this.remoteAssetId = sourceEventDto.getAssetId();
        this.remoteMessage = sourceEventDto.getMessage();
    }

    public String getRemoteTopicId() {
        return remoteTopicId;
    }

    public String getRemoteEventTypeId() {
        return remoteEventTypeId;
    }

    public String getRemoteSystemId() {
        return remoteSystemId;
    }

    public String getRemoteAssetId() {
        return remoteAssetId;
    }

    public String getRemoteMessage() {
        return remoteMessage;
    }

    @Override
    public String toString() {
        return "MonitorEvent{" +
            "remoteTopicId='" + remoteTopicId + '\'' +
            ", remoteEventTypeId='" + remoteEventTypeId + '\'' +
            ", remoteSystemId='" + remoteSystemId + '\'' +
            ", remoteAssetId='" + remoteAssetId + '\'' +
            '}';
    }
}
