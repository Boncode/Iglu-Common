package org.ijsberg.iglu.usermanagement.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.ijsberg.iglu.util.time.TimeSupport.*;

public class LoginAttemptTracker {

    private class LoginAttemptRecord {

        private Date firstAttemptTime = new Date();
        private int nrofAttempts = 0;

        private void reset() {
            firstAttemptTime = new Date();
            nrofAttempts = 0;
        }

        private boolean evaluate() {
            if(firstAttemptTime.before(nowMinus(attemptWindowInMinutes * MINUTE_IN_MS))) {
                reset();
                return false;
            }
            if(nrofAttempts >= failedAttemptsThresholdForLocking) {
                return true;
            }
            return false;
        }

    }

    private int failedAttemptsThresholdForLocking = 5;
    private int attemptWindowInMinutes = 15;

    private Map<String, LoginAttemptRecord> loginAttemptRecords = new HashMap<>();

    public void reportLoginAttemptFailed(String userId) {
        LoginAttemptRecord loginAttemptRecord = loginAttemptRecords.get(userId);
        if(loginAttemptRecord == null) {
            loginAttemptRecord = new LoginAttemptRecord();
            loginAttemptRecords.put(userId, loginAttemptRecord);
        }
        loginAttemptRecord.evaluate();
        loginAttemptRecord.nrofAttempts++;
    }

    public void reportLoginAttemptSucceeded(String userId) {
        loginAttemptRecords.remove(userId);
    }

    public boolean isLocked(String userId) {
        LoginAttemptRecord loginAttemptRecord = loginAttemptRecords.get(userId);
        if(loginAttemptRecord != null) {
            return loginAttemptRecord.evaluate();
        }
        return false;
    }

}
