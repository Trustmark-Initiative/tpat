package tmf.host;

import org.gtri.fj.data.Option;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.gtri.fj.data.List.arrayList;


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

    public static Option<Role> fromValueOption(final String value) {
        requireNonNull("value", value);

        return arrayList(Role.values()).filter(role -> role.getValue().equals(value)).headOption();
    }
}
