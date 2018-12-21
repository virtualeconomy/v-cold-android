package systems.v.coldwallet.Wallet;

public abstract class VSYSAsset {
    /**
     * Constant used to represent VSYS token in asset transactions.
     */
    public static final String VSYS = "VSYS";

    static String normalize(String assetId) {
        return assetId == null || assetId.isEmpty() ? VSYSAsset.VSYS : assetId;
    }

    static boolean isVSYS(String assetId) {
        return VSYS.equals(normalize(assetId));
    }

    static String toJsonObject(String assetId) {
        return isVSYS(assetId) ? null : assetId;
    }
}
