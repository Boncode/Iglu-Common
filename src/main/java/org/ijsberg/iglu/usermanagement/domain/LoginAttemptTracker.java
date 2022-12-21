package org.ijsberg.iglu.usermanagement.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.ijsberg.iglu.util.time.TimeSupport.MINUTE_IN_MS;
import static org.ijsberg.iglu.util.time.TimeSupport.nowMinus;

public class LoginAttemptTracker {

    private class LoginAttemptRecord {

        private Date firstAttemptTime = new Date();
        private int nrOfAttempts = 0;

        private void reset() {
            firstAttemptTime = new Date();
            nrOfAttempts = 0;
        }

        private boolean evaluate() {
            if(firstAttemptTime.before(nowMinus(attemptWindowInMinutes * MINUTE_IN_MS))) {
                reset();
                return false;
            }
            if(nrOfAttempts >= failedAttemptsThresholdForLocking) {
                return true;
            }
            return false;
        }

    }

    private int failedAttemptsThresholdForLocking = 5;
    private int attemptWindowInMinutes = 15;

    private final Map<String, LoginAttemptRecord> loginAttemptRecords = new HashMap<>();

    public void reportLoginAttemptFailed(String sessionId) {
        LoginAttemptRecord loginAttemptRecord = loginAttemptRecords.get(sessionId);
        if(loginAttemptRecord == null) {
            loginAttemptRecord = new LoginAttemptRecord();
            loginAttemptRecords.put(sessionId, loginAttemptRecord);
        }
        loginAttemptRecord.evaluate();
        loginAttemptRecord.nrOfAttempts++;
    }

    public void reportLoginAttemptSucceeded(String sessionId) {
        loginAttemptRecords.remove(sessionId);
    }

    public boolean isLocked(String sessionId) {
        LoginAttemptRecord loginAttemptRecord = loginAttemptRecords.get(sessionId);
        if(loginAttemptRecord != null) {
            return loginAttemptRecord.evaluate();
        }
        return false;
    }
}
