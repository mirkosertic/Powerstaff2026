package de.mirkosertic.powerstaff.shared;

public enum ContactType {

    EMAIL {
        @Override
        public String getLabel() {
            return "E-Mail";
        }

        @Override
        public String buildLink(String value) {
            return "mailto:" + value;
        }
    },

    WEB {
        @Override
        public String getLabel() {
            return "Website";
        }

        @Override
        public String buildLink(String value) {
            if (value != null && value.startsWith("http")) {
                return value;
            }
            return "https://" + value;
        }
    },

    XING {
        @Override
        public String getLabel() {
            return "XING";
        }

        @Override
        public String buildLink(String value) {
            if (value != null && value.startsWith("http")) {
                return value;
            }
            return "https://www.xing.com/profile/" + value;
        }
    },

    GULP {
        @Override
        public String getLabel() {
            return "GULP";
        }

        @Override
        public String buildLink(String value) {
            if (value != null && value.startsWith("http")) {
                return value;
            }
            return "https://www.gulp.de/gulp2/g/profil/" + value;
        }
    },

    TELEFON {
        @Override
        public String getLabel() {
            return "Telefon";
        }

        @Override
        public String buildLink(String value) {
            return "tel:" + value;
        }
    },

    FAX {
        @Override
        public String getLabel() {
            return "Fax";
        }

        @Override
        public String buildLink(String value) {
            return "tel:" + value;
        }
    };

    public abstract String getLabel();

    public abstract String buildLink(String value);
}
