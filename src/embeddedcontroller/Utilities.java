package embeddedcontroller;

public class Utilities {

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
            builder.append(" ");
        }
        return builder.toString();
    }
}
