package xyz.brckts.portablestonecutter.network;

public class MessageHandlerOnClient {
    public static boolean isThisProtocolAcceptedByClient(String protocolVersion) {
        return NetworkHandler.MESSAGE_PROTOCOL_VERSION.equals(protocolVersion);
    }
}
