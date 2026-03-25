package de.mirkosertic.powerstaff.shared;

public enum ProjectStatus {

    OFFEN(1) {
        @Override
        public String getLabel() {
            return "Offen";
        }
    },

    VERLOREN(2) {
        @Override
        public String getLabel() {
            return "Verloren";
        }
    },

    CANCELED(3) {
        @Override
        public String getLabel() {
            return "Storniert";
        }
    },

    BESETZT(4) {
        @Override
        public String getLabel() {
            return "Besetzt";
        }
    },

    SEARCH_ZU(5) {
        @Override
        public String getLabel() {
            return "Suche abgeschlossen";
        }
    };

    private final int code;

    ProjectStatus(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public abstract String getLabel();

    public static ProjectStatus fromInt(final int code) {
        for (final ProjectStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectStatus code: " + code);
    }
}
