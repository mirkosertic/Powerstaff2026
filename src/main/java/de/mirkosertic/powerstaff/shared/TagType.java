package de.mirkosertic.powerstaff.shared;

public enum TagType {

    SCHWERPUNKT(0) {
        @Override
        public String getLabel() {
            return "Schwerpunkt";
        }
    },

    FUNKTION(1) {
        @Override
        public String getLabel() {
            return "Funktion";
        }
    },

    EINSATZORT(2) {
        @Override
        public String getLabel() {
            return "Einsatzort";
        }
    },

    BEMERKUNG(3) {
        @Override
        public String getLabel() {
            return "Bemerkung";
        }
    },

    TYP(4) {
        @Override
        public String getLabel() {
            return "Typ";
        }
    };

    private final int order;

    TagType(final int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public abstract String getLabel();
}
