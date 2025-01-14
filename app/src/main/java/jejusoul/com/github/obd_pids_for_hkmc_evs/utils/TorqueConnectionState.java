package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

/**
 * TorqueConnectionState defines the possible states of the Torque Pro service connection.
 * 
 * This enum represents the various states that can occur during the lifecycle
 * of a connection to the Torque Pro service:
 * - DISCONNECTED: No active connection to Torque Pro
 * - CONNECTED: Successfully connected to Torque Pro
 * - ERROR: Connection error occurred
 * 
 * Usage:
 * TorqueConnectionState state = TorqueConnectionState.CONNECTED;
 * if (state == TorqueConnectionState.ERROR) {
 *     // Handle error state
 * }
 */
public enum TorqueConnectionState {
    DISCONNECTED,
    CONNECTED,
    ERROR
}
