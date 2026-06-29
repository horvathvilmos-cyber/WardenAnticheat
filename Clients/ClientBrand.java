package hu.ClashRoyale456.wardenAnticheat.Clients;

public enum ClientBrand {
    VANILLA("Vanilla"),
    FABRIC("Fabric"),
    LUNAR("Lunar Client"),
    LUNAR_FABRIC("Lunar (Fabric)"),
    LUNAR_OPTIFINE("Lunar (OptiFine)"),
    FEATHER_FABRIC("Feather (Fabric)"),
    OPTIFINE("OptiFine"),
    BABRICK("Babrick"),
    UNKNOWN("Unknown");

    private final String displayName;

    ClientBrand(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static ClientBrand fromString(String brand) {
        if (brand == null) return UNKNOWN;
        String lower = brand.toLowerCase();

        if (lower.contains("lunar") && lower.contains("fabric")) return LUNAR_FABRIC;
        if (lower.contains("lunar") && lower.contains("optifine")) return LUNAR_OPTIFINE;
        if (lower.contains("lunarclient") || lower.contains("lunar client") || lower.contains("lunar")) return LUNAR;
        if (lower.contains("feather") && lower.contains("fabric")) return FEATHER_FABRIC;
        if (lower.contains("feather")) return FEATHER_FABRIC;
        if (lower.contains("fabric")) return FABRIC;
        if (lower.contains("optifine") || lower.contains("of")) return OPTIFINE;
        if (lower.contains("babric") || lower.contains("babrick")) return BABRICK;
        if (lower.equals("vanilla")) return VANILLA;

        return UNKNOWN;
    }
}