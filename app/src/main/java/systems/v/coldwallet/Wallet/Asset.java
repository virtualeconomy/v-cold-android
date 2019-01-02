package systems.v.coldwallet.Wallet;

public abstract class Asset {
    /**
     * Constant used to represent VSYS token in asset transactions.
     */
    public static final String VSYS = "VSYS";

    static String normalize(String assetId) {
        return assetId == null || assetId.isEmpty() ? Asset.VSYS : assetId;
    }

    static boolean isVSYS(String assetId) {
        return VSYS.equals(normalize(assetId));
    }

    static String toJsonObject(String assetId) {
        return isVSYS(assetId) ? null : assetId;
    }
}
