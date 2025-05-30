package com.fullsend.jarvis.obd;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBDProtocol {
    private static final String TAG = "OBDProtocol";
    
    // Standard OBD-II Service IDs
    public static final byte SERVICE_01 = 0x01; // Show current data
    public static final byte SERVICE_02 = 0x02; // Show freeze frame data
    public static final byte SERVICE_03 = 0x03; // Show stored DTCs
    public static final byte SERVICE_04 = 0x04; // Clear DTCs
    public static final byte SERVICE_05 = 0x05; // Test results, oxygen sensor monitoring
    public static final byte SERVICE_06 = 0x06; // Test results, other component/system monitoring
    public static final byte SERVICE_07 = 0x07; // Show pending DTCs
    public static final byte SERVICE_08 = 0x08; // Control operation of on-board component/system
    public static final byte SERVICE_09 = 0x09; // Request vehicle information
    public static final byte SERVICE_0A = 0x0A; // Permanent DTCs
    
    // Enhanced diagnostic services (manufacturer specific)
    public static final byte SERVICE_10 = 0x10; // Diagnostic session control
    public static final byte SERVICE_11 = 0x11; // ECU reset
    public static final byte SERVICE_14 = 0x14; // Clear diagnostic information
    public static final byte SERVICE_18 = 0x18; // Read DTCs by status
    public static final byte SERVICE_19 = 0x19; // Read DTC information
    public static final byte SERVICE_22 = 0x22; // Read data by identifier
    public static final byte SERVICE_23 = 0x23; // Read memory by address
    public static final byte SERVICE_27 = 0x27; // Security access
    public static final byte SERVICE_2E = 0x2E; // Write data by identifier
    public static final byte SERVICE_31 = 0x31; // Routine control
    public static final byte SERVICE_34 = 0x34; // Request download
    public static final byte SERVICE_35 = 0x35; // Request upload
    public static final byte SERVICE_36 = 0x36; // Transfer data
    public static final byte SERVICE_37 = 0x37; // Request transfer exit
    
    // Common PID definitions for Service 01
    public static final byte PID_SUPPORTED_01_20 = 0x00;
    public static final byte PID_DTC_COUNT = 0x01;
    public static final byte PID_FREEZE_DTC = 0x02;
    public static final byte PID_FUEL_SYSTEM_STATUS = 0x03;
    public static final byte PID_ENGINE_LOAD = 0x04;
    public static final byte PID_COOLANT_TEMP = 0x05;
    public static final byte PID_SHORT_TERM_FUEL_TRIM_1 = 0x06;
    public static final byte PID_LONG_TERM_FUEL_TRIM_1 = 0x07;
    public static final byte PID_SHORT_TERM_FUEL_TRIM_2 = 0x08;
    public static final byte PID_LONG_TERM_FUEL_TRIM_2 = 0x09;
    public static final byte PID_FUEL_PRESSURE = 0x0A;
    public static final byte PID_INTAKE_MAP = 0x0B;
    public static final byte PID_ENGINE_RPM = 0x0C;
    public static final byte PID_VEHICLE_SPEED = 0x0D;
    public static final byte PID_TIMING_ADVANCE = 0x0E;
    public static final byte PID_INTAKE_AIR_TEMP = 0x0F;
    public static final byte PID_MAF_AIR_FLOW = 0x10;
    public static final byte PID_THROTTLE_POSITION = 0x11;
    public static final byte PID_SECONDARY_AIR_STATUS = 0x12;
    public static final byte PID_OXYGEN_SENSORS = 0x13;
    public static final byte PID_O2_B1S1 = 0x14;
    public static final byte PID_O2_B1S2 = 0x15;
    public static final byte PID_O2_B1S3 = 0x16;
    public static final byte PID_O2_B1S4 = 0x17;
    public static final byte PID_O2_B2S1 = 0x18;
    public static final byte PID_O2_B2S2 = 0x19;
    public static final byte PID_O2_B2S3 = 0x1A;
    public static final byte PID_O2_B2S4 = 0x1B;
    public static final byte PID_OBD_STANDARDS = 0x1C;
    public static final byte PID_OXYGEN_SENSORS_PRESENT = 0x1D;
    public static final byte PID_AUX_INPUT_STATUS = 0x1E;
    public static final byte PID_ENGINE_RUNTIME = 0x1F;
    
    // Extended PIDs
    public static final byte PID_SUPPORTED_21_40 = 0x20;
    public static final byte PID_DISTANCE_WITH_MIL = 0x21;
    public static final byte PID_FUEL_RAIL_PRESSURE = 0x22;
    public static final byte PID_FUEL_RAIL_GAUGE_PRESSURE = 0x23;
    public static final byte PID_COMMANDED_EGR = 0x2C;
    public static final byte PID_EGR_ERROR = 0x2D;
    public static final byte PID_COMMANDED_EVAP_PURGE = 0x2E;
    public static final byte PID_FUEL_TANK_LEVEL = 0x2F;
    public static final byte PID_WARMUPS_SINCE_CODES_CLEARED = 0x30;
    public static final byte PID_DISTANCE_SINCE_CODES_CLEARED = 0x31;
    public static final byte PID_EVAP_SYSTEM_VAPOR_PRESSURE = 0x32;
    public static final byte PID_ABSOLUTE_BAROMETRIC_PRESSURE = 0x33;
    public static final byte PID_CATALYST_TEMP_B1S1 = 0x3C;
    public static final byte PID_CATALYST_TEMP_B2S1 = 0x3D;
    public static final byte PID_CATALYST_TEMP_B1S2 = 0x3E;
    public static final byte PID_CATALYST_TEMP_B2S2 = 0x3F;
    
    private KKLCableManager kklManager;
    
    public static class OBDResponse {
        public boolean success;
        public byte[] rawData;
        public String errorMessage;
        public Map<String, Object> parsedData;
        
        public OBDResponse(boolean success) {
            this.success = success;
            this.parsedData = new HashMap<>();
        }
    }
    
    public interface OBDResponseListener {
        void onResponse(OBDResponse response);
    }
    
    public OBDProtocol(KKLCableManager kklManager) {
        this.kklManager = kklManager;
    }
    
    // Standard OBD-II Commands
    public void getCurrentData(byte pid, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_01, pid);
        sendOBDCommand(command, listener, pid);
    }
    
    public void getFreezeFrameData(byte pid, byte frame, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_02, pid, frame);
        sendOBDCommand(command, listener, pid);
    }
    
    public void getStoredDTCs(OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_03);
        sendOBDCommand(command, listener, (byte) 0x00);
    }
    
    public void clearDTCs(OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_04);
        sendOBDCommand(command, listener, (byte) 0x00);
    }
    
    public void getPendingDTCs(OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_07);
        sendOBDCommand(command, listener, (byte) 0x00);
    }
    
    public void getVehicleInfo(byte pid, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_09, pid);
        sendOBDCommand(command, listener, pid);
    }
    
    // Enhanced diagnostic commands
    public void enterDiagnosticSession(byte sessionType, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_10, sessionType);
        sendOBDCommand(command, listener, sessionType);
    }
    
    public void resetECU(byte resetType, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_11, resetType);
        sendOBDCommand(command, listener, resetType);
    }
    
    public void readDataByIdentifier(byte[] identifier, OBDResponseListener listener) {
        byte[] command = new byte[3 + identifier.length];
        command[0] = SERVICE_22;
        System.arraycopy(identifier, 0, command, 1, identifier.length);
        addChecksum(command);
        sendOBDCommand(command, listener, (byte) 0x22);
    }
    
    public void readMemoryByAddress(byte[] address, int length, OBDResponseListener listener) {
        byte[] command = new byte[4 + address.length];
        command[0] = SERVICE_23;
        command[1] = (byte) (address.length | (getByteLength(length) << 4));
        System.arraycopy(address, 0, command, 2, address.length);
        byte[] lengthBytes = intToBytes(length);
        System.arraycopy(lengthBytes, 0, command, 2 + address.length, lengthBytes.length);
        addChecksum(command);
        sendOBDCommand(command, listener, (byte) 0x23);
    }
    
    // Security access for advanced diagnostics
    public void requestSeed(byte securityLevel, OBDResponseListener listener) {
        byte[] command = buildCommand(SERVICE_27, securityLevel);
        sendOBDCommand(command, listener, securityLevel);
    }
    
    public void sendKey(byte securityLevel, byte[] key, OBDResponseListener listener) {
        byte[] command = new byte[2 + key.length + 1];
        command[0] = SERVICE_27;
        command[1] = (byte) (securityLevel + 1);
        System.arraycopy(key, 0, command, 2, key.length);
        addChecksum(command);
        sendOBDCommand(command, listener, (byte) (securityLevel + 1));
    }
    
    // Routine control for actuator tests
    public void startRoutine(byte[] routineId, byte[] params, OBDResponseListener listener) {
        byte[] command = new byte[2 + routineId.length + params.length + 1];
        command[0] = SERVICE_31;
        command[1] = 0x01; // Start routine
        System.arraycopy(routineId, 0, command, 2, routineId.length);
        System.arraycopy(params, 0, command, 2 + routineId.length, params.length);
        addChecksum(command);
        sendOBDCommand(command, listener, (byte) 0x31);
    }
    
    private byte[] buildCommand(byte service, byte... params) {
        byte[] command = new byte[1 + params.length + 1]; // service + params + checksum
        command[0] = service;
        System.arraycopy(params, 0, command, 1, params.length);
        addChecksum(command);
        return command;
    }
    
    private void addChecksum(byte[] command) {
        int checksum = 0;
        for (int i = 0; i < command.length - 1; i++) {
            checksum += command[i] & 0xFF;
        }
        command[command.length - 1] = (byte) (checksum & 0xFF);
    }
    
    private void sendOBDCommand(byte[] command, OBDResponseListener listener, byte pid) {
        if (!kklManager.isConnected()) {
            OBDResponse response = new OBDResponse(false);
            response.errorMessage = "KKL cable not connected";
            listener.onResponse(response);
            return;
        }
        
        kklManager.sendCommand(command, new KKLCableManager.CommandResponseListener() {
            @Override
            public void onResponse(byte[] rawResponse) {
                OBDResponse response = parseOBDResponse(rawResponse, pid);
                listener.onResponse(response);
            }
            
            @Override
            public void onError(String error) {
                OBDResponse response = new OBDResponse(false);
                response.errorMessage = error;
                listener.onResponse(response);
            }
        });
    }
    
    private OBDResponse parseOBDResponse(byte[] rawData, byte pid) {
        OBDResponse response = new OBDResponse(true);
        response.rawData = rawData;
        
        if (rawData.length < 2) {
            response.success = false;
            response.errorMessage = "Response too short";
            return response;
        }
        
        // Check for negative response
        if (rawData[0] == 0x7F) {
            response.success = false;
            response.errorMessage = "ECU returned error: " + getErrorDescription(rawData[2]);
            return response;
        }
        
        // Parse based on service type
        byte responseService = rawData[0];
        switch (responseService) {
            case (byte) (SERVICE_01 + 0x40): // Current data response
                parseCurrentDataResponse(response, rawData, pid);
                break;
            case (byte) (SERVICE_03 + 0x40): // Stored DTCs response
                parseStoredDTCsResponse(response, rawData);
                break;
            case (byte) (SERVICE_07 + 0x40): // Pending DTCs response
                parsePendingDTCsResponse(response, rawData);
                break;
            case (byte) (SERVICE_09 + 0x40): // Vehicle info response
                parseVehicleInfoResponse(response, rawData, pid);
                break;
            case (byte) (SERVICE_22 + 0x40): // Read data by identifier response
                parseDataByIdentifierResponse(response, rawData);
                break;
            default:
                Log.w(TAG, "Unknown response service: " + String.format("0x%02X", responseService));
                break;
        }
        
        return response;
    }
    
    private void parseCurrentDataResponse(OBDResponse response, byte[] data, byte pid) {
        if (data.length < 4) {
            response.success = false;
            response.errorMessage = "Invalid current data response length";
            return;
        }
        
        byte responsePid = data[2];
        if (responsePid != pid) {
            response.success = false;
            response.errorMessage = "PID mismatch in response";
            return;
        }
        
        // Parse based on PID
        switch (pid) {
            case PID_ENGINE_RPM:
                if (data.length >= 6) {
                    int rpm = ((data[3] & 0xFF) << 8 | (data[4] & 0xFF)) / 4;
                    response.parsedData.put("rpm", rpm);
                    response.parsedData.put("unit", "RPM");
                }
                break;
            case PID_VEHICLE_SPEED:
                if (data.length >= 5) {
                    int speed = data[3] & 0xFF;
                    response.parsedData.put("speed", speed);
                    response.parsedData.put("unit", "km/h");
                }
                break;
            case PID_COOLANT_TEMP:
                if (data.length >= 5) {
                    int temp = (data[3] & 0xFF) - 40;
                    response.parsedData.put("temperature", temp);
                    response.parsedData.put("unit", "°C");
                }
                break;
            case PID_ENGINE_LOAD:
                if (data.length >= 5) {
                    double load = ((data[3] & 0xFF) * 100.0) / 255.0;
                    response.parsedData.put("load", load);
                    response.parsedData.put("unit", "%");
                }
                break;
            case PID_THROTTLE_POSITION:
                if (data.length >= 5) {
                    double throttle = ((data[3] & 0xFF) * 100.0) / 255.0;
                    response.parsedData.put("throttle", throttle);
                    response.parsedData.put("unit", "%");
                }
                break;
            case PID_MAF_AIR_FLOW:
                if (data.length >= 6) {
                    double maf = ((data[3] & 0xFF) << 8 | (data[4] & 0xFF)) / 100.0;
                    response.parsedData.put("maf", maf);
                    response.parsedData.put("unit", "g/s");
                }
                break;
            case PID_FUEL_PRESSURE:
                if (data.length >= 5) {
                    int pressure = (data[3] & 0xFF) * 3;
                    response.parsedData.put("fuel_pressure", pressure);
                    response.parsedData.put("unit", "kPa");
                }
                break;
            case PID_INTAKE_MAP:
                if (data.length >= 5) {
                    int map = data[3] & 0xFF;
                    response.parsedData.put("intake_pressure", map);
                    response.parsedData.put("unit", "kPa");
                }
                break;
            case PID_TIMING_ADVANCE:
                if (data.length >= 5) {
                    double timing = ((data[3] & 0xFF) / 2.0) - 64.0;
                    response.parsedData.put("timing_advance", timing);
                    response.parsedData.put("unit", "° before TDC");
                }
                break;
            default:
                // For unknown PIDs, just store raw values
                byte[] values = new byte[data.length - 3];
                System.arraycopy(data, 3, values, 0, values.length);
                response.parsedData.put("raw_values", values);
                break;
        }
    }
    
    private void parseStoredDTCsResponse(OBDResponse response, byte[] data) {
        if (data.length < 3) {
            response.success = false;
            response.errorMessage = "Invalid DTC response length";
            return;
        }
        
        int dtcCount = data[2] & 0xFF;
        response.parsedData.put("dtc_count", dtcCount);
        
        List<String> dtcs = new ArrayList<>();
        for (int i = 3; i < data.length - 1; i += 2) {
            if (i + 1 < data.length) {
                String dtc = parseDTC(data[i], data[i + 1]);
                if (!dtc.equals("P0000")) { // Skip empty DTCs
                    dtcs.add(dtc);
                }
            }
        }
        response.parsedData.put("dtcs", dtcs);
    }
    
    private void parsePendingDTCsResponse(OBDResponse response, byte[] data) {
        parseStoredDTCsResponse(response, data); // Same format as stored DTCs
    }
    
    private void parseVehicleInfoResponse(OBDResponse response, byte[] data, byte pid) {
        if (data.length < 4) {
            response.success = false;
            response.errorMessage = "Invalid vehicle info response length";
            return;
        }
        
        switch (pid) {
            case 0x02: // VIN
                if (data.length >= 7) {
                    StringBuilder vin = new StringBuilder();
                    for (int i = 4; i < data.length - 1; i++) {
                        vin.append((char) data[i]);
                    }
                    response.parsedData.put("vin", vin.toString());
                }
                break;
            case 0x0A: // ECU name
                if (data.length >= 7) {
                    StringBuilder ecuName = new StringBuilder();
                    for (int i = 4; i < data.length - 1; i++) {
                        ecuName.append((char) data[i]);
                    }
                    response.parsedData.put("ecu_name", ecuName.toString());
                }
                break;
            default:
                byte[] values = new byte[data.length - 4];
                System.arraycopy(data, 4, values, 0, values.length);
                response.parsedData.put("raw_values", values);
                break;
        }
    }
    
    private void parseDataByIdentifierResponse(OBDResponse response, byte[] data) {
        if (data.length < 4) {
            response.success = false;
            response.errorMessage = "Invalid data identifier response length";
            return;
        }
        
        byte[] identifier = {data[2], data[3]};
        byte[] values = new byte[data.length - 4];
        System.arraycopy(data, 4, values, 0, values.length);
        
        response.parsedData.put("identifier", identifier);
        response.parsedData.put("data", values);
    }
    
    private String parseDTC(byte high, byte low) {
        char firstChar;
        int firstNibble = (high >> 6) & 0x03;
        switch (firstNibble) {
            case 0: firstChar = 'P'; break;
            case 1: firstChar = 'C'; break;
            case 2: firstChar = 'B'; break;
            case 3: firstChar = 'U'; break;
            default: firstChar = 'P'; break;
        }
        
        int secondNibble = (high >> 4) & 0x03;
        int thirdNibble = high & 0x0F;
        int fourthNibble = (low >> 4) & 0x0F;
        int fifthNibble = low & 0x0F;
        
        return String.format("%c%d%X%X%X", firstChar, secondNibble, thirdNibble, fourthNibble, fifthNibble);
    }
    
    private String getErrorDescription(byte errorCode) {
        switch (errorCode) {
            case 0x10: return "General reject";
            case 0x11: return "Service not supported";
            case 0x12: return "Sub-function not supported";
            case 0x13: return "Incorrect message length";
            case 0x21: return "Busy repeat request";
            case 0x22: return "Conditions not correct";
            case 0x31: return "Request out of range";
            case 0x33: return "Security access denied";
            case 0x35: return "Invalid key";
            case 0x36: return "Exceed number of attempts";
            case 0x37: return "Required time delay not expired";
            default: return "Unknown error: " + String.format("0x%02X", errorCode);
        }
    }
    
    private int getByteLength(int value) {
        if (value <= 0xFF) return 1;
        if (value <= 0xFFFF) return 2;
        if (value <= 0xFFFFFF) return 3;
        return 4;
    }
    
    private byte[] intToBytes(int value) {
        int length = getByteLength(value);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[length - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
        return bytes;
    }
}
