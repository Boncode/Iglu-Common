package org.ijsberg.iglu.configuration.model;

import java.util.HashSet;
import java.util.Set;

public class WebTrafficSettingsDto {

    private IpAccessPolicy ipAccessPolicy;
    private Set<String> whitelistedIps;
    private Set<String> blacklistedIps;

    public WebTrafficSettingsDto() {
        ipAccessPolicy = IpAccessPolicy.DISABLED;
        whitelistedIps = new HashSet<>();
        blacklistedIps = new HashSet<>();
    }

    public WebTrafficSettingsDto(IpAccessPolicy ipAccessPolicy, Set<String> whitelistedIps, Set<String> blacklistedIps) {
        this.ipAccessPolicy = ipAccessPolicy;
        this.whitelistedIps = whitelistedIps;
        this.blacklistedIps = blacklistedIps;
    }

    public Set<String> getBlacklistedIps() {
        return new HashSet<>(blacklistedIps);
    }

    public Set<String> getWhitelistedIps() {
        return new HashSet<>(whitelistedIps);
    }

    public IpAccessPolicy getIpAccessPolicy() {
        return ipAccessPolicy;
    }

    public void setIpAccessPolicy(IpAccessPolicy ipAccessPolicy) {
        this.ipAccessPolicy = ipAccessPolicy;
    }

    public void setWhitelistedIps(Set<String> whitelistedIps) {
        this.whitelistedIps = whitelistedIps;
    }

    public void setBlacklistedIps(Set<String> blacklistedIps) {
        this.blacklistedIps = blacklistedIps;
    }

    public boolean isAllowed(String ipAddress) {
        switch (ipAccessPolicy) {
            case DISABLED: return true;
            case WHITELISTING: return whitelistedIps.contains(ipAddress);
            // if IP is listed on both whitelist and blacklist access is allowed
            case BLACKLISTING: return whitelistedIps.contains(ipAddress) || !blacklistedIps.contains(ipAddress);
            default: return false;
        }
    }

    public void addToBlackList(String clientIp) {
        synchronized (blacklistedIps) {
            blacklistedIps.add(clientIp);
        }
    }

    @Override
    public String toString() {
        return "WebTrafficSettingsDto{" +
                "ipAccessPolicy=" + ipAccessPolicy +
                ", whitelistedIps=" + whitelistedIps +
                ", blacklistedIps=" + blacklistedIps +
                '}';
    }
}
