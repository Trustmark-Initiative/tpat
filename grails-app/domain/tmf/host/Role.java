package tmf.host;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;


public enum Role {
    ROLE_ADMIN("TPAT Administrator", "tpat-admin");

    private final String label;
    private final String value;

    Role(
            final String label,
            final String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public static Optional<Role> fromValue(final String value) {
        requireNonNull("value", value);

        return Arrays.asList(Role.values()).stream()
                .filter(role -> role.getValue().equals(value))
                .findAny();
    }
}
